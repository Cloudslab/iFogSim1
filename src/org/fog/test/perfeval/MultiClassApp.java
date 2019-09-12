package org.fog.test.perfeval;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
import org.fog.scheduler.TupleScheduler;
import org.fog.utils.Config;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

import com.google.common.primitives.Ints;

/**
 * Simulation setup for Multi class Applications on each device
 * @author DongJoo Seo
 * based on VRGameFog example
 */

class ClassInfo {
	static HashMap<String, ArrayList<Integer>> map_of_each_class = null;
	//[string:[1,2],...]
	static String[] name_of_classes = {"class1", "class2", "class3", "class4"};
	static ArrayList<Integer> number_of_each_class = null;
	//[number_of_c1,...]
	static ArrayList<Integer> device_idx = null;
	// just idx
	static List<Integer> random_idx = null;
	public static ArrayList<Integer> getDevice_idx() {
		return device_idx;
	}
	public static void setDevice_idx(ArrayList<Integer> device_idx) {
		ClassInfo.device_idx = device_idx;
	}
	public static HashMap<String, ArrayList<Integer>> getMap_of_each_class() {
		return map_of_each_class;
	}
	public static void setMap_of_each_class(HashMap<String, ArrayList<Integer>> map_of_each_class) {
		ClassInfo.map_of_each_class = map_of_each_class;
	}
	public static String[] getName_of_classes() {
		return name_of_classes;
	}
	public static void setName_of_classes(String[] name_of_classes) {
		ClassInfo.name_of_classes = name_of_classes;
	}
	public static ArrayList<Integer> getNumber_of_each_class() {
		return number_of_each_class;
	}
	public static void setNumber_of_each_class(ArrayList<Integer> number_of_each_class) {
		ClassInfo.number_of_each_class = number_of_each_class;
	}
	public static void setVariables() {
		setNumber_of_each_class(new ArrayList<Integer>());
		setDevice_idx(new ArrayList<Integer>());
		setMap_of_each_class(new HashMap<String, ArrayList<Integer>>());
		List<Integer> device_idx = new ArrayList() {};
		for(int x = 0; x < MultiClassApp.numOfSensorNode; x++) {
			device_idx.add(x);
		}
		random_idx = new ArrayList<Integer>() {};
		for(int x = 0; x < MultiClassApp.NUMBER_OF_APPS; x++) {
			random_idx.add(MultiClassApp.randomVarible(device_idx));
		}
		List<Integer> tmp = new ArrayList<Integer>() {};
		
		List<Integer> class1_map = new ArrayList(){};
		for(int x = 0; x < MultiClassApp.NUMBER_OF_CLASS1; x++) {
			class1_map.add(random_idx.get(0));
			tmp.add(random_idx.get(0));
			random_idx.remove(0);
		}
		List<Integer> class2_map = new ArrayList(){};
		for(int x = 0; x < MultiClassApp.NUMBER_OF_CLASS2; x++) {
			class2_map.add(random_idx.get(0));
			tmp.add(random_idx.get(0));
			random_idx.remove(0);
		}
		List<Integer> class3_map = new ArrayList(){};
		for(int x = 0; x < MultiClassApp.NUMBER_OF_CLASS3; x++) {
			class3_map.add(random_idx.get(0));
			tmp.add(random_idx.get(0));
			random_idx.remove(0);
		}
		List<Integer> class4_map = new ArrayList(){};
		for(int x = 0; x < MultiClassApp.NUMBER_OF_CLASS4; x++) {
			class4_map.add(random_idx.get(0));
			tmp.add(random_idx.get(0));
			random_idx.remove(0);
		}
		random_idx = tmp;
		int[] test_map[] = {Ints.toArray(class1_map),Ints.toArray(class2_map),Ints.toArray(class3_map),Ints.toArray(class4_map)};			
		for(int x=0;x < 4;x++) {
			map_of_each_class.put(MultiClassApp.appIds[x],(ArrayList<Integer>) Arrays.stream(test_map[x]).boxed().collect(Collectors.toList()));
		}

		for(int i=0; i< MultiClassApp.numOfSensorNode;i++) number_of_each_class.add(0);
		
		for (String app : MultiClassApp.appIds) {
			Log.printLine(map_of_each_class.get(app));
		}
		
		for(int[] each : test_map) {
			for(int idx : each) {
				Integer a = number_of_each_class.get(idx)+1;
				number_of_each_class.remove(idx);
				number_of_each_class.add(idx, a);
			}
		}
		Log.printLine(number_of_each_class);

	}
}
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
	static double CLASS1_TRANSMISSION_TIME = 0;
	static double CLASS2_TRANSMISSION_TIME = 0;
	static double CLASS3_TRANSMISSION_TIME = 0;
	static double CLASS4_TRANSMISSION_TIME = 0;
	static int NUMBER_OF_APPS = 0;
	static int NUMBER_OF_CLASS1 = 0;
	static int NUMBER_OF_CLASS2 = 0;
	static int NUMBER_OF_CLASS3 = 0;
	static int NUMBER_OF_CLASS4 = 0;
	static int ENABLE_LOG = 0;
	static double SENSOR_TO_EDGE_LATENCY = 50;
	static double EDGE_TO_FOG_LATENCY = 100;
	static double FOG_TO_CLOUD_LATENCY = 1000;
	static String ratio = "";
	static int OFFLOADING_POLICY = -1;
	static int CLASS_NUM = -1;
	static int SINGLE_APP = 0;
	static int CLASS1_MIPS = 48000;
	static int CLASS2_MIPS = 4835;
	static int CLASS3_MIPS = 60045;
	static int CLASS4_MIPS = 3062;
	static int CLOUD_MIPS = 40000;
	static int FOG_MIPS = 5900;
	static int EDGE_MIPS = 300;
	
	public static void main(String[] args) {
		Log.printLine("Starting multi class Applications...");
		if(args.length != 0) {
			openConfigFile(args[0]);
			openExecutionMapFile(args[1]);
		}
		if(ENABLE_LOG == 0)
			Log.disable();
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
			ClassInfo class_information = new ClassInfo();
			class_information.setVariables();

			CloudSim.init( num_user, calendar, trace_flag);
			Application app = null;
			if(SINGLE_APP != 0) {
				app = createApplication(CLASS_NUM,OFFLOADING_POLICY,class_information);
			}else {
				app = createApplications(OFFLOADING_POLICY, class_information);
			}
			
			ModuleMapping module_mapping = createDevicesAndMapping(num_user, appIds,0, class_information);
			
			Controller controller = new Controller("master-controller", fogDevices, sensors, actuators,using_fresult);
			
			if(using_fresult == 1) {
				controller.setResult_fpath((String)configs[0] + "/" + (String)configs[1] + "_"+ String.valueOf((Integer)configs[2]) + "_" + java.time.LocalDateTime.now()+".csv");
			}
			
			controller.submitApplication(app, 0, new ModulePlacementMapping(fogDevices, app, module_mapping));	
			
			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
			//
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

		origin_data = t.get(6).split("=");
		value = origin_data[origin_data.length-1];
		configs = appendValue(configs,Integer.valueOf(value));
		ENABLE_LOG = Integer.valueOf(value);

		origin_data = t.get(7).split("=");
		value = origin_data[origin_data.length-1];
		configs = appendValue(configs,Double.valueOf(value));
		CLASS1_TRANSMISSION_TIME = Double.valueOf(value);

		origin_data = t.get(8).split("=");
		value = origin_data[origin_data.length-1];
		configs = appendValue(configs,Double.valueOf(value));
		CLASS2_TRANSMISSION_TIME = Double.valueOf(value);

		origin_data = t.get(9).split("=");
		value = origin_data[origin_data.length-1];
		configs = appendValue(configs,Double.valueOf(value));
		CLASS3_TRANSMISSION_TIME = Double.valueOf(value);
		
		origin_data = t.get(10).split("=");
		value = origin_data[origin_data.length-1];
		configs = appendValue(configs,Double.valueOf(value));
		CLASS4_TRANSMISSION_TIME = Double.valueOf(value);
		
		origin_data = t.get(11).split("=");
		value = origin_data[origin_data.length-1];
		configs = appendValue(configs,Double.valueOf(value));
		SENSOR_TO_EDGE_LATENCY = Double.valueOf(value);

		origin_data = t.get(12).split("=");
		value = origin_data[origin_data.length-1];
		configs = appendValue(configs,Double.valueOf(value));
		EDGE_TO_FOG_LATENCY = Double.valueOf(value);
		
		origin_data = t.get(13).split("=");
		value = origin_data[origin_data.length-1];
		configs = appendValue(configs,Double.valueOf(value));
		FOG_TO_CLOUD_LATENCY = Double.valueOf(value);

		origin_data = t.get(14).split("=");
		value = origin_data[origin_data.length-1];
		configs = appendValue(configs,Integer.valueOf(value));
		OFFLOADING_POLICY = Integer.valueOf(value);
		
		origin_data = t.get(15).split("=");
		value = origin_data[origin_data.length-1];
		configs = appendValue(configs,Integer.valueOf(value));
		CLASS_NUM = Integer.valueOf(value);
		
		origin_data = t.get(16).split("=");
		value = origin_data[origin_data.length-1];
		configs = appendValue(configs,Integer.valueOf(value));
		SINGLE_APP = Integer.valueOf(value);

		
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
	private static Application createApplications(int offloading_policy, ClassInfo info) {
		// 0 -> running on the edge
		// 1 -> on the fog
		// 2 -> on the cloud
		// 3 -> on the random place			
		Application app = makeClasses("multi_app", 0, info);		
		return app;
	}
	private static Application createApplication(int number_of_class, int offloading_policy, ClassInfo info) {
		Application app = Application.createApplication("multi_app", 1);
		
		// 2. make 4 classes client, fog, cloud appmodule

		for(int i=0; i < NUMBER_OF_APPS; i++) {
			app.addAppModule("class"+number_of_class+"-"+String.valueOf(i), 2);
			printOneStringLog("Make appModule","name","class"+number_of_class+"-"+String.valueOf(i)+"-"+String.valueOf(i));			
		}
		app.addAppModule("class"+number_of_class+"_fog", 15);
		app.addAppModule("class"+number_of_class+"_cloud", 20);
		
		printOneStringLog("Make appModule","name","class"+number_of_class+"_fog");
		printOneStringLog("Make appModule","name","class"+number_of_class+"_cloud");
		String appName = "class"+number_of_class;
		String fog = appName+"_fog";
		String cloud = appName+"_cloud";		
		// 3. make appEdges;
		switch(offloading_policy) {
		case 0:
				for(int i=0; i < NUMBER_OF_APPS; i++) {
					String sensor = "CAM-"+String.valueOf(i);
					String edge = appName+"-"+String.valueOf(i);
					String cam_data ="CAM_CLASS"+number_of_class+"-"+String.valueOf(i);
					String data ="DATA"+number_of_class+"-"+String.valueOf(i);
					
					
					if(appName.equals("class1")) {
						app.addAppEdge(cam_data, edge, CLASS1_MIPS, 50, cam_data, Tuple.UP, AppEdge.SENSOR);
					}
					else if(appName.equals("class2")) {
						app.addAppEdge(cam_data, edge, CLASS2_MIPS, 50, cam_data, Tuple.UP, AppEdge.SENSOR);
					}
					else if(appName.equals("class3")) {
						app.addAppEdge(cam_data, edge, CLASS3_MIPS, 50, cam_data, Tuple.UP, AppEdge.SENSOR);
					}
					else {
						app.addAppEdge(cam_data, edge, CLASS4_MIPS, 50, cam_data, Tuple.UP, AppEdge.SENSOR);
					}					

					app.addAppEdge(edge, fog, 50, 50, data, Tuple.UP, AppEdge.MODULE);				
					
					// make fog -> edge
					String result = "RESULT"+number_of_class+"-"+String.valueOf(i);
					app.addAppEdge(fog, edge, 50, 50, result, Tuple.DOWN, AppEdge.MODULE);
					
					// make edge -> act
					String act = "ACT"+number_of_class+"-"+String.valueOf(i);
					app.addAppEdge(edge,act, 50, 20,"SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);
					
				}
				// make fog -> cloud
				app.addAppEdge(fog,cloud, 50, 20,"USERS_STATE", Tuple.UP, AppEdge.MODULE);
			break;
		case 1:
			for(int i=0; i < NUMBER_OF_APPS; i++) {
				String sensor = "CAM-"+String.valueOf(i);
				String edge = appName+"-"+String.valueOf(i);
				String cam_data ="CAM_CLASS"+number_of_class+"-"+String.valueOf(i);

				app.addAppEdge(cam_data, edge, 50, 50, cam_data, Tuple.UP, AppEdge.SENSOR); //edge computing
				
				// make edge -> fog				
				String data ="DATA"+number_of_class+"-"+String.valueOf(i);
				if(appName.equals("class1")) {
					app.addAppEdge(edge, fog, CLASS1_MIPS, 50, data, Tuple.UP, AppEdge.MODULE);
				}
				else if(appName.equals("class2")) {
					app.addAppEdge(edge, fog, CLASS2_MIPS, 50, data, Tuple.UP, AppEdge.MODULE);
				}
				else if(appName.equals("class3")) {
					app.addAppEdge(edge, fog, CLASS3_MIPS, 50, data, Tuple.UP, AppEdge.MODULE);
				}
				else {
					app.addAppEdge(edge, fog, CLASS4_MIPS, 50, data, Tuple.UP, AppEdge.MODULE);
				} // fog computing 

				// make fog -> edge
				String result = "RESULT"+number_of_class+"-"+String.valueOf(i);
				app.addAppEdge(fog, edge, 50, 50, result, Tuple.DOWN, AppEdge.MODULE);
				// make edge -> act
				String act = "ACT"+number_of_class+"-"+String.valueOf(i);
				app.addAppEdge(edge,act, 50, 20,"SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);
				
			}
			// make fog -> cloud
			app.addAppEdge(fog,cloud, 50, 20,"USERS_STATE", Tuple.UP, AppEdge.MODULE);			
			break;
		case 2:
			for(int i=0; i < NUMBER_OF_APPS; i++) {
				String sensor = "CAM-"+String.valueOf(i);
				String edge = appName+"-"+String.valueOf(i);
				String cam_data ="CAM_CLASS"+number_of_class+"-"+String.valueOf(i);

				app.addAppEdge(cam_data, edge, 50, 50, cam_data, Tuple.UP, AppEdge.SENSOR); //edge computing
				
				// make edge -> fog				
				String data ="DATA"+number_of_class+"-"+String.valueOf(i);
				app.addAppEdge(edge, fog, 50, 50, data, Tuple.UP, AppEdge.MODULE);
				
				// make fog -> cloud
				String cinput = "CINPUT"+number_of_class+"-"+String.valueOf(i);
				String result = "RESULT"+number_of_class+"-"+String.valueOf(i);				
				String cresult = "CRESULT"+number_of_class+"-"+String.valueOf(i);

				if(appName.equals("class1")) {
					app.addAppEdge(fog, cloud, CLASS1_MIPS, 50, cinput, Tuple.UP, AppEdge.MODULE);
				}
				else if(appName.equals("class2")) {
					app.addAppEdge(fog, cloud, CLASS2_MIPS, 50, cinput, Tuple.UP, AppEdge.MODULE);
				}
				else if(appName.equals("class3")) {
					app.addAppEdge(fog, cloud, CLASS3_MIPS, 50, cinput, Tuple.UP, AppEdge.MODULE);
				}
				else {
					app.addAppEdge(fog, cloud, CLASS4_MIPS, 50, cinput, Tuple.UP, AppEdge.MODULE);
				}  
				// cloud -> fog
				app.addAppEdge(cloud, fog, 50, 50, cresult, Tuple.DOWN, AppEdge.MODULE);
				// make fog -> edge
				app.addAppEdge(fog, edge, 50, 50, result, Tuple.DOWN, AppEdge.MODULE);				
				// make edge -> act
				String act = "ACT"+number_of_class+"-"+String.valueOf(i);
				app.addAppEdge(edge,act, 50, 20,"SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);
				
			}
			break;		
		}
		Log.printLine("---------------------------------------");
		Log.printLine("---------------------------------------");
		
		// 4. make tuple mapping
		for(int i=0; i < NUMBER_OF_APPS; i++) {
			String sensor = "CAM-"+String.valueOf(i);
			String edge = appName+"-"+String.valueOf(i);
			String data ="DATA"+number_of_class+"-"+String.valueOf(i);
			String result = "RESULT"+number_of_class+"-"+String.valueOf(i);
			String act = "ACT"+number_of_class+"-"+String.valueOf(i);
			String cam_data ="CAM_CLASS"+number_of_class+"-"+String.valueOf(i);
			String cinput = "CINPUT"+number_of_class+"-"+String.valueOf(i);
			String cresult = "CRESULT"+number_of_class+"-"+String.valueOf(i);
			// module name, input, output
			// mapping (edge) cam_data -> data
			printTwoStringLog("Make tuple mapping module:"+edge,"input",cam_data,"right",data);
			app.addTupleMapping(edge, cam_data, data, new FractionalSelectivity(1.0));

			if(offloading_policy == 2) {
				// mapping (fog) data -> cinput
				app.addTupleMapping(fog, data, cinput, new FractionalSelectivity(1.0));
				app.addTupleMapping(fog, cresult, result, new FractionalSelectivity(1.0));
				app.addTupleMapping(cloud, cinput, cresult, new FractionalSelectivity(1.0));
				app.addTupleMapping(edge, result, "SELF_STATE_UPDATE", new FractionalSelectivity(1.0));
				loops.add(new AppLoop(new ArrayList<String>(){{add(cam_data);add(edge);add(fog);add(cloud);add(fog);add(edge);add(act);}}));
			}else {
				// mapping (fog) data -> result
				printTwoStringLog("Make tuple mapping module:"+fog,"input",data,"right",result);
				app.addTupleMapping(fog, data, result, new FractionalSelectivity(1.0));
				// mapping (fog) data -> USERS_STATE
				printTwoStringLog("Make tuple mapping module:"+fog,"input",data,"right","USERS_STATE");
				app.addTupleMapping(fog, data, "USERS_STATE", new FractionalSelectivity(1.0));				
				printTwoStringLog("Make tuple mapping module:"+edge,"input",result,"right","SELF_STATE_UPDATE");
				app.addTupleMapping(edge, result, "SELF_STATE_UPDATE", new FractionalSelectivity(1.0));
				loops.add(new AppLoop(new ArrayList<String>(){{add(cam_data);add(edge);add(fog);add(edge);add(act);}}));
			}			
			
			// mapping (edge) result -> SELF_STATE_UPDATE
		}
		// mapping (cloud) USERS_STATE -> CLOUD_RESULT
		if(offloading_policy != 2)
			app.addTupleMapping(cloud, "USERS_STATE", "CLOUD_RESULT", new FractionalSelectivity(1.0));
		app.setLoops(loops);
		app.setUserId(1);
		return app;
	}
	
	private static Application makeClasses(String appId, int offloading_policy, ClassInfo info) {
		// 1. make empty app
		Application app = Application.createApplication(appId, 1);
		
		// 2. make 4 classes client, fog, cloud appmodule
		for(int x=0;x<4;x++) {
			for(int i=0; i < info.number_of_each_class.get(x); i++) {
				app.addAppModule("class"+String.valueOf(x+1)+"-"+String.valueOf(i), 2);
			
				printOneStringLog("Make appModule","name","class"+String.valueOf(x+1)+"-"+String.valueOf(i));			
			}
			app.addAppModule("class"+String.valueOf(x+1)+"_fog", 15);
			app.addAppModule("class"+String.valueOf(x+1)+"_cloud", 20);
			
			printOneStringLog("Make appModule","name","class"+String.valueOf(x+1)+"_fog");
			printOneStringLog("Make appModule","name","class"+String.valueOf(x+1)+"_cloud");
		}
		
		// 3. make appEdges
		switch(offloading_policy) {
		case 0:
			for(int x=0;x<4;x++) {
				String appName = "class"+String.valueOf(x+1);
				String fog = appName+"_fog";
				String cloud = appName+"_cloud";
				for(int i=0; i < info.number_of_each_class.get(x); i++) {
					String sensor = "CAM"+String.valueOf(x+1)+"-"+String.valueOf(i);
					String edge = appName+"-"+String.valueOf(i);
					String cam_data ="CAM_CLASS"+String.valueOf(x+1)+"-"+String.valueOf(i);
					// make sensor -> edge
					printTwoStringLog("Make edge(sensor->edge)","left",sensor,"right",edge);
					printOneStringLog("Tuple type","name", cam_data);
					app.addAppEdge(cam_data, edge, 10, 50, cam_data, Tuple.UP, AppEdge.SENSOR);
					
					// make edge -> fog
					printTwoStringLog("Make edge(edge->fog)","left",edge,"right",fog);
					printOneStringLog("Tuple type","name","DATA"+String.valueOf(x+1)+"-"+String.valueOf(i));
					String data ="DATA"+String.valueOf(x+1)+"-"+String.valueOf(i);
					if(appName.equals("class1")) {
						app.addAppEdge(edge, fog, 77145, 50, data, Tuple.UP, AppEdge.MODULE);				
					}
					else if(appName.equals("class2")) {
						app.addAppEdge(edge, fog, 4835, 50, data, Tuple.UP, AppEdge.MODULE);				
					}
					else if(appName.equals("class3")) {
						app.addAppEdge(edge, fog, 150472, 50, data, Tuple.UP, AppEdge.MODULE);				
					}
					else {
						app.addAppEdge(edge, fog, 3062, 50, data, Tuple.UP, AppEdge.MODULE);				
					}
					
					// make fog -> edge
					String result = "RESULT"+String.valueOf(x+1)+"-"+String.valueOf(i);
					printTwoStringLog("Make edge(fog->edge)","left",fog,"right",edge);
					printOneStringLog("Tuple type","name",result);					
					app.addAppEdge(fog, edge, 10, 50, result, Tuple.DOWN, AppEdge.MODULE);
					
					// make edge -> act
					String act = "ACT"+String.valueOf(x+1)+"-"+String.valueOf(i);
					printTwoStringLog("Make edge(edge->act)","left",sensor,"right",act);
					printOneStringLog("Tuple type","name","SELF_STATE_UPDATE");					
					app.addAppEdge(edge,act, 50, 20,"SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);
					
				}
				// make fog -> cloud
				printTwoStringLog("Make edge(fog->cloud)","left",fog,"right",cloud);
				printOneStringLog("Tuple type","name","USERS_STATE"+String.valueOf(x+1));					
				app.addAppEdge(fog,cloud, 50, 20,"USERS_STATE", Tuple.UP, AppEdge.MODULE);
			}
			break;
		case 1:
			for(int x=0;x<4;x++) {
				String appName = "class"+String.valueOf(x+1);
				String fog = appName+"_fog";
				String cloud = appName+"_cloud";
				for(int i=0; i < info.number_of_each_class.get(x); i++) {
					String sensor = "CAM"+String.valueOf(x+1)+"-"+String.valueOf(i);
					String edge = appName+"-"+String.valueOf(i);
					String cam_data ="CAM_CLASS"+String.valueOf(x+1)+"-"+String.valueOf(i);
					// make sensor -> edge
					printTwoStringLog("Make edge(sensor->edge)","left",sensor,"right",edge);
					printOneStringLog("Tuple type","name", cam_data);
					app.addAppEdge(cam_data, edge, 10, 50, cam_data, Tuple.UP, AppEdge.SENSOR);
					
					// make edge -> fog
					printTwoStringLog("Make edge(edge->fog)","left",edge,"right",fog);
					printOneStringLog("Tuple type","name","DATA"+String.valueOf(x+1)+"-"+String.valueOf(i));
					String data ="DATA"+String.valueOf(x+1)+"-"+String.valueOf(i);
					app.addAppEdge(edge, fog, 10, 50, data, Tuple.UP, AppEdge.MODULE);					
					// make fog -> edge
					String result = "RESULT"+String.valueOf(x+1)+"-"+String.valueOf(i);
					printTwoStringLog("Make edge(fog->edge)","left",fog,"right",edge);
					printOneStringLog("Tuple type","name",result);					
					if(appName.equals("class1")) {
						app.addAppEdge(fog, edge, 5000, 50, result, Tuple.DOWN, AppEdge.MODULE);
					}
					else if(appName.equals("class2")) {
						app.addAppEdge(fog, edge, 100, 50, result, Tuple.DOWN, AppEdge.MODULE);
					}
					else if(appName.equals("class3")) {
						app.addAppEdge(fog, edge, 2200, 50, result, Tuple.DOWN, AppEdge.MODULE);
					}
					else {
						app.addAppEdge(fog, edge, 30, 50, result, Tuple.DOWN, AppEdge.MODULE);
					}

					
					// make edge -> act
					String act = "ACT"+String.valueOf(x+1)+"-"+String.valueOf(i);
					printTwoStringLog("Make edge(edge->act)","left",sensor,"right",act);
					printOneStringLog("Tuple type","name","SELF_STATE_UPDATE");					
					app.addAppEdge(edge,act, 50, 20,"SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);
					
				}
				// make fog -> cloud
				printTwoStringLog("Make edge(fog->cloud)","left",fog,"right",cloud);
				printOneStringLog("Tuple type","name","USERS_STATE"+String.valueOf(x+1));					
				app.addAppEdge(fog,cloud, 50, 20,"USERS_STATE", Tuple.UP, AppEdge.MODULE);
			}
			break;
		case 2:
			break;		
		}
		Log.printLine("---------------------------------------");
		Log.printLine("---------------------------------------");
		
		// 4. make tuple mapping
		for(int x=0;x<4;x++) {
			String appName = "class"+String.valueOf(x+1);
			String fog = appName+"_fog";
			String cloud = appName+"_cloud";
			for(int i=0; i < info.number_of_each_class.get(x); i++) {
				String sensor = "CAM"+String.valueOf(x+1)+"-"+String.valueOf(i);
				String edge = appName+"-"+String.valueOf(i);
				String data ="DATA"+String.valueOf(x+1)+"-"+String.valueOf(i);
				String result = "RESULT"+String.valueOf(x+1)+"-"+String.valueOf(i);
				String act = "ACT"+String.valueOf(x+1)+"-"+String.valueOf(i);
				String cam_data ="CAM_CLASS"+String.valueOf(x+1)+"-"+String.valueOf(i);
				// module name, input, output
				// mapping (edge) cam_data -> data
				printTwoStringLog("Make tuple mapping module:"+edge,"input",cam_data,"right",data);
				app.addTupleMapping(edge, cam_data, data, new FractionalSelectivity(1.0));

				// mapping (fog) data -> result
				printTwoStringLog("Make tuple mapping module:"+fog,"input",data,"right",result);
				app.addTupleMapping(fog, data, result, new FractionalSelectivity(1.0));
				
				// mapping (fog) data -> USERS_STATE
				printTwoStringLog("Make tuple mapping module:"+fog,"input",data,"right","USERS_STATE");
				app.addTupleMapping(fog, data, "USERS_STATE", new FractionalSelectivity(1.0));
				
				// mapping (edge) result -> SELF_STATE_UPDATE
				printTwoStringLog("Make tuple mapping module:"+edge,"input",result,"right","SELF_STATE_UPDATE");
				app.addTupleMapping(edge, result, "SELF_STATE_UPDATE", new FractionalSelectivity(1.0));
				loops.add(new AppLoop(new ArrayList<String>(){{add(cam_data);add(edge);add(fog);add(edge);add(act);}}));
			}
			// mapping (cloud) USERS_STATE -> CLOUD_RESULT
			app.addTupleMapping(cloud, "USERS_STATE", "CLOUD_RESULT", new FractionalSelectivity(1.0));
		}
		app.setLoops(loops);
		app.setUserId(1);
		
		return app;
	}
	private static void printTwoStringLog(String Purpose,String left,String l1,String right,String r1) {
		Log.printLine("["+Purpose+"] "+left+": "+l1+" ,"+right+" : "+r1);
	}
	private static void printOneStringLog(String Purpose,String left,String l1) {
		Log.printLine("["+Purpose+"] "+left+": "+l1);
	}
	private static Integer getKIndexofList(Integer k,Integer how_many, List<Integer> li) {
		Integer total = 0;
		Integer z = 0;
		Integer i = 0;
		List<Integer> temp = li;
		while(true) {
			z = temp.indexOf(k);
			if(i == how_many) {
				total += z;
				return total;
			}else {
				temp = temp.subList(z+1, temp.size());
				total+=z;
				total++;
				i++;
				continue;
			}
		}
	}
	private static Integer getWhichDevice(Integer idx_in_r) {
		Integer z = idx_in_r;
		if(z < NUMBER_OF_CLASS1) {
			return 0;
		} else if (z >= NUMBER_OF_CLASS1 && z < NUMBER_OF_CLASS1+NUMBER_OF_CLASS2) {
			return 1;
		} else if (z >= NUMBER_OF_CLASS1+NUMBER_OF_CLASS2 && z < NUMBER_OF_CLASS1+NUMBER_OF_CLASS2+NUMBER_OF_CLASS3) {
			return 2;
		} else if (z >= NUMBER_OF_CLASS1+NUMBER_OF_CLASS2+NUMBER_OF_CLASS3 && z < NUMBER_OF_CLASS1+NUMBER_OF_CLASS2+NUMBER_OF_CLASS3+NUMBER_OF_CLASS4) {
			return 3;
		} else {
			return -1;
		}
	}
	private static ModuleMapping createDevicesAndMapping(int userId,String[] appIds, int offloading_policy, ClassInfo info) {
		ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
		
		//TODO: make fog,cloud devices configurable, now these fixed to 1
		// 1. make cloud device
		FogDevice cloud = createFogDevice("cloud", CLOUD_MIPS, 32000, 10000, 10000, 0, 0.01, 100, 50); 
		cloud.setParentId(-1);		
		fogDevices.add(cloud);
		
		// 2. make fog device
		FogDevice fog = createFogDevice("fog-layer", FOG_MIPS, 8000, 1000, 1000, 1, 0.0, 8, 3.5);
		fog.setParentId(cloud.getId()); 
		fog.setUplinkLatency(FOG_TO_CLOUD_LATENCY);
		fogDevices.add(fog);
		
		// 3. make edge device
		for(int i=0;i<numOfSensorNode;i++){
			String sensorNodeId = "0-"+i;
			FogDevice sensorNode = createFogDevice("m-"+sensorNodeId, EDGE_MIPS, 1000, 1000, 1000, 2, 0, 1.0815,0.5665);
			sensorNode.setParentId(fog.getId());
			sensorNode.setUplinkLatency(EDGE_TO_FOG_LATENCY);
			fogDevices.add(sensorNode);
		}
		if(SINGLE_APP != 0) {
			// 4. make sensor,act devices
			String appName = "class"+CLASS_NUM;
			String fog_module = appName+"_fog";
			String cloud_module = appName+"_cloud";
			int idx = 0;
			for(int i=0; i < NUMBER_OF_APPS; i++) {
				String sensor = "CAM-"+String.valueOf(i);
				String edge = appName+"-"+String.valueOf(i);
				String data ="DATA"+CLASS_NUM+"-"+String.valueOf(i);
				String result = "RESULT"+CLASS_NUM+"-"+String.valueOf(i);
				String act = "ACT"+CLASS_NUM+"-"+String.valueOf(i);
				String cam_data ="CAM_CLASS"+CLASS_NUM+"-"+String.valueOf(i);
				String sensor_device_name = "s-"+CLASS_NUM+"-"+String.valueOf(i);

				//make sensors
				printOneStringLog("make sensor device :","name",sensor_device_name);
				Sensor sensor_device = null;
				if(appIds[CLASS_NUM-1].equals("class1")) {
					sensor_device = new Sensor(sensor_device_name,cam_data,userId,"multi_app",new DeterministicDistribution(CLASS1_TRANSMISSION_TIME));;				
				}
				else if(appIds[CLASS_NUM-1].equals("class2")) {
					sensor_device = new Sensor(sensor_device_name,cam_data,userId,"multi_app",new DeterministicDistribution(CLASS2_TRANSMISSION_TIME));;				
				}
				else if(appIds[CLASS_NUM-1].equals("class3")) {
					sensor_device = new Sensor(sensor_device_name,cam_data,userId,"multi_app",new DeterministicDistribution(CLASS3_TRANSMISSION_TIME));;				
				}
				else {
					sensor_device = new Sensor(sensor_device_name,cam_data,userId,"multi_app",new DeterministicDistribution(CLASS4_TRANSMISSION_TIME));;				
				}
				sensors.add(sensor_device);
				//make actuators
				String act_device_name = "act-"+CLASS_NUM+"-"+String.valueOf(i);
				printOneStringLog("make actuator device :","name",act_device_name);
				Actuator noti = new Actuator(act_device_name,userId,"multi_app",act);
				actuators.add(noti);
				
				// 5. connect devices to edge

				sensor_device.setGatewayDeviceId(fogDevices.get(2+idx).getId());
				sensor_device.setLatency(SENSOR_TO_EDGE_LATENCY);
				noti.setGatewayDeviceId(fogDevices.get(2+idx).getId());
				noti.setLatency(0.05);
				moduleMapping.addModuleToDevice(edge, fogDevices.get(idx+2).getName());
				idx++;
				if(idx == (numOfSensorNode)) idx = 0;
			}
			moduleMapping.addModuleToDevice(cloud_module, "cloud"); 			
			moduleMapping.addModuleToDevice(fog_module, "fog-layer"); 
		}else {
			// 4. make sensor,act devices
			ArrayList<Integer> device_idx = new ArrayList<Integer>(){};
			for(int x=0;x<4;x++) device_idx.add(0);

			for(int x=0;x<4;x++) {
				String appName = "class"+String.valueOf(x+1);
				String fog_module = appName+"_fog";
				String cloud_module = appName+"_cloud";
				for(int i=0; i < info.number_of_each_class.get(x); i++) {
					String sensor = "CAM"+String.valueOf(x+1)+"-"+String.valueOf(i);
					String edge = appName+"-"+String.valueOf(i);
					String data ="DATA"+String.valueOf(x+1)+"-"+String.valueOf(i);
					String result = "RESULT"+String.valueOf(x+1)+"-"+String.valueOf(i);
					String act = "ACT"+String.valueOf(x+1)+"-"+String.valueOf(i);
					String cam_data ="CAM_CLASS"+String.valueOf(x+1)+"-"+String.valueOf(i);
					String sensor_device_name = "s-"+String.valueOf(x+1)+"-"+String.valueOf(i);

					//make sensors
					printOneStringLog("make sensor device :","name",sensor_device_name);
					Sensor sensor_device = null;
					if(appIds[x].equals("class1")) {
						sensor_device = new Sensor(sensor_device_name,cam_data,userId,"multi_app",new DeterministicDistribution(CLASS1_TRANSMISSION_TIME));;				
					}
					else if(appIds[x].equals("class2")) {
						sensor_device = new Sensor(sensor_device_name,cam_data,userId,"multi_app",new DeterministicDistribution(CLASS2_TRANSMISSION_TIME));;				
					}
					else if(appIds[x].equals("class3")) {
						sensor_device = new Sensor(sensor_device_name,cam_data,userId,"multi_app",new DeterministicDistribution(CLASS3_TRANSMISSION_TIME));;				
					}
					else {
						sensor_device = new Sensor(sensor_device_name,cam_data,userId,"multi_app",new DeterministicDistribution(CLASS4_TRANSMISSION_TIME));;				
					}
					sensors.add(sensor_device);
					//make actuators
					String act_device_name = "act-"+String.valueOf(x+1)+"-"+String.valueOf(i);
					printOneStringLog("make actuator device :","name",act_device_name);
					Actuator noti = new Actuator(act_device_name,userId,"multi_app",act);
					actuators.add(noti);
					
					//TODO: change fogDevices to Hashmap				
					Integer idx = getKIndexofList(x,i,ClassInfo.random_idx);
					// 5. connect devices to edge

					sensor_device.setGatewayDeviceId(fogDevices.get(getWhichDevice(idx)+2).getId());
					sensor_device.setLatency(SENSOR_TO_EDGE_LATENCY);
					noti.setGatewayDeviceId(fogDevices.get(getWhichDevice(idx)+2).getId());
					noti.setLatency(0.05);
					moduleMapping.addModuleToDevice(edge, fogDevices.get(getWhichDevice(idx)+2).getName());
					Log.printLine("sensor, noti :"+sensor_device.getName()+","+noti.getName()+"->"+sensor_device.getGatewayDeviceId());
				}
				moduleMapping.addModuleToDevice(cloud_module, "cloud"); 			
				moduleMapping.addModuleToDevice(fog_module, "fog-layer"); 
			}			
		}

		return moduleMapping;
	}
	private static ModuleMapping createFogDevices(int userId, String[] appIds, int offloading_policy, ClassInfo info) {

		FogDevice cloud = createFogDevice("cloud", CLOUD_MIPS, 32000, 10000, 10000, 0, 0.01, 100, 50); 
		cloud.setParentId(-1);		
		fogDevices.add(cloud);
		
		//TODO: now number of fog-layer devices fixed at 1
		FogDevice fog = createFogDevice("fog-layer", FOG_MIPS, 8000, 1000, 1000, 1, 0.0, 8, 2.1);
		fog.setParentId(cloud.getId()); 
		fog.setUplinkLatency(FOG_TO_CLOUD_LATENCY);
		fogDevices.add(fog);

		for(int i=0;i<numOfSensorNode;i++){
			String sensorNodeId = "0-"+i;
			FogDevice sensorNode = createFogDevice("m-"+sensorNodeId, EDGE_MIPS, 1000, 1000, 1000, 2, 0, 0.5665, 1.0815);
			sensorNode.setParentId(fog.getId());
			sensorNode.setUplinkLatency(EDGE_TO_FOG_LATENCY);
			fogDevices.add(sensorNode);
		}		
		ArrayList<Integer> apps = new ArrayList<Integer>(){};
		for(int x=0; x < 4; x++) apps.add(0);
		int didx = 2;
		int i = 0;
		for(int x=0; x < 4; x++) {
			i=0;
			for(Integer app_idx : info.map_of_each_class.get(appIds[x])) {
				Sensor sensor = null;
				if(appIds[x].equals("class1")) {
					sensor = new Sensor("s-"+String.valueOf(app_idx+1)+"-"+apps.get(app_idx),"CAM_class"+String.valueOf(app_idx+1), userId, appIds[x], new DeterministicDistribution(CLASS1_TRANSMISSION_TIME));				
				}
				else if(appIds[x].equals("class2")) {
					sensor = new Sensor("s-"+String.valueOf(app_idx+1)+"-"+apps.get(app_idx),"CAM_class"+String.valueOf(app_idx+1), userId, appIds[x], new DeterministicDistribution(CLASS2_TRANSMISSION_TIME));				
				}
				else if(appIds[x].equals("class3")) {
					sensor = new Sensor("s-"+String.valueOf(app_idx+1)+"-"+apps.get(app_idx),"CAM_class"+String.valueOf(app_idx+1), userId, appIds[x], new DeterministicDistribution(CLASS3_TRANSMISSION_TIME));				
				}
				else {
					sensor = new Sensor("s-"+String.valueOf(app_idx+1)+"-"+apps.get(app_idx),"CAM_class"+String.valueOf(app_idx+1), userId, appIds[x], new DeterministicDistribution(CLASS4_TRANSMISSION_TIME));				
				}
				sensors.add(sensor);
				Actuator noti = new Actuator(appIds[x]+"_act-"+String.valueOf(app_idx+1),userId,appIds[x],appIds[x]+"_ACT");
				actuators.add(noti);
				sensor.setGatewayDeviceId(fogDevices.get(didx).getId());
				sensor.setLatency(SENSOR_TO_EDGE_LATENCY);
				noti.setGatewayDeviceId(fogDevices.get(didx).getId());
				noti.setLatency(5.0);
				Log.printLine("Sensor : "+"s-"+String.valueOf(app_idx+1)+"-"+apps.get(app_idx)+", Device: "+fogDevices.get(didx).getName()+", Tuple: "+"CAM_class"+String.valueOf(app_idx+1));
				apps.set(app_idx, apps.get(app_idx)+1);
			}
			didx += 1;
		}
			
		for(int x=0; x < 4; x++) apps.add(0);
		ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping
		for(int x=0; x < 4; x++) {
			moduleMapping.addModuleToDevice("class"+String.valueOf(x+1)+"_cloud", "cloud"); // fixing all instances of the Connector module to the Cloud			
			moduleMapping.addModuleToDevice("class"+String.valueOf(x+1)+"_fog", "fog-layer"); // fixing all instances of the Concentration Calculator module to the Cloud
		}
		apps.clear();
		for(int x=0; x < 4; x++) apps.add(0);
		for(int x=0; x < 4; x++) {
			for(Integer app_idx : info.map_of_each_class.get(appIds[x])) {
				for(FogDevice device : fogDevices) {
					if(device.getName().startsWith("m")){
						String[] temps = device.getName().split("-");
						String last = temps[temps.length-1];
						if(Integer.valueOf(last) == x) {
							Log.printLine("module name : "+"class"+String.valueOf(app_idx+1)+"-"+Integer.valueOf(apps.get(app_idx))+"-> "+device.getName());
							moduleMapping.addModuleToDevice("class"+String.valueOf(app_idx+1)+"-"+Integer.valueOf(apps.get(app_idx)), device.getName());	
							apps.set(app_idx, apps.get(app_idx)+1);
						}
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
		int bw = 100000;

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
		double cost = 5.0; // the cost of using processing in this resource
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
					new AppModuleAllocationPolicy(hostList), storageList, 1000000, upBw, downBw, 0, ratePerMips);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		fogdevice.setLevel(level);
		return fogdevice;
	}
}