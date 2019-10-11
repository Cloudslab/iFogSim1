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
		// 0 -> running on the edge
		// 1 -> on the fog
		// 2 -> on the cloud
		// 3 -> on the random place
		Application app = makeClasses("multi_app", 0, info);
		return app;
	}

	private static Application createApplication(ClassInfo info) {
		Application app = Application.createApplication("multi_app", 1);
		int number_of_class = info.CLASS_NUM;
		int offloading_policy = info.OFFLOADING_POLICY;
		// 2. make 4 classes client, fog, cloud appmodule

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

		// 3. make appEdges;
		switch (offloading_policy) {
		case 0:
			for (int i = 0; i < info.NUMBER_OF_APPS; i++) {
				String sensor = "CAM-" + String.valueOf(i);
				String edge = appName + "-" + String.valueOf(i);
				String cam_data = "CAM_CLASS" + number_of_class + "-" + String.valueOf(i);
				String data = "DATA" + number_of_class + "-" + String.valueOf(i);

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
				String result = "RESULT" + number_of_class + "-" + String.valueOf(i);
				app.addAppEdge(fog, edge, 50, 50, result, Tuple.DOWN, AppEdge.MODULE);

				// make edge -> act
				String act = "ACT" + number_of_class + "-" + String.valueOf(i);
				app.addAppEdge(edge, act, 50, 50, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);

			}
			// make fog -> cloud
			app.addAppEdge(fog, cloud, 50, 50, "USERS_STATE", Tuple.UP, AppEdge.MODULE);
			break;
		case 1:
			for (int i = 0; i < info.NUMBER_OF_APPS; i++) {
				String sensor = "CAM-" + String.valueOf(i);
				String edge = appName + "-" + String.valueOf(i);
				String cam_data = "CAM_CLASS" + number_of_class + "-" + String.valueOf(i);

				app.addAppEdge(cam_data, edge, 50, up_size, cam_data, Tuple.UP, AppEdge.SENSOR); // edge computing

				// make edge -> fog
				String data = "DATA" + number_of_class + "-" + String.valueOf(i);
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
				String result = "RESULT" + number_of_class + "-" + String.valueOf(i);
				app.addAppEdge(fog, edge, 50, down_size, result, Tuple.DOWN, AppEdge.MODULE);
				// make edge -> act
				String act = "ACT" + number_of_class + "-" + String.valueOf(i);
				app.addAppEdge(edge, act, 50, 50, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);

			}
			// make fog -> cloud
			app.addAppEdge(fog, cloud, 50, 50, "USERS_STATE", Tuple.UP, AppEdge.MODULE);
			break;
		case 2:
			for (int i = 0; i < info.NUMBER_OF_APPS; i++) {
				String sensor = "CAM-" + String.valueOf(i);
				String edge = appName + "-" + String.valueOf(i);
				String cam_data = "CAM_CLASS" + number_of_class + "-" + String.valueOf(i);

				app.addAppEdge(cam_data, edge, 50, up_size, cam_data, Tuple.UP, AppEdge.SENSOR); // edge computing

				// make edge -> fog
				String data = "DATA" + number_of_class + "-" + String.valueOf(i);
				app.addAppEdge(edge, fog, 50, up_size, data, Tuple.UP, AppEdge.MODULE);

				// make fog -> cloud
				String cinput = "CINPUT" + number_of_class + "-" + String.valueOf(i);
				String result = "RESULT" + number_of_class + "-" + String.valueOf(i);
				String cresult = "CRESULT" + number_of_class + "-" + String.valueOf(i);
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
				String act = "ACT" + number_of_class + "-" + String.valueOf(i);
				app.addAppEdge(edge, act, 50, 20, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);

			}
			break;
		}
		// 4. make tuple mapping
		for (int i = 0; i < info.NUMBER_OF_APPS; i++) {
			String sensor = "CAM-" + String.valueOf(i);
			String edge = appName + "-" + String.valueOf(i);
			String data = "DATA" + number_of_class + "-" + String.valueOf(i);
			String result = "RESULT" + number_of_class + "-" + String.valueOf(i);
			String act = "ACT" + number_of_class + "-" + String.valueOf(i);
			String cam_data = "CAM_CLASS" + number_of_class + "-" + String.valueOf(i);
			String cinput = "CINPUT" + number_of_class + "-" + String.valueOf(i);
			String cresult = "CRESULT" + number_of_class + "-" + String.valueOf(i);
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
				info.printTwoStringLog("Make tuple mapping module:" + edge, "input", result, "right",
						"SELF_STATE_UPDATE");
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

			// mapping (edge) result -> SELF_STATE_UPDATE
		}
		// mapping (cloud) USERS_STATE -> CLOUD_RESULT
		if (offloading_policy != 2)
			app.addTupleMapping(cloud, "USERS_STATE", "CLOUD_RESULT", new FractionalSelectivity(1.0));
		app.setLoops(loops);
		app.setUserId(1);
		return app;
	}

	private static Application makeClasses(String appId, int offloading_policy, ClassInfo info) {
		// 1. make empty app
		Application app = Application.createApplication(appId, 1);

		// 2. make 4 classes client, fog, cloud appmodule
		for (int x = 0; x < 4; x++) {
			for (int i = 0; i < info.number_of_each_class.get(x); i++) {
				app.addAppModule("class" + String.valueOf(x + 1) + "-" + String.valueOf(i), 2);

				info.printOneStringLog("Make appModule", "name",
						"class" + String.valueOf(x + 1) + "-" + String.valueOf(i));
			}
			app.addAppModule("class" + String.valueOf(x + 1) + "_fog", 15);
			app.addAppModule("class" + String.valueOf(x + 1) + "_cloud", 20);

			info.printOneStringLog("Make appModule", "name", "class" + String.valueOf(x + 1) + "_fog");
			info.printOneStringLog("Make appModule", "name", "class" + String.valueOf(x + 1) + "_cloud");
		}

		// 3. make appEdges
		switch (offloading_policy) {
		case 0:
			for (int x = 0; x < 4; x++) {
				String appName = "class" + String.valueOf(x + 1);
				String fog = appName + "_fog";
				String cloud = appName + "_cloud";
				for (int i = 0; i < info.number_of_each_class.get(x); i++) {
					String sensor = "CAM" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					String edge = appName + "-" + String.valueOf(i);
					String cam_data = "CAM_CLASS" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					// make sensor -> edge
					info.printTwoStringLog("Make edge(sensor->edge)", "left", sensor, "right", edge);
					info.printOneStringLog("Tuple type", "name", cam_data);
					app.addAppEdge(cam_data, edge, 10, 50, cam_data, Tuple.UP, AppEdge.SENSOR);

					// make edge -> fog
					info.printTwoStringLog("Make edge(edge->fog)", "left", edge, "right", fog);
					info.printOneStringLog("Tuple type", "name",
							"DATA" + String.valueOf(x + 1) + "-" + String.valueOf(i));
					String data = "DATA" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					if (appName.equals("class1")) {
						app.addAppEdge(edge, fog, 77145, 50, data, Tuple.UP, AppEdge.MODULE);
					} else if (appName.equals("class2")) {
						app.addAppEdge(edge, fog, 4835, 50, data, Tuple.UP, AppEdge.MODULE);
					} else if (appName.equals("class3")) {
						app.addAppEdge(edge, fog, 150472, 50, data, Tuple.UP, AppEdge.MODULE);
					} else {
						app.addAppEdge(edge, fog, 3062, 50, data, Tuple.UP, AppEdge.MODULE);
					}

					// make fog -> edge
					String result = "RESULT" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					info.printTwoStringLog("Make edge(fog->edge)", "left", fog, "right", edge);
					info.printOneStringLog("Tuple type", "name", result);
					app.addAppEdge(fog, edge, 10, 50, result, Tuple.DOWN, AppEdge.MODULE);

					// make edge -> act
					String act = "ACT" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					info.printTwoStringLog("Make edge(edge->act)", "left", sensor, "right", act);
					info.printOneStringLog("Tuple type", "name", "SELF_STATE_UPDATE");
					app.addAppEdge(edge, act, 50, 20, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);

				}
				// make fog -> cloud
				info.printTwoStringLog("Make edge(fog->cloud)", "left", fog, "right", cloud);
				info.printOneStringLog("Tuple type", "name", "USERS_STATE" + String.valueOf(x + 1));
				app.addAppEdge(fog, cloud, 50, 20, "USERS_STATE", Tuple.UP, AppEdge.MODULE);
			}
			break;
		case 1:
			for (int x = 0; x < 4; x++) {
				String appName = "class" + String.valueOf(x + 1);
				String fog = appName + "_fog";
				String cloud = appName + "_cloud";
				for (int i = 0; i < info.number_of_each_class.get(x); i++) {
					String sensor = "CAM" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					String edge = appName + "-" + String.valueOf(i);
					String cam_data = "CAM_CLASS" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					// make sensor -> edge
					info.printTwoStringLog("Make edge(sensor->edge)", "left", sensor, "right", edge);
					info.printOneStringLog("Tuple type", "name", cam_data);
					app.addAppEdge(cam_data, edge, 10, 50, cam_data, Tuple.UP, AppEdge.SENSOR);

					// make edge -> fog
					info.printTwoStringLog("Make edge(edge->fog)", "left", edge, "right", fog);
					info.printOneStringLog("Tuple type", "name",
							"DATA" + String.valueOf(x + 1) + "-" + String.valueOf(i));
					String data = "DATA" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					app.addAppEdge(edge, fog, 10, 50, data, Tuple.UP, AppEdge.MODULE);
					// make fog -> edge
					String result = "RESULT" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					info.printTwoStringLog("Make edge(fog->edge)", "left", fog, "right", edge);
					info.printOneStringLog("Tuple type", "name", result);
					if (appName.equals("class1")) {
						app.addAppEdge(fog, edge, 5000, 50, result, Tuple.DOWN, AppEdge.MODULE);
					} else if (appName.equals("class2")) {
						app.addAppEdge(fog, edge, 100, 50, result, Tuple.DOWN, AppEdge.MODULE);
					} else if (appName.equals("class3")) {
						app.addAppEdge(fog, edge, 2200, 50, result, Tuple.DOWN, AppEdge.MODULE);
					} else {
						app.addAppEdge(fog, edge, 30, 50, result, Tuple.DOWN, AppEdge.MODULE);
					}

					// make edge -> act
					String act = "ACT" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					info.printTwoStringLog("Make edge(edge->act)", "left", sensor, "right", act);
					info.printOneStringLog("Tuple type", "name", "SELF_STATE_UPDATE");
					app.addAppEdge(edge, act, 50, 20, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);

				}
				// make fog -> cloud
				info.printTwoStringLog("Make edge(fog->cloud)", "left", fog, "right", cloud);
				info.printOneStringLog("Tuple type", "name", "USERS_STATE" + String.valueOf(x + 1));
				app.addAppEdge(fog, cloud, 50, 20, "USERS_STATE", Tuple.UP, AppEdge.MODULE);
			}
			break;
		case 2:
			break;
		}
		Log.printLine("---------------------------------------");
		Log.printLine("---------------------------------------");

		// 4. make tuple mapping
		for (int x = 0; x < 4; x++) {
			String appName = "class" + String.valueOf(x + 1);
			String fog = appName + "_fog";
			String cloud = appName + "_cloud";
			for (int i = 0; i < info.number_of_each_class.get(x); i++) {
				String sensor = "CAM" + String.valueOf(x + 1) + "-" + String.valueOf(i);
				String edge = appName + "-" + String.valueOf(i);
				String data = "DATA" + String.valueOf(x + 1) + "-" + String.valueOf(i);
				String result = "RESULT" + String.valueOf(x + 1) + "-" + String.valueOf(i);
				String act = "ACT" + String.valueOf(x + 1) + "-" + String.valueOf(i);
				String cam_data = "CAM_CLASS" + String.valueOf(x + 1) + "-" + String.valueOf(i);
				// module name, input, output
				// mapping (edge) cam_data -> data
				info.printTwoStringLog("Make tuple mapping module:" + edge, "input", cam_data, "right", data);
				app.addTupleMapping(edge, cam_data, data, new FractionalSelectivity(1.0));

				// mapping (fog) data -> result
				info.printTwoStringLog("Make tuple mapping module:" + fog, "input", data, "right", result);
				app.addTupleMapping(fog, data, result, new FractionalSelectivity(1.0));

				// mapping (fog) data -> USERS_STATE
				info.printTwoStringLog("Make tuple mapping module:" + fog, "input", data, "right", "USERS_STATE");
				app.addTupleMapping(fog, data, "USERS_STATE", new FractionalSelectivity(1.0));

				// mapping (edge) result -> SELF_STATE_UPDATE
				info.printTwoStringLog("Make tuple mapping module:" + edge, "input", result, "right",
						"SELF_STATE_UPDATE");
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
			// mapping (cloud) USERS_STATE -> CLOUD_RESULT
			app.addTupleMapping(cloud, "USERS_STATE", "CLOUD_RESULT", new FractionalSelectivity(1.0));
		}
		app.setLoops(loops);
		app.setUserId(1);

		return app;
	}

	private static ModuleMapping createDevicesAndMapping(int userId, String[] appIds, int offloading_policy,
			ClassInfo info) {
		ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
		int class_num = info.CLASS_NUM - 1;
		// TODO: make fog,cloud devices configurable, now these fixed to 1
		// 1. make cloud device
		FogDevice cloud = createFogDevice("cloud", info.CLOUD_MIPS[class_num], 32000, info.CLOUD_UPBW[class_num],
				info.CLOUD_DOWNBW[class_num], 0, 0.01, 100, 0, info);
//		FogDevice cloud = createFogDevice("cloud", info.CLOUD_MIPS[class_num], 32000,
//				(info.CLOUD_UPBW[class_num] * info.NUMBER_OF_APPS >= info.CLOUD_MAXBW) ? info.CLOUD_MAXBW
//						: info.CLOUD_UPBW[class_num] * info.NUMBER_OF_APPS,
//				(info.CLOUD_DOWNBW[class_num] * info.NUMBER_OF_APPS >= info.CLOUD_MAXBW) ? info.CLOUD_MAXBW
//						: info.CLOUD_DOWNBW[class_num] * info.NUMBER_OF_APPS,
//				0, 0.01, 100, 0);

		cloud.setParentId(-1);
		fogDevices.add(cloud);

		// 2. make fog device
		// bandwidth(kb/s)
		FogDevice fog = createFogDevice("fog-layer", info.FOG_MIPS, 8000, info.FOG_UPBW[class_num],
				info.FOG_DOWNBW[class_num], 1, 0.0, 8, 0, info);
//		FogDevice fog = createFogDevice("fog-layer", info.FOG_MIPS, 8000,
//				(info.FOG_UPBW[class_num] * info.NUMBER_OF_APPS >= info.FOG_MAXBW) ? info.FOG_MAXBW
//						: info.FOG_UPBW[class_num] * info.NUMBER_OF_APPS,
//				(info.FOG_DOWNBW[class_num] * info.NUMBER_OF_APPS >= info.FOG_MAXBW) ? info.FOG_MAXBW
//						: info.FOG_DOWNBW[class_num] * info.NUMBER_OF_APPS,
//				1, 0.0, 8, 0);
		fog.setParentId(cloud.getId());
		fog.setUplinkLatency(info.FOG_TO_CLOUD_LATENCY);
		fogDevices.add(fog);

		// 3. make edge device
		for (int i = 0; i < info.numOfSensorNode; i++) {
			String sensorNodeId = "0-" + i;
			FogDevice sensorNode = createFogDevice("m-" + sensorNodeId, info.EDGE_MIPS[class_num], 1000,
					info.EDGE_UPBW[class_num], info.EDGE_DOWNBW[class_num], 2, 0, 1.0815, 0, info);
//			FogDevice sensorNode = createFogDevice("m-" + sensorNodeId, info.EDGE_MIPS[class_num], 1000,
//					(info.EDGE_UPBW[class_num] >= info.EDGE_MAXBW) ? info.EDGE_MAXBW : info.EDGE_UPBW[class_num],
//					(info.EDGE_DOWNBW[class_num] >= info.EDGE_MAXBW) ? info.EDGE_MAXBW : info.EDGE_DOWNBW[class_num], 2,
//					0, 1.0815, 0);
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
			// 4. make sensor,act devices
			ArrayList<Integer> device_idx = new ArrayList<Integer>() {
			};
			for (int x = 0; x < 4; x++)
				device_idx.add(0);

			for (int x = 0; x < 4; x++) {
				String appName = "class" + String.valueOf(x + 1);
				String fog_module = appName + "_fog";
				String cloud_module = appName + "_cloud";
				for (int i = 0; i < info.number_of_each_class.get(x); i++) {
					String sensor = "CAM" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					String edge = appName + "-" + String.valueOf(i);
					String data = "DATA" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					String result = "RESULT" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					String act = "ACT" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					String cam_data = "CAM_CLASS" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					String sensor_device_name = "s-" + String.valueOf(x + 1) + "-" + String.valueOf(i);

					// make sensors
					info.printOneStringLog("make sensor device :", "name", sensor_device_name);
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
					String act_device_name = "act-" + String.valueOf(x + 1) + "-" + String.valueOf(i);
					info.printOneStringLog("make actuator device :", "name", act_device_name);
					Actuator noti = new Actuator(act_device_name, userId, "multi_app", act);
					actuators.add(noti);

					// TODO: change fogDevices to Hashmap
					Integer idx = info.getKIndexofList(x, i, ClassInfo.random_idx);
					// 5. connect devices to edge

					sensor_device.setGatewayDeviceId(fogDevices.get(info.getWhichDevice(idx) + 2).getId());
					sensor_device.setLatency(info.SENSOR_TO_EDGE_LATENCY);
					noti.setGatewayDeviceId(fogDevices.get(info.getWhichDevice(idx) + 2).getId());
					noti.setLatency(0.05);
					moduleMapping.addModuleToDevice(edge, fogDevices.get(info.getWhichDevice(idx) + 2).getName());
					Log.printLine("sensor, noti :" + sensor_device.getName() + "," + noti.getName() + "->"
							+ sensor_device.getGatewayDeviceId());
				}
				moduleMapping.addModuleToDevice(cloud_module, "cloud");
				moduleMapping.addModuleToDevice(fog_module, "fog-layer");
			}
		}

		return moduleMapping;
	}

	private static ModuleMapping createFogDevices(int userId, String[] appIds, int offloading_policy, ClassInfo info) {

		int class_num = info.CLASS_NUM - 1;
		FogDevice cloud = createFogDevice("cloud", info.CLOUD_MIPS[class_num], 32000, 10000, 10000, 0, 0.01, 100, 0,
				info);
		cloud.setParentId(-1);
		fogDevices.add(cloud);

		// TODO: now number of fog-layer devices fixed at 1
		FogDevice fog = createFogDevice("fog-layer", info.FOG_MIPS, 8000, 1000, 1000, 1, 0.0, 8, 0, info);
		fog.setParentId(cloud.getId());
		fog.setUplinkLatency(info.FOG_TO_CLOUD_LATENCY);
		fogDevices.add(fog);

		for (int i = 0; i < info.numOfSensorNode; i++) {
			String sensorNodeId = "0-" + i;
			FogDevice sensorNode = createFogDevice("m-" + sensorNodeId, info.EDGE_MIPS[class_num], 1000, 1000, 1000, 2,
					0, 0.5665, 0, info);
			sensorNode.setParentId(fog.getId());
			sensorNode.setUplinkLatency(info.EDGE_TO_FOG_LATENCY);
			fogDevices.add(sensorNode);
		}
		ArrayList<Integer> apps = new ArrayList<Integer>() {
		};
		for (int x = 0; x < 4; x++)
			apps.add(0);
		int didx = 2;
		int i = 0;
		for (int x = 0; x < 4; x++) {
			i = 0;
			for (Integer app_idx : info.map_of_each_class.get(appIds[x])) {
				Sensor sensor = null;
				if (appIds[x].equals("class1")) {
					sensor = new Sensor("s-" + String.valueOf(app_idx + 1) + "-" + apps.get(app_idx),
							"CAM_class" + String.valueOf(app_idx + 1), userId, appIds[x],
							new DeterministicDistribution(info.CLASS1_TRANSMISSION_TIME));
				} else if (appIds[x].equals("class2")) {
					sensor = new Sensor("s-" + String.valueOf(app_idx + 1) + "-" + apps.get(app_idx),
							"CAM_class" + String.valueOf(app_idx + 1), userId, appIds[x],
							new DeterministicDistribution(info.CLASS2_TRANSMISSION_TIME));
				} else if (appIds[x].equals("class3")) {
					sensor = new Sensor("s-" + String.valueOf(app_idx + 1) + "-" + apps.get(app_idx),
							"CAM_class" + String.valueOf(app_idx + 1), userId, appIds[x],
							new DeterministicDistribution(info.CLASS3_TRANSMISSION_TIME));
				} else {
					sensor = new Sensor("s-" + String.valueOf(app_idx + 1) + "-" + apps.get(app_idx),
							"CAM_class" + String.valueOf(app_idx + 1), userId, appIds[x],
							new DeterministicDistribution(info.CLASS4_TRANSMISSION_TIME));
				}
				sensors.add(sensor);
				Actuator noti = new Actuator(appIds[x] + "_act-" + String.valueOf(app_idx + 1), userId, appIds[x],
						appIds[x] + "_ACT");
				actuators.add(noti);
				sensor.setGatewayDeviceId(fogDevices.get(didx).getId());
				sensor.setLatency(info.SENSOR_TO_EDGE_LATENCY);
				noti.setGatewayDeviceId(fogDevices.get(didx).getId());
				noti.setLatency(5.0);
				Log.printLine("Sensor : " + "s-" + String.valueOf(app_idx + 1) + "-" + apps.get(app_idx) + ", Device: "
						+ fogDevices.get(didx).getName() + ", Tuple: " + "CAM_class" + String.valueOf(app_idx + 1));
				apps.set(app_idx, apps.get(app_idx) + 1);
			}
			didx += 1;
		}

		for (int x = 0; x < 4; x++)
			apps.add(0);
		ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping
		for (int x = 0; x < 4; x++) {
			moduleMapping.addModuleToDevice("class" + String.valueOf(x + 1) + "_cloud", "cloud"); // fixing all
																									// instances of the
																									// Connector module
																									// to the Cloud
			moduleMapping.addModuleToDevice("class" + String.valueOf(x + 1) + "_fog", "fog-layer"); // fixing all
																									// instances of the
																									// Concentration
																									// Calculator module
																									// to the Cloud
		}
		apps.clear();
		for (int x = 0; x < 4; x++)
			apps.add(0);
		for (int x = 0; x < 4; x++) {
			for (Integer app_idx : info.map_of_each_class.get(appIds[x])) {
				for (FogDevice device : fogDevices) {
					if (device.getName().startsWith("m")) {
						String[] temps = device.getName().split("-");
						String last = temps[temps.length - 1];
						if (Integer.valueOf(last) == x) {
							Log.printLine("module name : " + "class" + String.valueOf(app_idx + 1) + "-"
									+ Integer.valueOf(apps.get(app_idx)) + "-> " + device.getName());
							moduleMapping.addModuleToDevice(
									"class" + String.valueOf(app_idx + 1) + "-" + Integer.valueOf(apps.get(app_idx)),
									device.getName());
							apps.set(app_idx, apps.get(app_idx) + 1);
						}
					}
				}
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