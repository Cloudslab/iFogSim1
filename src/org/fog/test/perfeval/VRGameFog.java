package org.fog.test.perfeval;

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
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

public class VRGameFog {
	static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	static List<Sensor> sensors = new ArrayList<Sensor>();
	static List<Actuator> actuators = new ArrayList<Actuator>();
	
	static boolean CLOUD = false;
	
	static int numOfDepts = 1;
	static int numOfMobilesPerDept = 4;
	static double EEG_TRANSMISSION_TIME = 5.1;
	//static double EEG_TRANSMISSION_TIME = 10;
	public static void main(String[] args) {

		if(args.length < 4){
			System.out.println("PLEASE ENTER #depts, #mobiles_per_dept and EEG inter-transmission time.");
			System.exit(0);
		}
		
		CLOUD = (args[0].equals("CLOUD"))?true:false;
		numOfDepts = Integer.parseInt(args[1]);
		numOfMobilesPerDept = Integer.parseInt(args[2]);
		EEG_TRANSMISSION_TIME = (Integer.parseInt(args[3])==10)?10.0:5.1;
		
		Log.printLine("Starting VRGame...");

		try {
			Log.disable();
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			CloudSim.init(num_user, calendar, trace_flag);

			String appId = "vr_game";
			
			FogBroker broker = new FogBroker("broker");
			
			Application application = createApplication(appId, broker.getId());
			application.setUserId(broker.getId());
			
			//PhysicalTopology physicalTopology = JsonToTopology.getPhysicalTopology(broker.getId(), appId, "topologies/test_instance_count");

			createFogDevices(broker.getId(), appId);
			
			ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
			
			if(CLOUD){
				moduleMapping.addModuleToDevice("connector", "cloud", numOfDepts*numOfMobilesPerDept);
				moduleMapping.addModuleToDevice("classifier", "cloud", numOfDepts*numOfMobilesPerDept);
				for(FogDevice device : fogDevices){
					if(device.getName().startsWith("m")){
						moduleMapping.addModuleToDevice("client", device.getName(), 1);
					}
				}
			}else{
				moduleMapping.addModuleToDevice("connector", "cloud", numOfDepts*numOfMobilesPerDept);
			}
			
			
			Controller controller = new Controller("master-controller", fogDevices, sensors, 
					actuators, moduleMapping);
			
			controller.submitApplication(application, 0);

			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			Log.printLine("VRGame finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	private static void createFogDevices(int userId, String appId) {
		FogDevice cloud = createFogDevice("cloud", 40000, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25);
		cloud.setParentId(-1);
		FogDevice proxy = createFogDevice("proxy-server", 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333);
		proxy.setParentId(cloud.getId());
		proxy.setUplinkLatency(100);
		
		fogDevices.add(cloud);
		fogDevices.add(proxy);
		
		for(int i=0;i<numOfDepts;i++){
			addDept(i+"", userId, appId, proxy.getId());
		}
		
	}

	private static FogDevice addDept(String id, int userId, String appId, int parentId){
		FogDevice dept = createFogDevice("d-"+id, 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333);
		fogDevices.add(dept);
		dept.setParentId(parentId);
		dept.setUplinkLatency(4);
		for(int i=0;i<numOfMobilesPerDept;i++){
			String mobileId = id+"-"+i;
			FogDevice mobile = addMobile(mobileId, userId, appId, dept.getId());
			mobile.setUplinkLatency(2);
			fogDevices.add(mobile);
		}
		return dept;
	}
	
	private static FogDevice addMobile(String id, int userId, String appId, int parentId){
		FogDevice mobile = createFogDevice("m-"+id, 1000, 1000, 10000, 270, 3, 0, 87.53, 82.44);
		mobile.setParentId(parentId);
		Sensor eegSensor = new Sensor("s-"+id, "EEG", userId, appId, new DeterministicDistribution(EEG_TRANSMISSION_TIME));
		sensors.add(eegSensor);
		Actuator display = new Actuator("a-"+id, userId, appId, "DISPLAY");
		actuators.add(display);
		eegSensor.setGatewayDeviceId(mobile.getId());
		eegSensor.setLatency(6.0);
		display.setGatewayDeviceId(mobile.getId());
		display.setLatency(1.0);
		return mobile;
	}
	
	private static FogDevice createFogDevice(String nodeName, long mips,
			int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {
		
		List<Pe> peList = new ArrayList<Pe>();

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

		int hostId = FogUtils.generateEntityId();
		long storage = 1000000; // host storage
		int bw = 10000;

		PowerHost host = new PowerHost(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerOverbooking(bw),
				storage,
				peList,
				new StreamOperatorScheduler(peList),
				new FogLinearPowerModel(busyPower, idlePower)
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
			fogdevice = new FogDevice(nodeName, characteristics, 
					new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		fogdevice.setLevel(level);
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
	
		if(EEG_TRANSMISSION_TIME==10)
			application.addAppEdge("EEG", "client", 2000, 500, "EEG", Tuple.UP, AppEdge.SENSOR);
		else
			application.addAppEdge("EEG", "client", 3000, 500, "EEG", Tuple.UP, AppEdge.SENSOR);
		application.addAppEdge("client", "classifier", 3500, 500, "_SENSOR", Tuple.UP, AppEdge.MODULE);
		application.addAppEdge("classifier", "connector", 1000, 1000, 1000, "PLAYER_GAME_STATE", Tuple.UP, AppEdge.MODULE);
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