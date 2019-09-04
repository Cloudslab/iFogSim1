package org.fog.test.perfeval;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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
import org.fog.utils.Config;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

/**
 * Simulation setup for Multi class Applications on each device
 * @author DongJoo Seo
 * based on VRGameFog example
 */
public class MultiClassApp {
	static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	static List<Sensor> sensors = new ArrayList<Sensor>();
	static List<Actuator> actuators = new ArrayList<Actuator>();
	static List<AppLoop> loops = new ArrayList<AppLoop>();
	static String[] appIds = new String[] {"class1","class2","class3","class4"};
	static boolean CLOUD = false;
	static int numOfGWNode = 1;
	static Object[] configs = new Object[]{};
	static int using_fresult = -1;
	static int USING_EXECUTION_MAP = -1;
	static int numOfSensorNode = Config.NUMBER_OF_EDGE;
	static double CLASS1_TRANSMISSION_TIME = 1000;
	static double CLASS2_TRANSMISSION_TIME = 100;
	static double CLASS3_TRANSMISSION_TIME = 500;
	static double CLASS4_TRANSMISSION_TIME = 10;
	static int NUMBER_OF_APPS = 0;
	static int NUMBER_OF_CLASS1 = 0;
	static int NUMBER_OF_CLASS2 = 0;
	static int NUMBER_OF_CLASS3 = 0;
	static int NUMBER_OF_CLASS4 = 0;
	
	static String ratio = "";
	
	public static void main(String[] args) {
		Log.printLine("Starting multi class Applications...");

		if(args.length != 0) {
			openConfigFile(args[0]);
			openExecutionMapFile(args[1]);
		}
		startSimulation();
		System.exit(0);
	}
	public static int randomVarible(List<Integer> list) {
	    Random rand = new Random();
	    return list.get(rand.nextInt(list.size()));
	}
	private static void startSimulation() {
		Log.printLine("CREATE APPLICATIONS");
		try {
			int num_user = 1;
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;
			
			List<Integer> device_idx = new ArrayList() {};
			for(int x = 0; x < numOfSensorNode; x++) {
				device_idx.add(x);
			}
			List<Integer> random_idx = new ArrayList() {};
			for(int x = 0; x < NUMBER_OF_APPS; x++) {
				random_idx.add(randomVarible(device_idx));
			}
			Log.printLine(random_idx);
			// c1,c2,c3,c4 5 5 5 5
			// device lists 에서 랜덤으로 5개 뽑기			
			// TODO: fix by file input 
			int[] test_map[] = {{0,1,2,3},{0,1,2,3},{0,1,2,3},{0,1,2,3}};
			List<List<Integer>> test_map2 = new ArrayList<List<Integer>>();
			for(int x=0;x < 4;x++) {
				
			}
			
			HashMap<String, int[]> class_map = new HashMap<String, int[]>();

			for(int x=0;x < 4;x++) {
				class_map.put(appIds[x],test_map[x]);
			}
//			Log.disable();
			//init
			CloudSim.init( num_user, calendar, trace_flag);
			
			//make applications( class1, class2, class3, class4 )
			Application[] applications = createApplications(); // TODO: now fix 4,4,4,4 
			
			
			ModuleMapping module_mapping = createFogDevices(num_user, appIds, class_map);
			Controller controller = new Controller("master-controller", fogDevices, sensors, actuators,using_fresult);
			
			if(using_fresult == 1) {
				controller.setResult_fpath((String)configs[0] + "/" + (String)configs[1] + "_"+ String.valueOf((Integer)configs[2]) + "_" + java.time.LocalDateTime.now()+".csv");
			}
			for(Application app : applications) {
				controller.submitApplication(app, 0, new ModulePlacementMapping(fogDevices, app, module_mapping));				
			}
			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

			CloudSim.startSimulation();			

		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}
	public static List<String> readFile(String filename)
	{
	  List<String> records = new ArrayList<String>();
	  try
	  {
	    BufferedReader reader = new BufferedReader(new FileReader(filename));
	    String line;
	    while ((line = reader.readLine()) != null)
	    {
	      records.add(line);
	    }
	    reader.close();
	    return records;
	  }
	  catch (Exception e)
	  {
	    System.err.format("Exception occurred trying to read '%s'.", filename);
	    e.printStackTrace();
	    return null;
	  }
	}
	private static Object[] appendValue(Object[] obj, Object newObj) {
		ArrayList<Object> temp = new ArrayList<Object>(Arrays.asList(obj));
		temp.add(newObj);
		return temp.toArray();

	}
	public static void openExecutionMapFile(String filepath) {
		List<String> t = readFile(filepath);
		for(String line : t)
		{
//			System.out.println(line);
			// line -> 1,2,3,4,5,6,7,8
		}
	}
	// open config file for multiple simulation or configuable simulation
	public static void openConfigFile(String filepath) {
		List<String> t = readFile(filepath);
		
		Log.printLine("start to read config file");
		// output path
		String[] origin_data = t.get(0).split("=");
		String value = origin_data[origin_data.length-1];
		configs = appendValue(configs,value);
		
		// app name
		origin_data = t.get(1).split("=");
		value = origin_data[origin_data.length-1];
		configs = appendValue(configs,value);
		
		// number of edges
		origin_data = t.get(2).split("=");
		value = origin_data[origin_data.length-1];
		configs = appendValue(configs,Integer.valueOf(value));
		numOfSensorNode = Integer.valueOf(value);
		
		// using file result
		origin_data = t.get(3).split("=");
		value = origin_data[origin_data.length-1];
		configs = appendValue(configs,Integer.valueOf(value));
		
		using_fresult = (int)configs[3];
		
		// number of apps
		origin_data = t.get(4).split("=");
		value = origin_data[origin_data.length-1];
		configs = appendValue(configs,Integer.valueOf(value));
		NUMBER_OF_APPS = (int)configs[4];
		
		// ratio
		origin_data = t.get(5).split("=");
		value = origin_data[origin_data.length-1];
		ratio = String.valueOf(value);
		//System.out.println(ratio);
		setNumberOfApps(ratio);
	}
	private static void setNumberOfApps(String ratio) {
		String[] ratios = ratio.split(",");
		Float total = 0.0f;
		Float mul = 0.0f;
		
		for(String i : ratios) total += Integer.valueOf(i);
		mul = NUMBER_OF_APPS / total;
		NUMBER_OF_CLASS1 = Integer.valueOf(ratios[0])*mul.intValue();
		NUMBER_OF_CLASS2 = Integer.valueOf(ratios[1])*mul.intValue();
		NUMBER_OF_CLASS3 = Integer.valueOf(ratios[2])*mul.intValue();
		NUMBER_OF_CLASS4 = Integer.valueOf(ratios[3])*mul.intValue();
		
	}
	private static Application[] createApplications() {
		Application lists[] = new Application[] {null,null,null,null};
		lists[0] = makeClass("class1",4);
		lists[1] = makeClass("class2",4);
		lists[2] = makeClass("class3",4);
		lists[3] = makeClass("class4",4);
		return lists;
	}
	private static Application makeClass(String appId, int num_of_class1) {
		Log.printLine("make application "+appId+" * "+String.valueOf(num_of_class1));
		// broker do not anything now so set 1
		Application application = Application.createApplication(appId, 1);
		
		
		for(int i=0; i < num_of_class1; i++) {
			Log.printLine("make app module client : "+appId+"-"+String.valueOf(i));
			// TODO: have to check which occupied ram size
			application.addAppModule(appId+"-"+String.valueOf(i), 100);
		}
		Log.printLine("make app module fog, cloud: "+appId+"_fog ,"+appId+"_cloud");
		application.addAppModule(appId+"_fog", 100);
		application.addAppModule(appId+"_cloud", 100);
		
		for(int i=0; i < num_of_class1; i++) {
			Log.printLine("make app edge sensor->edge, edge->fog: "+"CAM_"+appId+"-"+appId+"-"+String.valueOf(i)+
					","+appId+"-"+String.valueOf(i)+"-"+appId+"_fog");
			application.addAppEdge("CAM_"+appId, appId+"-"+String.valueOf(i), 10, 500, "CAM_"+appId, Tuple.UP, AppEdge.SENSOR);
			application.addAppEdge(appId+"-"+String.valueOf(i), appId+"_fog", 20, 500, "DATA"+"-"+String.valueOf(i), Tuple.UP, AppEdge.MODULE);
		}

		application.addAppEdge(appId+"_fog", appId+"_cloud", 30, 200, "USERS_STATE", Tuple.UP, AppEdge.MODULE);

		for(int i=0; i< numOfSensorNode; i++) {		
			Log.printLine("make app edge fog->edge, edge->act: "+appId+"_fog"+"-"+appId+"-"+String.valueOf(i)+
					","+appId+"-"+String.valueOf(i)+"-"+appId+"_ACT");
			if(appId.equals("class1")) {
				application.addAppEdge(appId+"_fog", appId+"-"+String.valueOf(i), 5000, 500, "RESULT"+"-"+String.valueOf(i), Tuple.DOWN, AppEdge.MODULE);				
			}
			else if(appId.equals("class2")) {
				application.addAppEdge(appId+"_fog", appId+"-"+String.valueOf(i), 100, 500, "RESULT"+"-"+String.valueOf(i), Tuple.DOWN, AppEdge.MODULE);				
			}
			else if(appId.equals("class3")) {
				application.addAppEdge(appId+"_fog", appId+"-"+String.valueOf(i), 2200, 500, "RESULT"+"-"+String.valueOf(i), Tuple.DOWN, AppEdge.MODULE);				
			}
			else {
				application.addAppEdge(appId+"_fog", appId+"-"+String.valueOf(i), 30, 500, "RESULT"+"-"+String.valueOf(i), Tuple.DOWN, AppEdge.MODULE);
			}
			application.addAppEdge(appId+"-"+String.valueOf(i), appId+"_ACT", 50, 20, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);
		}
		
		for(int i=0; i< numOfSensorNode; i++) {		
			Log.printLine("make tuple mapping edge["+appId+"-"+String.valueOf(i)+"] : "+ "CAM_"+appId+"-"+
					"DATA"+"-"+String.valueOf(i));
			application.addTupleMapping(appId+"-"+String.valueOf(i), "CAM_"+appId,
					"DATA"+"-"+String.valueOf(i), new FractionalSelectivity(0.9)); 
			Log.printLine("make tuple mapping fog["+appId+"_fog"+"] : "+ "DATA"+"-"+String.valueOf(i) +"-"+
					"RESULT"+"-"+String.valueOf(i));
			application.addTupleMapping(appId+"_fog", "DATA"+"-"+String.valueOf(i), "RESULT"+"-"+String.valueOf(i), new FractionalSelectivity(1.0));
			Log.printLine("make tuple mapping fog["+appId+"_fog"+"] : "+ "DATA"+"-"+String.valueOf(i) +"-"+
					"USERS_STATE");
			application.addTupleMapping(appId+"_fog", "DATA"+"-"+String.valueOf(i), "USERS_STATE", new FractionalSelectivity(1.0));
		}
				
		for(int i=0; i< numOfSensorNode; i++) {		
			Log.printLine("make tuple mapping edge["+appId+"-"+String.valueOf(i)+"] : "+ "RESULT"+"-"+String.valueOf(i) +"-"+
					"SELF_STATE_UPDATE");
			application.addTupleMapping(appId+"-"+String.valueOf(i), "RESULT"+"-"+String.valueOf(i), "SELF_STATE_UPDATE", new FractionalSelectivity(1.0)); 
		}
		application.addTupleMapping(appId+"_cloud", "USERS_STATE", "CLOUD_RESULT", new FractionalSelectivity(1.0));

		for(int i=0; i< numOfSensorNode; i++) {
			String cli = new String(appId+"-"+String.valueOf(i));
			String sensor = new String("CAM_"+appId+"-"+String.valueOf(i));
			loops.add(new AppLoop(new ArrayList<String>(){{add(sensor);add(cli);add(appId+"_fog");add(cli);add(appId+"_ACT");}}));
		}
		application.setLoops(loops);
		application.setUserId(0);
		
		return application;
	}


	private static ModuleMapping createFogDevices(int userId, String[] appIds,HashMap<String, int[]> class_map) {

		FogDevice cloud = createFogDevice("cloud", 17675, 32000, 10000, 10000, 0, 0.01, 100, 50); // creates the fog device Cloud at the apex of the hierarchy with level=0
		cloud.setParentId(-1);		
		fogDevices.add(cloud);
		
		//TODO: now number of fog-layer devices fixed at 1
		FogDevice fog = createFogDevice("fog-layer", 2000, 8000, 1000, 1000, 1, 0.0, 8, 3.5); // creates the fog device Proxy Server (level=1)
		fog.setParentId(cloud.getId()); // setting Cloud as parent of the Proxy Server
		fog.setUplinkLatency(1000); // latency of connection between gateways and cloud is 100 ms
		fogDevices.add(fog);

		for(int i=0;i<numOfSensorNode;i++){
			String sensorNodeId = 0+"-"+i;
			FogDevice sensorNode = createFogDevice("m-"+sensorNodeId, 65, 1000, 1000, 1000, 2, 0, 2, 3.7);
			sensorNode.setParentId(fog.getId());
			sensorNode.setUplinkLatency(10); // latency of connection between the smartphone and proxy server is 10 ms
			fogDevices.add(sensorNode);
		}
		
		for(String appId : appIds) {
			for(int idx_of_sensor_device : class_map.get(appId)) {
				Sensor sensor = null;
				if(appId.equals("class1")) {
					sensor = new Sensor("s-0-"+String.valueOf(idx_of_sensor_device),"CAM_"+appId, userId, appId, new DeterministicDistribution(CLASS1_TRANSMISSION_TIME));				
				}
				else if(appId.equals("class2")) {
					sensor = new Sensor("s-0-"+String.valueOf(idx_of_sensor_device),"CAM_"+appId, userId, appId, new DeterministicDistribution(CLASS2_TRANSMISSION_TIME));				
				}
				else if(appId.equals("class3")) {
					sensor = new Sensor("s-0-"+String.valueOf(idx_of_sensor_device),"CAM_"+appId, userId, appId, new DeterministicDistribution(CLASS3_TRANSMISSION_TIME));				
				}
				else {
					sensor = new Sensor("s-0-"+String.valueOf(idx_of_sensor_device),"CAM_"+appId, userId, appId, new DeterministicDistribution(CLASS4_TRANSMISSION_TIME));				
				}
				sensors.add(sensor);
				Actuator noti = new Actuator(appId+"_act-"+String.valueOf(idx_of_sensor_device),userId,appId,appId+"_ACT");
				actuators.add(noti);
				Log.printLine(fogDevices.get(2+idx_of_sensor_device).getName());
				Log.printLine(sensor.getName());
				Log.printLine(noti.getName());
				sensor.setGatewayDeviceId(fogDevices.get(2+idx_of_sensor_device).getId());
				//TODO : randomize trigger
				sensor.setLatency(100.0);
				noti.setGatewayDeviceId(fogDevices.get(2+idx_of_sensor_device).getId());
				noti.setLatency(5.0);
			}
		}
		ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping
		for(String appId : appIds) {
			moduleMapping.addModuleToDevice(appId+"_cloud", "cloud"); // fixing all instances of the Connector module to the Cloud			
			moduleMapping.addModuleToDevice(appId+"_fog", "fog-layer"); // fixing all instances of the Concentration Calculator module to the Cloud

		}
		for(FogDevice device : fogDevices){
			if(device.getName().startsWith("m")){
				String[] temps = device.getName().split("-");
				String last = temps[temps.length-1];
//				Log.formatLine("device name : %s , app name : %s", device.getName(),"client"+"-"+Integer.valueOf(last));
				for(String appId : appIds) {
					for(int idx_of_sensor_device : class_map.get(appId)) {
						if(idx_of_sensor_device == Integer.valueOf(last))
							moduleMapping.addModuleToDevice(appId+"-"+Integer.valueOf(last), device.getName());
					}
				}


			}
		}
		return moduleMapping;
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
}