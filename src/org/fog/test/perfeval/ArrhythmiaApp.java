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
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

/**
 * Simulation setup for Arrhythmia classification
 * @author DongJoo Seo
 * based on VRGameFog example
 */
public class ArrhythmiaApp {
	static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	static List<Sensor> sensors = new ArrayList<Sensor>();
	static List<Actuator> actuators = new ArrayList<Actuator>();
	static List<AppLoop> loops = new ArrayList<AppLoop>();
	static boolean CLOUD = false;
	
	static int numOfGWNode = 1;
	static int numOfSensorNode = 16;
	static double ECG_TRANSMISSION_TIME = 6.1;
	
	public static void main(String[] args) {

		Log.printLine("Starting Arrhythmia Applications...");

		try {
			//Log.disable();
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			CloudSim.init(num_user, calendar, trace_flag);

			String appId = "arrhythmia"; // identifier of the application
			
			FogBroker broker = new FogBroker("broker"); // now this is empty class
			
			Application application = createApplication(appId, broker.getId());
			application.setUserId(broker.getId());
			
			createFogDevices(broker.getId(), appId);
			
			ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping

			
			moduleMapping.addModuleToDevice("cloud_updater", "cloud"); // fixing all instances of the Connector module to the Cloud
			moduleMapping.addModuleToDevice("classification_module", "fog-layer"); // fixing all instances of the Concentration Calculator module to the Cloud
			for(FogDevice device : fogDevices){
				if(device.getName().startsWith("m")){
					String[] temps = device.getName().split("-");
					String last = temps[temps.length-1];
					Log.formatLine("device name : %s , app name : %s", device.getName(),"client"+"-"+Integer.valueOf(last));
					moduleMapping.addModuleToDevice("client"+"-"+Integer.valueOf(last), device.getName());  // fixing all instances of the Client module to the Smartphones
				}
			}
			
			
			Controller controller = new Controller("master-controller", fogDevices, sensors, 
					actuators);
			
			controller.submitApplication(application, 0, new ModulePlacementMapping(fogDevices, application, moduleMapping));

			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			Log.printLine("arrhythmia finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	/**
	 * Creates the fog devices in the physical topology of the simulation.
	 * @param userId
	 * @param appId
	 */
	private static void createFogDevices(int userId, String appId) {

		// assumption
		FogDevice cloud = createFogDevice("cloud", 150000, 400000, 10000, 10000, 0, 0.01, 16*103, 16*83.25); // creates the fog device Cloud at the apex of the hierarchy with level=0
		cloud.setParentId(-1);
		
		fogDevices.add(cloud);
		
		for(int i=0;i<numOfGWNode;i++){
			addGw(i+"", userId, appId, cloud.getId()); // adding a fog device for every Gateway in physical topology. The parent of each gateway is the Proxy Server
		}
	}

	private static FogDevice addGw(String id, int userId, String appId, int parentId){

		// HP Compaq 8200 Elite
		// clock : 3.10GH - quad core - 3100MHz 
		// RAM : 16GB
		// Storage : 250GB HDD
		// MIPS : 40000 (just assumed)
		
		FogDevice fog = createFogDevice("fog-layer", 40000, 16000, 10000, 10000, 1, 0.0, 107.339, 83.4333); // creates the fog device Proxy Server (level=1)
		fog.setParentId(parentId); // setting Cloud as parent of the Proxy Server
		fog.setUplinkLatency(100); // latency of connection between gateways and cloud is 100 ms
		fogDevices.add(fog);
		
		for(int i=0;i<numOfSensorNode;i++){
			String sensorNodeId = id+"-"+i;
			FogDevice sensorNode = addSensorsNode(sensorNodeId, userId, appId, fog.getId()); // adding sensor node to the physical topology. 
			sensorNode.setUplinkLatency(10); // latency of connection between the smartphone and proxy server is 10 ms
			fogDevices.add(sensorNode);
		}
		return fog;
	}
	
	private static FogDevice addSensorsNode(String id, int userId, String appId, int parentId){
		// Raspberry Pi 3
		// clock : 1.2GHz
		// RAM : 1GB
		// Storage : 16GB eMMC
		// MIPS : 3500 (http://www.roylongbottom.org.uk/dhrystone%20results.htm)

		FogDevice sensorNode = createFogDevice("m-"+id, 3500, 1000, 10000, 270, 2, 0, 87.53, 82.44);
		sensorNode.setParentId(parentId);
		Sensor ecgSensor = new Sensor("s-"+id, "ECG", userId, appId, new DeterministicDistribution(ECG_TRANSMISSION_TIME)); // inter-transmission time of EEG sensor follows a deterministic distribution
		sensors.add(ecgSensor);
		Actuator notification = new Actuator("a-"+id, userId, appId, "NOTIFIER");
		actuators.add(notification);
		ecgSensor.setGatewayDeviceId(sensorNode.getId());
		ecgSensor.setLatency(6.0);  // latency of connection between ECG sensors and the parent Smartphone is 6 ms
		notification.setGatewayDeviceId(sensorNode.getId());
		notification.setLatency(1.0);  // latency of connection between Display actuator and the parent Sensor node is 1 ms
		return sensorNode;
	}
	
	/**
	 * Creates a vanilla fog device
	 * @param nodeName name of the device to be used in simulation
	 * @param mips MIPS
	 * @param ram RAM
	 * @param upBw uplink bandwidth
	 * @param downBw downlink bandwidth
	 * @param level hierarchy level of the device
	 * @param ratePerMips cost rate per MIPS used
	 * @param busyPower
	 * @param idlePower
	 * @return
	 */
	private static FogDevice createFogDevice(String nodeName, long mips,
			int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {
		
		List<Pe> peList = new ArrayList<Pe>();

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
		String vmm = "docker";
		double time_zone = 10.0; // time zone this resource located

		// TODO: make more configurable
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

	/**
	 * Function to create the arrhythmia application in the DDF model. 
	 * @param appId unique identifier of the application
	 * @param userId identifier of the user of the application
	 * @return
	 */
	@SuppressWarnings({"serial" })
	private static Application createApplication(String appId, int userId){
		
		Application application = Application.createApplication(appId, userId); // creates an empty application model (empty directed graph)
		
		/*
		 * Adding modules (vertices) to the application model (directed graph)
		 */
		// TODO : check the RAM size of application and apply below
		
		for(int i=0; i < numOfSensorNode; i++) {
			application.addAppModule("client"+"-"+String.valueOf(i), 10); // adding module Client to the application model
		}
		application.addAppModule("classification_module", 10); // adding classification module to the application model
		application.addAppModule("cloud_updater", 10); // adding cloud updater module to the application model
		
		/*
		 * Connecting the application modules (vertices) in the application model (directed graph) with edges
		 */
		
		/*  The workflow of below sample application
		 * 
		 *  ECG --------> client --------> classification module --------> cloud updater
		 *                 |  ^                     |
		 *                 |  |                     |
		 *  Notifier  <----   -----------------------
		 *                                        
		 */
		
		// TODO : Making sure about each params(tupleCpuLenth, tupleNwLength)


		for(int i=0; i< numOfSensorNode; i++) {
			application.addAppEdge("ECG", "client"+"-"+String.valueOf(i), 500, 500, "ECG", Tuple.UP, AppEdge.SENSOR);			
			// adding edge from ECG (sensor) to Client module carrying tuples of type ECG	 
			application.addAppEdge("client"+"-"+String.valueOf(i), "classification_module", 3500, 500, "_SENSOR"+"-"+String.valueOf(i), Tuple.UP, AppEdge.MODULE);
			// adding edge from Client to Concentration Calculator module carrying tuples of type _SENSOR
		}
	
		application.addAppEdge("classification_module", "cloud_updater", 100, 100, 1000, "USERS_STATE", Tuple.UP, AppEdge.MODULE); 
		// adding periodic edge (period=1000ms) from Concentration Calculator to Connector module carrying tuples of type USERS_STATE

		for(int i=0; i< numOfSensorNode; i++) {		
			application.addAppEdge("classification_module", "client"+"-"+String.valueOf(i), 14, 500, "CLASSIFIED_RESULT"+"-"+String.valueOf(i), Tuple.DOWN, AppEdge.MODULE);
			// adding edge from Concentration Calculator to Client module carrying tuples of type CONCENTRATION
			application.addAppEdge("client"+"-"+String.valueOf(i), "NOTIFIER", 50, 20, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);
			// adding edge from Client module to Display (actuator) carrying tuples of type SELF_STATE_UPDATE
		}

		/*
		 * Defining the input-output relationships (represented by selectivity) of the application modules. 
		 */
		for(int i=0; i< numOfSensorNode; i++) {		
			application.addTupleMapping("client"+"-"+String.valueOf(i), "ECG", "_SENSOR"+"-"+String.valueOf(i), new FractionalSelectivity(0.9)); 
			// 0.9 tuples of type _SENSOR are emitted by Client module per incoming tuple of type ECG			
			application.addTupleMapping("classification_module", "_SENSOR"+"-"+String.valueOf(i), "CLASSIFIED_RESULT"+"-"+String.valueOf(i), new FractionalSelectivity(1.0));
			// 1.0 tuples of type CONCENTRATION are emitted by Concentration Calculator module per incoming tuple of type _SENSOR 

		}

		application.addTupleMapping("classification_module", "_SENSOR", "USERS_STATE", new FractionalSelectivity(1.0));
		
		for(int i=0; i< numOfSensorNode; i++) {		
			application.addTupleMapping("client"+"-"+String.valueOf(i), "CLASSIFIED_RESULT"+"-"+String.valueOf(i), "SELF_STATE_UPDATE", new FractionalSelectivity(1.0)); 
			// 1.0 tuples of type SELF_STATE_UPDATE are emitted by Client module per incoming tuple of type CONCENTRATION 
		}
		/*
		 * Defining application loops to monitor the latency of. 
		 * Here, we add only one loop for monitoring : ECG(sensor) -> Client -> Classification Module -> Client -> NOTIFIER (actuator)
		 */
		
		for(int i=0; i< numOfSensorNode; i++) {
			String cli = new String("client"+"-"+String.valueOf(i));
			loops.add(new AppLoop(new ArrayList<String>(){{add("ECG");add(cli);add("classification_module");add(cli);add("NOTIFIER");}}));
//			AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("ECG");add("client");add("classification_module");add("client");add("NOTIFIER");}});
		}
//		List<AppLoop> loops = new ArrayList<AppLoop>(1){{add(loop1);}};
		application.setLoops(loops);
		return application;
	}
}