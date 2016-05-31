package org.fog.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
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
import org.fog.utils.FogUtils;
import org.fog.utils.GeoCoverage;
import org.fog.utils.distribution.DeterministicDistribution;

public class GameModuleMapping {

	@SuppressWarnings("serial")
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
			
			int transmitInterval = 1000;

			Application application = createApplication(appId, broker.getId());
			application.setUserId(broker.getId());
			List<FogDevice> fogDevices = createFogDevices(appId, broker.getId(), transmitInterval);
			
			final Sensor s0 = createSensor("EEGSensor-0", application, broker.getId(), CloudSim.getEntityId("mobile-0"), transmitInterval, 2000, 100, "EEG", "client");
			final Sensor s1 = createSensor("EEGSensor-1", application, broker.getId(), CloudSim.getEntityId("mobile-1"), transmitInterval, 2000, 100, "EEG", "client");
			final Actuator actuator0 = createActuator("Display-0", appId, broker.getId(), CloudSim.getEntityId("mobile-0"), "ACTUATOR", "client");
			final Actuator actuator1 = createActuator("Display-1", appId, broker.getId(), CloudSim.getEntityId("mobile-1"), "ACTUATOR", "client");
			
			List<Sensor> sensors = new ArrayList<Sensor>(){{add(s0);add(s1);}};
			List<Actuator> actuators = new ArrayList<Actuator>(){{add(actuator0);add(actuator1);}};
			
			application.getModuleByName("client").subscribeActuator(actuator0.getId(), "ACTUATOR");
			application.getModuleByName("client").subscribeActuator(actuator1.getId(), "ACTUATOR");
			
			ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
			moduleMapping.addModuleToDevice("client", "mobile-0");
			moduleMapping.addModuleToDevice("client", "mobile-1");
			
			//moduleMapping.addModuleToDevice("client", "cloud");
			
			//moduleMapping.addModuleToDevice("classifier", "gateway-0");
			//moduleMapping.addModuleToDevice("classifier", "gateway-1");
			
			moduleMapping.addModuleToDevice("classifier", "router");
			
			//moduleMapping.addModuleToDevice("classifier", "cloud");
			moduleMapping.addModuleToDevice("connector", "cloud");
			
			Controller controller = new Controller("master-controller", fogDevices, sensors, actuators, moduleMapping);
			
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
		Sensor s = new Sensor(sensorName, userId, app.getAppId(), gatewayDeviceId, 2, null, new DeterministicDistribution(transmitInterval), tupleCpuSize, tupleNwSize, tupleType, destOpId);
		return s;
		//app.registerSensor(sensor0);
	}
	private static Actuator createActuator(String actuatorName, String appId, int userId, int gatewayDeviceId, String actuatorType, String srcModuleName){
		Actuator actuator = new Actuator(actuatorName, userId, appId, gatewayDeviceId, 2, null, actuatorType, srcModuleName);
		return actuator;
		//app.registerSensor(sensor0);
	}
	
	@SuppressWarnings("serial")
	private static List<FogDevice> createFogDevices(String appId, int userId, int transmitInterval) {
		final FogDevice gw0 = createFogDevice("mobile-0", 1000, new GeoCoverage(-100, 100, -100, 100), 1000, 1000, 10);
		final FogDevice gw1 = createFogDevice("mobile-1", 1000, new GeoCoverage(-100, 100, -100, 100), 1000, 1000, 10);
		
		final FogDevice mid = createFogDevice("router", 1000, new GeoCoverage(-100, 100, -100, 100), 1000, 1000, 50);
		
		final FogDevice cloud = createFogDevice("cloud", FogUtils.MAX, new GeoCoverage(-FogUtils.MAX, FogUtils.MAX, -FogUtils.MAX, FogUtils.MAX), FogUtils.MAX, 1000, 1);
		cloud.setChildrenIds(new ArrayList<Integer>(){{add(mid.getId());}});
		mid.setChildrenIds(new ArrayList<Integer>(){{add(gw1.getId());add(gw0.getId());}});
		
		gw0.setParentId(mid.getId());
		gw1.setParentId(mid.getId());
		mid.setParentId(cloud.getId());
		cloud.setParentId(-1);

		List<FogDevice> fogDevices = new ArrayList<FogDevice>(){{add(gw0);add(gw1);add(mid);add(cloud);}};
		return fogDevices;
	}

	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 */
	private static FogDevice createFogDevice(String name, int mips, GeoCoverage geoCoverage, double uplinkBandwidth, double downlinkBandwidth, double latency) {

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

		int hostId = FogUtils.generateEntityId();
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;

		PowerHost host = new PowerHost(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerOverbooking(bw),
				storage,
				peList,
				new StreamOperatorScheduler(peList),
				new PowerModelLinear(100, 40)
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
				costPerStorage, costPerBw);

		FogDevice fogdevice = null;
		try {
			fogdevice = new FogDevice(name, characteristics, 
					new AppModuleAllocationPolicy(hostList), storageList, 10, uplinkBandwidth, downlinkBandwidth, latency, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fogdevice;
	}
	
	@SuppressWarnings({"serial" })
	private static Application createApplication(String appId, int userId){
		
		Application application = Application.createApplication(appId, userId);
		application.addAppModule("client", 10);
		application.addAppModule("classifier", 10);
		application.addAppModule("connector", 10);
		
		application.addTupleMapping("client", "EEG", "_SENSOR", new FractionalSelectivity(0.9));
		application.addTupleMapping("client", "CLASSIFICATION", "SELF_STATE_UPDATE", new FractionalSelectivity(1.0));
		application.addTupleMapping("classifier", "_SENSOR", "CLASSIFICATION", new FractionalSelectivity(1.0));
		application.addTupleMapping("client", "GLOBAL_GAME_STATE", "GLOBAL_STATE_UPDATE", new FractionalSelectivity(1.0));
	
		application.addAppEdge("EEG", "client", 2000, 500, "EEG", Tuple.UP, AppEdge.SENSOR);
		application.addAppEdge("client", "classifier", 3500, 500, "_SENSOR", Tuple.UP, AppEdge.MODULE);
		application.addAppEdge("classifier", "connector", 100, 1000, 1000, "PLAYER_GAME_STATE", Tuple.UP, AppEdge.MODULE);
		application.addAppEdge("classifier", "client", 14, 500, "CLASSIFICATION", Tuple.DOWN, AppEdge.MODULE);
		application.addAppEdge("connector", "client", 100, 28, 1000, "GLOBAL_GAME_STATE", Tuple.DOWN, AppEdge.MODULE);
		application.addAppEdge("client", "DISPLAY", 1000, 500, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);
		application.addAppEdge("client", "DISPLAY", 1000, 500, "GLOBAL_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);
		
		final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("EEG");add("client");add("classifier");add("client");add("DISPLAY");}});
		List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);}};
		
		application.setLoops(loops);
		return application;
	}
}