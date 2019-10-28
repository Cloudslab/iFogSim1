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

public class MultiClassApp {
	static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	static List<Sensor> sensors = new ArrayList<Sensor>();
	static List<Actuator> actuators = new ArrayList<Actuator>();
	static List<AppLoop> loops = new ArrayList<AppLoop>();
	static String[] appIds = new String[] { "class1", "class2", "class3", "class4" };

	public static void main(String[] args) {
		Log.printLine("Starting multi class Applications...");
		ClassInfo class_info = new ClassInfo();
		if (args.length != 0) {
			// TODO: add multiple application on same device execution map
//			class_info.openConfigFile(args[0], args[1]);
			class_info.openConfigFile(args[0]);
		}
		if (class_info.ENABLE_LOG == 0)
			Log.disable();
		startSimulation(class_info);
		System.exit(0);
	}

	private static void setOffloading(ClassInfo class_info) {
		class_info.OFFLOADING_POLICY = class_info.res_map[class_info.CLASS_NUM - 1][class_info.NUMBER_OF_APPS
				- 1][class_info.PACKET_LOSS / 2];
	}

	private static void startSimulation(ClassInfo class_info) {
		Log.printLine("CREATE APPLICATIONS");
		try {
			int num_user = 1;
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;

			class_info.setVariables();

			CloudSim.init(num_user, calendar, trace_flag);
			Application app = null;
			if (class_info.SINGLE_APP != 0) {
				if (class_info.using_res_map != 0) {
					setOffloading(class_info);
				}
				app = createApplication(class_info);
			} else {
				app = createApplications(class_info);
			}

			ModuleMapping module_mapping = createDevicesAndMapping(num_user, appIds, 0, class_info);

			Controller controller = new Controller("master-controller", fogDevices, sensors, actuators,
					class_info.using_fresult);

			if (class_info.using_fresult == 1) {
				controller.setResult_fpath((String) class_info.configs[0] + "/" + (String) class_info.configs[1] + "_"
						+ String.valueOf((Integer) class_info.configs[2]) + "_" + java.time.LocalDateTime.now()
						+ ".csv");
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

	private static Application createApplications(ClassInfo info) {
		Application app = makeClasses("multi_app", info);
		return app;
	}

	private static void tupleMapping(Application app, ClassInfo info, int offloading_policy, int device_idx, int number_of_class) {
		String appName = "class" + number_of_class;
		String fog = appName + "_fog";
		String cloud = appName + "_cloud";

		String sensor = "CAM-" + String.valueOf(device_idx);
		String edge = appName + "-" + String.valueOf(device_idx);
		String data = "DATA" + number_of_class + "-" + String.valueOf(device_idx);
		String result = "RESULT" + number_of_class + "-" + String.valueOf(device_idx);
		String act = "ACT" + number_of_class + "-" + String.valueOf(device_idx);
		String cam_data = "CAM_CLASS" + number_of_class + "-" + String.valueOf(device_idx);
		String cinput = "CINPUT" + number_of_class + "-" + String.valueOf(device_idx);
		String cresult = "CRESULT" + number_of_class + "-" + String.valueOf(device_idx);

		System.out.println(appName);
		System.out.println(fog);
		System.out.println(cloud);
		System.out.println(sensor);
		System.out.println(edge);
		System.out.println(data);
		System.out.println(result);
		System.out.println(act);
		System.out.println(cam_data);
		System.out.println(cinput);
		System.out.println(cresult);
		// module name, input, output
		// mapping (edge) cam_data -> data
		info.printTwoStringLog("Make tuple mapping module:" + edge, "input", cam_data, "right", data);
		app.addTupleMapping(edge, cam_data, data, new FractionalSelectivity(1.0));
		if (offloading_policy == 2) {
			// mapping (fog) data -> cinput
			app.addTupleMapping(fog, data, cinput, new FractionalSelectivity(1.0));
			app.addTupleMapping(fog, cresult, result, new FractionalSelectivity(1.0));
			app.addTupleMapping(cloud, cinput, cresult, new FractionalSelectivity(1.0));
			app.addTupleMapping(edge, result, "SELF_STATE_UPDATE", new FractionalSelectivity(1.0));
			loops.add(new AppLoop(new ArrayList<String>() {
				{
					add(cam_data);
					add(edge);
					add(fog);
					add(cloud);
					add(fog);
					add(edge);
					add(act);
				}
			}));
		} else {
			// mapping (fog) data -> result
			info.printTwoStringLog("Make tuple mapping module:" + fog, "input", data, "right", result);
			app.addTupleMapping(fog, data, result, new FractionalSelectivity(1.0));
			// mapping (fog) data -> USERS_STATE
			info.printTwoStringLog("Make tuple mapping module:" + fog, "input", data, "right", "USERS_STATE");
			app.addTupleMapping(fog, data, "USERS_STATE", new FractionalSelectivity(1.0));
			info.printTwoStringLog("Make tuple mapping module:" + edge, "input", result, "right", "SELF_STATE_UPDATE");
			app.addTupleMapping(edge, result, "SELF_STATE_UPDATE", new FractionalSelectivity(1.0));
			loops.add(new AppLoop(new ArrayList<String>() {
				{
					add(cam_data);
					add(edge);
					add(fog);
					add(edge);
					add(act);
				}
			}));
		}
		if (offloading_policy != 2)
			app.addTupleMapping(cloud, "USERS_STATE", "CLOUD_RESULT", new FractionalSelectivity(1.0));
	}

	private static void makeEdge(Application app, ClassInfo info, int offloading_policy, int device_idx, int number_of_class) {
		String appName = "class" + number_of_class;
		String fog = appName + "_fog";
		String cloud = appName + "_cloud";

		double up_size = 0;
		double down_size = 0;

		switch (appName) {
		case "class1":
			up_size = info.CLASS1_INPUT_SIZE;
			down_size = info.CLASS1_OUTPUT_SIZE;
			break;
		case "class2":
			up_size = info.CLASS2_INPUT_SIZE;
			down_size = info.CLASS2_OUTPUT_SIZE;
			break;
		case "class3":
			up_size = info.CLASS3_INPUT_SIZE;
			down_size = info.CLASS3_OUTPUT_SIZE;
			break;
		case "class4":
			up_size = info.CLASS4_INPUT_SIZE;
			down_size = info.CLASS4_OUTPUT_SIZE;
			break;
		}
		String sensor = "CAM-" + String.valueOf(device_idx);
		String edge = appName + "-" + String.valueOf(device_idx);
		String cam_data = "CAM_CLASS" + number_of_class + "-" + String.valueOf(device_idx);
		String data = "DATA" + number_of_class + "-" + String.valueOf(device_idx);
		String result = "RESULT" + number_of_class + "-" + String.valueOf(device_idx);
		String act = "ACT" + number_of_class + "-" + String.valueOf(device_idx);
		String cinput = "CINPUT" + number_of_class + "-" + String.valueOf(device_idx);
		String cresult = "CRESULT" + number_of_class + "-" + String.valueOf(device_idx);

		switch (offloading_policy) {
		case 0:
			if (appName.equals("class1")) {
				app.addAppEdge(cam_data, edge, info.CLASS1_MIPS, up_size, cam_data, Tuple.UP, AppEdge.SENSOR);
			} else if (appName.equals("class2")) {
				app.addAppEdge(cam_data, edge, info.CLASS2_MIPS, up_size, cam_data, Tuple.UP, AppEdge.SENSOR);
			} else if (appName.equals("class3")) {
				app.addAppEdge(cam_data, edge, info.CLASS3_MIPS, up_size, cam_data, Tuple.UP, AppEdge.SENSOR);
			} else {
				app.addAppEdge(cam_data, edge, info.CLASS4_MIPS, up_size, cam_data, Tuple.UP, AppEdge.SENSOR);
			}

			app.addAppEdge(edge, fog, 50, 50, data, Tuple.UP, AppEdge.MODULE);

			// make fog -> edge
			app.addAppEdge(fog, edge, 50, 50, result, Tuple.DOWN, AppEdge.MODULE);

			// make edge -> act
			app.addAppEdge(edge, act, 50, 50, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);
			// make fog -> cloud
			app.addAppEdge(fog, cloud, 50, 50, "USERS_STATE", Tuple.UP, AppEdge.MODULE);
			break;
		case 1:
			app.addAppEdge(cam_data, edge, 50, up_size, cam_data, Tuple.UP, AppEdge.SENSOR); // edge computing
			// make edge -> fog
			if (appName.equals("class1")) {
				app.addAppEdge(edge, fog, info.CLASS1_MIPS, up_size, data, Tuple.UP, AppEdge.MODULE);
			} else if (appName.equals("class2")) {
				app.addAppEdge(edge, fog, info.CLASS2_MIPS, up_size, data, Tuple.UP, AppEdge.MODULE);
			} else if (appName.equals("class3")) {
				app.addAppEdge(edge, fog, info.CLASS3_MIPS, up_size, data, Tuple.UP, AppEdge.MODULE);
			} else {
				app.addAppEdge(edge, fog, info.CLASS4_MIPS, up_size, data, Tuple.UP, AppEdge.MODULE);
			} // fog computing

			// make fog -> edge

			app.addAppEdge(fog, edge, 50, down_size, result, Tuple.DOWN, AppEdge.MODULE);
			// make edge -> act

			app.addAppEdge(edge, act, 50, 50, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);
			// make fog -> cloud
			app.addAppEdge(fog, cloud, 50, 50, "USERS_STATE", Tuple.UP, AppEdge.MODULE);
			break;
		case 2:
			app.addAppEdge(cam_data, edge, 50, up_size, cam_data, Tuple.UP, AppEdge.SENSOR); // edge computing

			// make edge -> fog
			app.addAppEdge(edge, fog, 50, up_size, data, Tuple.UP, AppEdge.MODULE);

			// make fog -> cloud
			// app.addAppEdge(fog, cloud, 50, up_size, cinput, Tuple.UP, AppEdge.MODULE);

			if (appName.equals("class1")) {
				app.addAppEdge(fog, cloud, info.CLASS1_MIPS, up_size, cinput, Tuple.UP, AppEdge.MODULE);
			} else if (appName.equals("class2")) {
				app.addAppEdge(fog, cloud, info.CLASS2_MIPS, up_size, cinput, Tuple.UP, AppEdge.MODULE);
			} else if (appName.equals("class3")) {
				app.addAppEdge(fog, cloud, info.CLASS3_MIPS, up_size, cinput, Tuple.UP, AppEdge.MODULE);
			} else {
				app.addAppEdge(fog, cloud, info.CLASS4_MIPS, up_size, cinput, Tuple.UP, AppEdge.MODULE);
			}
			// cloud -> fog
			app.addAppEdge(cloud, fog, 50, down_size, cresult, Tuple.DOWN, AppEdge.MODULE);
			/*
			 * if(appName.equals("class1")) { app.addAppEdge(cloud, fog, CLASS1_MIPS,
			 * down_size, cresult, Tuple.DOWN, AppEdge.MODULE); } else
			 * if(appName.equals("class2")) { app.addAppEdge(cloud, fog, CLASS2_MIPS,
			 * down_size, cresult, Tuple.DOWN, AppEdge.MODULE); } else
			 * if(appName.equals("class3")) { app.addAppEdge(cloud, fog, CLASS3_MIPS,
			 * down_size, cresult, Tuple.DOWN, AppEdge.MODULE); } else {
			 * app.addAppEdge(cloud, fog, CLASS4_MIPS, down_size, cresult, Tuple.DOWN,
			 * AppEdge.MODULE); }
			 */
			// make fog -> edge
			app.addAppEdge(fog, edge, 50, down_size, result, Tuple.DOWN, AppEdge.MODULE);
			// make edge -> act
			app.addAppEdge(edge, act, 50, 20, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);
			break;
		}
	}

	private static Application createApplication(ClassInfo info) {
		Application app = Application.createApplication("multi_app", 1);
		int number_of_class = info.CLASS_NUM;
		int offloading_policy = info.OFFLOADING_POLICY;

		for (int i = 0; i < info.NUMBER_OF_APPS; i++) {
			app.addAppModule("class" + number_of_class + "-" + String.valueOf(i), 10);
			info.printOneStringLog("Make appModule", "name", "class" + number_of_class + "-" + String.valueOf(i));
		}
		app.addAppModule("class" + number_of_class + "_fog", 15);
		app.addAppModule("class" + number_of_class + "_cloud", 20);

		info.printOneStringLog("Make appModule", "name", "class" + number_of_class + "_fog");
		info.printOneStringLog("Make appModule", "name", "class" + number_of_class + "_cloud");

		String appName = "class" + number_of_class;
		String fog = appName + "_fog";
		String cloud = appName + "_cloud";

		double up_size = 0;
		double down_size = 0;
		switch (appName) {
		case "class1":
			up_size = info.CLASS1_INPUT_SIZE;
			down_size = info.CLASS1_OUTPUT_SIZE;
			break;
		case "class2":
			up_size = info.CLASS2_INPUT_SIZE;
			down_size = info.CLASS2_OUTPUT_SIZE;
			break;
		case "class3":
			up_size = info.CLASS3_INPUT_SIZE;
			down_size = info.CLASS3_OUTPUT_SIZE;
			break;
		case "class4":
			up_size = info.CLASS4_INPUT_SIZE;
			down_size = info.CLASS4_OUTPUT_SIZE;
			break;
		}
		int current_idx = 0;
		for (int p = 0; p < 3; p++) {
			for (int k = 0; k < info.RUNNING_REGION.get(p); k++) {
				makeEdge(app, info, p, current_idx++, info.CLASS_NUM);
			}
		}
		current_idx = 0;
		for (int p = 0; p < 3; p++) {
			for (int k = 0; k < info.RUNNING_REGION.get(p); k++) {
				tupleMapping(app, info, p, current_idx++, info.CLASS_NUM);
			}
		}

		app.setLoops(loops);
		app.setUserId(1);
		return app;
	}

	private static Application makeClasses(String appId, ClassInfo info) {
		// 1. make empty app
		Application app = Application.createApplication(appId, 1);
		int[] number_of_classes = {info.NUMBER_OF_CLASS1, info.NUMBER_OF_CLASS2, info.NUMBER_OF_CLASS3, info.NUMBER_OF_CLASS4};
		
		int j = 0;
		for(int each : number_of_classes) {
			j++;
			if(each == 0) continue;
			for (int i = 0; i < each; i++) {
				app.addAppModule("class" + j + "-" + String.valueOf(i), 10);
				info.printOneStringLog("Make appModule", "name", "class" + j + "-" + String.valueOf(i));
			}
			app.addAppModule("class" + j + "_fog", 15);
			app.addAppModule("class" + j + "_cloud", 20);

			info.printOneStringLog("Make appModule", "name", "class" + j + "_fog");
			info.printOneStringLog("Make appModule", "name", "class" + j + "_cloud");

			String appName = "class" + j;
			String fog = appName + "_fog";
			String cloud = appName + "_cloud";

			double up_size = 0;
			double down_size = 0;
			switch (appName) {
			case "class1":
				up_size = info.CLASS1_INPUT_SIZE;
				down_size = info.CLASS1_OUTPUT_SIZE;
				break;
			case "class2":
				up_size = info.CLASS2_INPUT_SIZE;
				down_size = info.CLASS2_OUTPUT_SIZE;
				break;
			case "class3":
				up_size = info.CLASS3_INPUT_SIZE;
				down_size = info.CLASS3_OUTPUT_SIZE;
				break;
			case "class4":
				up_size = info.CLASS4_INPUT_SIZE;
				down_size = info.CLASS4_OUTPUT_SIZE;
				break;
			}
		}
		int current_idx = 0;
		j = 0;
		for(int each : number_of_classes) {
			j++;
			if(each == 0) continue;
			for(int x=0;x < each; x++)
				makeEdge(app,info, info.OFFLOADING_POLICY, current_idx++, j);
			current_idx = 0;
		}
		current_idx = 0;
		j = 0;
		for(int each : number_of_classes) {
			j++;
			if(each == 0) continue;
			for(int x=0;x < each; x++)
				tupleMapping(app,info, info.OFFLOADING_POLICY, current_idx++, j);
			current_idx = 0;
		}
		
		app.setLoops(loops);
		app.setUserId(1);
		

		return app;
	}

	private static ModuleMapping createDevicesAndMapping(int userId, String[] appIds, int offloading_policy,
			ClassInfo info) {
		ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
		int class_num = info.CLASS_NUM - 1;
		// 1. make cloud device
		FogDevice cloud = createFogDevice("cloud", info.CLOUD_MIPS[class_num], 32000, info.CLOUD_UPBW[class_num],
				info.CLOUD_DOWNBW[class_num], 0, 0.01, 100, 0, info);

		cloud.setParentId(-1);
		fogDevices.add(cloud);

		// 2. make fog device
		// bandwidth(kb/s)
		FogDevice fog = createFogDevice("fog-layer", info.FOG_MIPS, 8000, info.FOG_UPBW[class_num],
				info.FOG_DOWNBW[class_num], 1, 0.0, 8, 0, info);
		fog.setParentId(cloud.getId());
		fog.setUplinkLatency(info.FOG_TO_CLOUD_LATENCY);
		fogDevices.add(fog);

		// 3. make edge device
		for (int i = 0; i < info.numOfSensorNode; i++) {
			String sensorNodeId = "0-" + i;
			FogDevice sensorNode = createFogDevice("m-" + sensorNodeId, info.EDGE_MIPS[class_num], 1000,
					info.EDGE_UPBW[class_num], info.EDGE_DOWNBW[class_num], 2, 0, 1.0815, 0, info);
			sensorNode.setParentId(fog.getId());
			sensorNode.setUplinkLatency(info.EDGE_TO_FOG_LATENCY);
			fogDevices.add(sensorNode);
		}
		if (info.SINGLE_APP != 0) {
			// 4. make sensor,act devices
			String appName = "class" + info.CLASS_NUM;
			String fog_module = appName + "_fog";
			String cloud_module = appName + "_cloud";
			int idx = 0;
			for (int i = 0; i < info.NUMBER_OF_APPS; i++) {
				String sensor = "CAM-" + String.valueOf(i);
				String edge = appName + "-" + String.valueOf(i);
				String data = "DATA" + info.CLASS_NUM + "-" + String.valueOf(i);
				String result = "RESULT" + info.CLASS_NUM + "-" + String.valueOf(i);
				String act = "ACT" + info.CLASS_NUM + "-" + String.valueOf(i);
				String cam_data = "CAM_CLASS" + info.CLASS_NUM + "-" + String.valueOf(i);
				String sensor_device_name = "s-" + info.CLASS_NUM + "-" + String.valueOf(i);

				// make sensors
				info.printOneStringLog("make sensor device :", "name", sensor_device_name);
				Sensor sensor_device = null;
				if (appIds[info.CLASS_NUM - 1].equals("class1")) {
					sensor_device = new Sensor(sensor_device_name, cam_data, userId, "multi_app",
							new DeterministicDistribution(info.CLASS1_TRANSMISSION_TIME));
					;
				} else if (appIds[info.CLASS_NUM - 1].equals("class2")) {
					sensor_device = new Sensor(sensor_device_name, cam_data, userId, "multi_app",
							new DeterministicDistribution(info.CLASS2_TRANSMISSION_TIME));
					;
				} else if (appIds[info.CLASS_NUM - 1].equals("class3")) {
					sensor_device = new Sensor(sensor_device_name, cam_data, userId, "multi_app",
							new DeterministicDistribution(info.CLASS3_TRANSMISSION_TIME));
					;
				} else {
					sensor_device = new Sensor(sensor_device_name, cam_data, userId, "multi_app",
							new DeterministicDistribution(info.CLASS4_TRANSMISSION_TIME));
					;
				}
				sensors.add(sensor_device);
				// make actuators
				String act_device_name = "act-" + info.CLASS_NUM + "-" + String.valueOf(i);
				info.printOneStringLog("make actuator device :", "name", act_device_name);
				Actuator noti = new Actuator(act_device_name, userId, "multi_app", act);
				actuators.add(noti);

				// 5. connect devices to edge

				sensor_device.setGatewayDeviceId(fogDevices.get(2 + idx).getId());
				sensor_device.setLatency(info.SENSOR_TO_EDGE_LATENCY);
				noti.setGatewayDeviceId(fogDevices.get(2 + idx).getId());
				noti.setLatency(0.05);
				moduleMapping.addModuleToDevice(edge, fogDevices.get(idx + 2).getName());
				idx++;
				if (idx == (info.numOfSensorNode))
					idx = 0;
			}
			moduleMapping.addModuleToDevice(cloud_module, "cloud");
			moduleMapping.addModuleToDevice(fog_module, "fog-layer");
		} else {
			int[] number_of_classes = {info.NUMBER_OF_CLASS1, info.NUMBER_OF_CLASS2, info.NUMBER_OF_CLASS3, info.NUMBER_OF_CLASS4};
			// 4. make sensor,act devices
			int j = 0;
			int idx = 2;
			for (int each : number_of_classes) {
				j++;
				if(each == 0) continue;
				String appName = "class" + String.valueOf(j);
				String fog_module = appName + "_fog";
				String cloud_module = appName + "_cloud";					
				for(int x = 0; x < each; x++) {
					String sensor = "CAM" + String.valueOf(j) + "-" + String.valueOf(x);
					String edge = appName + "-" + String.valueOf(x);
					String data = "DATA" + String.valueOf(j) + "-" + String.valueOf(x);
					String result = "RESULT" + String.valueOf(j) + "-" + String.valueOf(x);
					String act = "ACT" + String.valueOf(j) + "-" + String.valueOf(x);
					String cam_data = "CAM_CLASS" + String.valueOf(j) + "-" + String.valueOf(x);
					String sensor_device_name = "s-" + String.valueOf(j) + "-" + String.valueOf(x);
					System.out.println(sensor);
					System.out.println(edge);
					System.out.println(data);
					System.out.println(result);
					System.out.println(act);
					System.out.println(cam_data);
					System.out.println(sensor_device_name);
					
					// make sensors
					info.printOneStringLog("make sensor device", "name", sensor_device_name);
					Sensor sensor_device = null;
					if (appIds[x].equals("class1")) {
						sensor_device = new Sensor(sensor_device_name, cam_data, userId, "multi_app",
								new DeterministicDistribution(info.CLASS1_TRANSMISSION_TIME));
						;
					} else if (appIds[x].equals("class2")) {
						sensor_device = new Sensor(sensor_device_name, cam_data, userId, "multi_app",
								new DeterministicDistribution(info.CLASS2_TRANSMISSION_TIME));
						;
					} else if (appIds[x].equals("class3")) {
						sensor_device = new Sensor(sensor_device_name, cam_data, userId, "multi_app",
								new DeterministicDistribution(info.CLASS3_TRANSMISSION_TIME));
						;
					} else {
						sensor_device = new Sensor(sensor_device_name, cam_data, userId, "multi_app",
								new DeterministicDistribution(info.CLASS4_TRANSMISSION_TIME));
						;
					}
					sensors.add(sensor_device);
					// make actuators
					String act_device_name = "act-" + String.valueOf(j) + "-" + String.valueOf(x);
					info.printOneStringLog("make actuator device", "name", act_device_name);
					Actuator noti = new Actuator(act_device_name, userId, "multi_app", act);
					actuators.add(noti);

					//Integer idx = info.getKIndexofList(x, i, ClassInfo.random_idx);
					// 5. connect devices to edge
					sensor_device.setGatewayDeviceId(fogDevices.get(idx).getId());
					sensor_device.setLatency(info.SENSOR_TO_EDGE_LATENCY);
					noti.setGatewayDeviceId(fogDevices.get(idx).getId());
					noti.setLatency(0.05);
					moduleMapping.addModuleToDevice(edge, fogDevices.get(idx++).getName());
					Log.printLine("sensor, noti :" + sensor_device.getName() + "," + noti.getName() + "->"
							+ sensor_device.getGatewayDeviceId());
				}
				moduleMapping.addModuleToDevice(cloud_module, "cloud");
				moduleMapping.addModuleToDevice(fog_module, "fog-layer");
			}
		}

		return moduleMapping;
	}

	private static FogDevice createFogDevice(String nodeName, long mips, int ram, double upBw, double downBw, int level,
			double ratePerMips, double busyPower, double idlePower, ClassInfo info) {

		List<Pe> peList = new ArrayList<Pe>();

		peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

		int hostId = FogUtils.generateEntityId();
		long storage = 1000000; // host storage
		int bw = 100000;

		PowerHost host = new PowerHost(hostId, new RamProvisionerSimple(ram), new BwProvisionerOverbooking(bw), storage,
				peList, new StreamOperatorScheduler(peList), new FogLinearPowerModel(busyPower, idlePower));

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

		LinkedList<Storage> storageList = new LinkedList<Storage>();
		// devices by now

		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(arch, os, vmm, host, time_zone, cost,
				costPerMem, costPerStorage, costPerBw);

		FogDevice fogdevice = null;
		try {
			fogdevice = new FogDevice(nodeName, characteristics, new AppModuleAllocationPolicy(hostList), storageList,
					1000000, upBw, downBw, 0, ratePerMips, info);
		} catch (Exception e) {
			e.printStackTrace();
		}

		fogdevice.setLevel(level);
		return fogdevice;
	}
}