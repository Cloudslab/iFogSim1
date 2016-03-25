package org.fog.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.scheduler.TupleScheduler;
import org.fog.utils.FogUtils;
import org.fog.utils.GeoCoverage;
import org.fog.utils.distribution.DeterministicDistribution;

public class VRGameModuleMapping {

	public static void main(String[] args) {

		Log.printLine("Starting VRGame...");

		try {
			Log.disable();
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			CloudSim.init(num_user, calendar, trace_flag);

			String appId = "vr_game";
			
			FogBroker broker = new FogBroker("broker");
			
			int transmitInterval = 100;

			Application application = createApplication(appId, broker.getId(), transmitInterval);

			List<FogDevice> fogDevices = createFogDevices(appId, broker.getId(), transmitInterval);
			
			Sensor s0 = createSensor("EEGSensor-0", application, broker.getId(), CloudSim.getEntityId("gateway-0"), transmitInterval, 100, 100, "SENSOR", "client");
			Sensor s1 = createSensor("EEGSensor-1", application, broker.getId(), CloudSim.getEntityId("gateway-1"), transmitInterval, 100, 100, "SENSOR", "client");
			Actuator actuator0 = createActuator("Display-0", appId, broker.getId(), CloudSim.getEntityId("gateway-0"), "ACTUATOR", "client");
			Actuator actuator1 = createActuator("Display-1", appId, broker.getId(), CloudSim.getEntityId("gateway-1"), "ACTUATOR", "client");
			
			application.getModuleByName("client").subscribeActuator(actuator0.getId(), "ACTUATOR");
			application.getModuleByName("client").subscribeActuator(actuator1.getId(), "ACTUATOR");
			
			
			ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
			moduleMapping.addModuleToDevice("client", "gateway-0");
			moduleMapping.addModuleToDevice("client", "gateway-1");
			moduleMapping.addModuleToDevice("classifier", "gateway-0");
			moduleMapping.addModuleToDevice("classifier", "gateway-1");
			/*moduleMapping.addModuleToDevice("classifier", "cloud");*/
			moduleMapping.addModuleToDevice("tuner", "cloud");
			
			Controller controller = new Controller("master-controller", fogDevices, moduleMapping);
			
			s0.setControllerId(controller.getId());
			s1.setControllerId(controller.getId());
			s0.setApp(application);
			s1.setApp(application);
			actuator0.setApp(application);
			actuator1.setApp(application);
			
			controller.submitApplication(application, 0);
			
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			Log.printLine("VRGame finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	private static Sensor createSensor(String sensorName, Application app, int userId, int gatewayDeviceId, int transmitInterval, int tupleCpuSize, int tupleNwSize, String tupleType, String destOpId){
		Sensor s = new Sensor(sensorName, userId, app.getAppId(), gatewayDeviceId, 10, null, new DeterministicDistribution(transmitInterval), tupleCpuSize, tupleNwSize, tupleType, destOpId);
		return s;
		//app.registerSensor(sensor0);
	}
	private static Actuator createActuator(String actuatorName, String appId, int userId, int gatewayDeviceId, String actuatorType, String srcModuleName){
		Actuator actuator = new Actuator(actuatorName, userId, appId, gatewayDeviceId, null, actuatorType, srcModuleName);
		return actuator;
		//app.registerSensor(sensor0);
	}
	
	@SuppressWarnings("serial")
	private static List<FogDevice> createFogDevices(String appId, int userId, int transmitInterval) {
		final FogDevice gw0 = createFogDevice("gateway-0", 1000, new GeoCoverage(-100, 100, -100, 100), 1000, 1000, 50, 10);
		final FogDevice gw1 = createFogDevice("gateway-1", 1000, new GeoCoverage(-100, 100, -100, 100), 1000, 1000, 50, 10);
		
		final FogDevice cloud = createFogDevice("cloud", FogUtils.MAX, new GeoCoverage(-FogUtils.MAX, FogUtils.MAX, -FogUtils.MAX, FogUtils.MAX), FogUtils.MAX, 1000, 1, 1);
		cloud.setChildrenIds(new ArrayList<Integer>(){{add(gw0.getId());add(gw1.getId());}});
		
		gw0.setParentId(cloud.getId());
		gw1.setParentId(cloud.getId());
		cloud.setParentId(-1);

		List<FogDevice> fogDevices = new ArrayList<FogDevice>(){{add(gw0);add(gw1);add(cloud);}};
		return fogDevices;
	}

	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 */
	private static FogDevice createFogDevice(String name, int mips, GeoCoverage geoCoverage, double uplinkBandwidth, double downlinkBandwidth, double latency, double actuatorDelay) {

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

		int hostId = FogUtils.generateEntityId();
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;

		Host host = new Host(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerOverbooking(bw),
				storage,
				peList,
				new StreamOperatorScheduler(peList)
			);

		List<Host> hostList = new ArrayList<Host>();
		hostList.add(host);

		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
													// devices by now

		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
				arch, os, vmm, host, time_zone, cost, costPerMem,
				costPerStorage, costPerBw, geoCoverage);

		FogDevice fogdevice = null;
		try {
			fogdevice = new FogDevice(name, geoCoverage, characteristics, 
					new AppModuleAllocationPolicy(hostList), storageList, 0, uplinkBandwidth, downlinkBandwidth, latency, actuatorDelay);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fogdevice;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	private static Application createApplication(String appId, int userId, int transmitInterval){
		int mips = 1000;
		long size = 10000; // image size (MB)
		int ram = 512; // vm memory (MB)
		long bw = 1000;
		String vmm = "Xen"; // VMM name
		
		Map<Pair<String, String>, Double> clientSelectivityMap = new HashMap<Pair<String, String>, Double>();
		clientSelectivityMap.put(new Pair("SENSOR", "_SENSOR"),  1.0);
		clientSelectivityMap.put(new Pair("CLASSIFICATION", "ACTUATOR"),  1.0);
		final AppModule client = new AppModule(FogUtils.generateEntityId(), "client", null, appId, userId, 
				mips, ram, bw, size, vmm, new TupleScheduler(mips, 1), clientSelectivityMap);
		
		Map<Pair<String, String>, Double> classifierSelectivityMap = new HashMap<Pair<String, String>, Double>();
		classifierSelectivityMap.put(new Pair("_SENSOR", "CLASSIFICATION"),  1.0);
		classifierSelectivityMap.put(new Pair("_SENSOR", "HISTORY"), 1.0);
		final AppModule classifier = new AppModule(FogUtils.generateEntityId(), "classifier", null, appId, userId, 
				mips, ram, bw, size, vmm, new TupleScheduler(mips, 1), classifierSelectivityMap);

		Map<Pair<String, String>, Double> tunerSelectivityMap = new HashMap<Pair<String, String>, Double>();
		tunerSelectivityMap.put(new Pair("HISTORY", "TUNING_PARAMS"),  1.0);
		final AppModule tuner = new AppModule(FogUtils.generateEntityId(), "tuner", null, appId, userId, 
				mips, ram, bw, size, vmm, new TupleScheduler(mips, 1), tunerSelectivityMap);
		
		List<AppModule> modules = new ArrayList<AppModule>(){{add(client);add(classifier);add(tuner);}};
		
		final AppEdge edgeSensor = new AppEdge("SENSOR", "client", 1000, 100, "SENSOR", Tuple.UP, AppEdge.SENSOR);
		final AppEdge edge_Sensor = new AppEdge("client", "classifier", 1000, 100, "_SENSOR", Tuple.UP, AppEdge.MODULE);
		final AppEdge edgeHistory = new AppEdge("classifier", "tuner", 1000, 100, "HISTORY", Tuple.UP, AppEdge.MODULE);
		final AppEdge edgeClassification = new AppEdge("classifier", "client", 1000, 100, "CLASSIFICATION", Tuple.DOWN, AppEdge.MODULE);
		final AppEdge edgeTuningParams = new AppEdge("tuner", "classifier", 1000, 100, "TUNING_PARAMS", Tuple.DOWN, AppEdge.MODULE);
		final AppEdge edgeActuator = new AppEdge("client", "ACTUATOR", 1000, 100, "ACTUATOR", Tuple.DOWN, AppEdge.ACTUATOR);
		List<AppEdge> edges = new ArrayList<AppEdge>(){{add(edgeSensor);add(edge_Sensor);add(edgeHistory);add(edgeClassification);
		add(edgeTuningParams);add(edgeActuator);}};
		
		final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("SENSOR");add("client");add("classifier");add("client");add("ACTUATOR");}});
		final AppLoop loop2 = new AppLoop(new ArrayList<String>(){{add("classifier");add("tuner");add("classifier");}});
		List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);add(loop2);}};
		
		
		
		
		GeoCoverage geoCoverage = new GeoCoverage(-100, 100, -100, 100);
		Application app = new Application(appId, modules, edges, loops, geoCoverage);
		return app;
	}
}