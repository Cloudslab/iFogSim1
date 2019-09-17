package org.fog.test.perfeval;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.cloudbus.cloudsim.Log;
import org.fog.utils.Config;

import com.google.common.primitives.Ints;

/**
 * Simulation setup for Multi class Applications on each device
 * 
 * @author DongJoo Seo based on VRGameFog example
 */

class ClassInfo {
	static HashMap<String, ArrayList<Integer>> map_of_each_class = null;
	// [string:[1,2],...]
	static String[] name_of_classes = { "class1", "class2", "class3", "class4" };
	static ArrayList<Integer> number_of_each_class = null;
	// [number_of_c1,...]
	static ArrayList<Integer> device_idx = null;
	// just idx
	static List<Integer> random_idx = null;

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

	static int USING_EXECUTION_MAP = -1;

	static String ratio = "";

	static int ENABLE_LOG = 0;

	static int OFFLOADING_POLICY = -1;
	static int CLASS_NUM = -1;
	static int SINGLE_APP = 0;
	static int CLASS1_MIPS = 48000;
	static int CLASS2_MIPS = 4835;
	static int CLASS3_MIPS = 1000000;
	static int CLASS4_MIPS = 3062;
	static int CLOUD_MIPS = 225000;
	static int FOG_MIPS = 84000;
	static int EDGE_MIPS = 2225;

	// class3
	static long EDGE_UPBW = 2187000;
	static long EDGE_DOWNBW = 1953000;
	static long FOG_UPBW = 1000000;
	static long FOG_DOWNBW = 1000000;
	static long CLOUD_UPBW = 1500000;
	static long CLOUD_DOWNBW = 150000;

	static double CLASS1_INPUT_SIZE = 164000 / 1024;
	static double CLASS1_OUTPUT_SIZE = 161 / 1024;
	static double CLASS2_INPUT_SIZE = 14000000 / 1024;
	static double CLASS2_OUTPUT_SIZE = 615000 / 1024;
	static double CLASS3_INPUT_SIZE = 8200000 / 1024;
	static double CLASS3_OUTPUT_SIZE = 8200000 / 1024;
	static double CLASS4_INPUT_SIZE = 146000 / 1024;
	static double CLASS4_OUTPUT_SIZE = 14800000 / 1024;

	static int CLOUD_NETWORK = -1;
	static int PACKET_LOSS = -1;

	static double SENSOR_TO_EDGE_LATENCY = 50;
	static double EDGE_TO_FOG_LATENCY = 100;
	static double FOG_TO_CLOUD_LATENCY = 1000;

	static int EDGE_MAXBW = 1000000;
	static int FOG_MAXBW = 1000000;
	static int CLOUD_MAXBW = 10000000;

	static int using_fresult = -1;

	static Object[] configs = new Object[] {};

	public static ArrayList<Integer> getDevice_idx() {
		return device_idx;
	}

	public static int randomVarible(List<Integer> list) {
		Random rand = new Random();
		return list.get(rand.nextInt(list.size()));
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

	private static Object[] appendValue(Object[] obj, Object newObj) {
		ArrayList<Object> temp = new ArrayList<Object>(Arrays.asList(obj));
		temp.add(newObj);
		return temp.toArray();

	}

	public static List<String> readFile(String filename) {
		List<String> records = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = reader.readLine()) != null) {
				records.add(line);
			}
			reader.close();
			return records;
		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", filename);
			e.printStackTrace();
			return null;
		}
	}

	public static void setFogPacketLossAndCloudNetwork() {
		switch (ClassInfo.PACKET_LOSS) {
		case 5:
			ClassInfo.EDGE_UPBW = 1016;
			ClassInfo.FOG_DOWNBW = 906;
			break;
		case 10:
			ClassInfo.EDGE_UPBW = 309;
			ClassInfo.FOG_DOWNBW = 276;
			break;
		case 15:
			ClassInfo.EDGE_UPBW = 99;
			ClassInfo.FOG_DOWNBW = 88;
			break;
		case 20:
			ClassInfo.EDGE_UPBW = 51;
			ClassInfo.FOG_DOWNBW = 45;
			break;
		case 25:
			ClassInfo.EDGE_UPBW = 30;
			ClassInfo.FOG_DOWNBW = 27;
			break;
		case 30:
			ClassInfo.EDGE_UPBW = 16;
			ClassInfo.FOG_DOWNBW = 14;
			break;
		default:
			ClassInfo.EDGE_UPBW = 2187;
			ClassInfo.FOG_DOWNBW = 1953;
			break;
		}

		switch (ClassInfo.CLOUD_NETWORK) {
		case 0:
			ClassInfo.FOG_UPBW = 1280;
			ClassInfo.CLOUD_DOWNBW = 1250;
			break;
		case 1:
			ClassInfo.FOG_UPBW = 650;
			ClassInfo.CLOUD_DOWNBW = 1400;
			break;
		case 2:
			ClassInfo.FOG_UPBW = 133;
			ClassInfo.CLOUD_DOWNBW = 260;
			break;
		default:
			break;
		}
	}

	public static void openExecutionMapFile(String filepath) {
		List<String> t = readFile(filepath);
		for (String line : t) {
//			System.out.println(line);
			// line -> 1,2,3,4,5,6,7,8
		}
	}
	// open config file for multiple simulation or configuable simulation

	public static void openConfigFile(String filepath, String filepath_map) {
		List<String> t = readFile(filepath);

		Log.printLine("start to read config file");
		// output path
		String[] origin_data = t.get(0).split("=");
		String value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, value);

		// app name
		origin_data = t.get(1).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, value);

		// number of edges
		origin_data = t.get(2).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Integer.valueOf(value));
		numOfSensorNode = Integer.valueOf(value);

		// using file result
		origin_data = t.get(3).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Integer.valueOf(value));

		using_fresult = (int) configs[3];

		// number of apps
		origin_data = t.get(4).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Integer.valueOf(value));
		NUMBER_OF_APPS = (int) configs[4];

		// ratio
		origin_data = t.get(5).split("=");
		value = origin_data[origin_data.length - 1];
		ratio = String.valueOf(value);
		// System.out.println(ratio);
		setNumberOfApps(ratio);

		origin_data = t.get(6).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Integer.valueOf(value));
		ENABLE_LOG = Integer.valueOf(value);

		origin_data = t.get(7).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Double.valueOf(value));
		CLASS1_TRANSMISSION_TIME = Double.valueOf(value);

		origin_data = t.get(8).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Double.valueOf(value));
		CLASS2_TRANSMISSION_TIME = Double.valueOf(value);

		origin_data = t.get(9).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Double.valueOf(value));
		CLASS3_TRANSMISSION_TIME = Double.valueOf(value);

		origin_data = t.get(10).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Double.valueOf(value));
		CLASS4_TRANSMISSION_TIME = Double.valueOf(value);

		origin_data = t.get(11).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Double.valueOf(value));
		SENSOR_TO_EDGE_LATENCY = Double.valueOf(value);

		origin_data = t.get(12).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Double.valueOf(value));
		EDGE_TO_FOG_LATENCY = Double.valueOf(value);

		origin_data = t.get(13).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Double.valueOf(value));
		FOG_TO_CLOUD_LATENCY = Double.valueOf(value);

		origin_data = t.get(14).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Integer.valueOf(value));
		OFFLOADING_POLICY = Integer.valueOf(value);

		origin_data = t.get(15).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Integer.valueOf(value));
		CLASS_NUM = Integer.valueOf(value);

		origin_data = t.get(16).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Integer.valueOf(value));
		SINGLE_APP = Integer.valueOf(value);

		origin_data = t.get(17).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Integer.valueOf(value));
		CLOUD_NETWORK = Integer.valueOf(value);

		origin_data = t.get(18).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Integer.valueOf(value));
		PACKET_LOSS = Integer.valueOf(value);
		setFogPacketLossAndCloudNetwork();
		openExecutionMapFile(filepath_map);
	}

	private static void setNumberOfApps(String ratio) {
		String[] ratios = ratio.split(",");
		Float total = 0.0f;
		Float mul = 0.0f;

		for (String i : ratios)
			total += Integer.valueOf(i);
		mul = NUMBER_OF_APPS / total;
		NUMBER_OF_CLASS1 = Integer.valueOf(ratios[0]) * mul.intValue();
		NUMBER_OF_CLASS2 = Integer.valueOf(ratios[1]) * mul.intValue();
		NUMBER_OF_CLASS3 = Integer.valueOf(ratios[2]) * mul.intValue();
		NUMBER_OF_CLASS4 = Integer.valueOf(ratios[3]) * mul.intValue();

	}

	public static void setVariables() {
		setNumber_of_each_class(new ArrayList<Integer>());
		setDevice_idx(new ArrayList<Integer>());
		setMap_of_each_class(new HashMap<String, ArrayList<Integer>>());
		List<Integer> device_idx = new ArrayList() {
		};
		for (int x = 0; x < numOfSensorNode; x++) {
			device_idx.add(x);
		}
		random_idx = new ArrayList<Integer>() {
		};
		for (int x = 0; x < NUMBER_OF_APPS; x++) {
			random_idx.add(randomVarible(device_idx));
		}
		List<Integer> tmp = new ArrayList<Integer>() {
		};

		List<Integer> class1_map = new ArrayList() {
		};
		for (int x = 0; x < NUMBER_OF_CLASS1; x++) {
			class1_map.add(random_idx.get(0));
			tmp.add(random_idx.get(0));
			random_idx.remove(0);
		}
		List<Integer> class2_map = new ArrayList() {
		};
		for (int x = 0; x < NUMBER_OF_CLASS2; x++) {
			class2_map.add(random_idx.get(0));
			tmp.add(random_idx.get(0));
			random_idx.remove(0);
		}
		List<Integer> class3_map = new ArrayList() {
		};
		for (int x = 0; x < NUMBER_OF_CLASS3; x++) {
			class3_map.add(random_idx.get(0));
			tmp.add(random_idx.get(0));
			random_idx.remove(0);
		}
		List<Integer> class4_map = new ArrayList() {
		};
		for (int x = 0; x < NUMBER_OF_CLASS4; x++) {
			class4_map.add(random_idx.get(0));
			tmp.add(random_idx.get(0));
			random_idx.remove(0);
		}
		random_idx = tmp;
		int[] test_map[] = { Ints.toArray(class1_map), Ints.toArray(class2_map), Ints.toArray(class3_map),
				Ints.toArray(class4_map) };
		for (int x = 0; x < 4; x++) {
			map_of_each_class.put(MultiClassApp.appIds[x],
					(ArrayList<Integer>) Arrays.stream(test_map[x]).boxed().collect(Collectors.toList()));
		}

		for (int i = 0; i < numOfSensorNode; i++)
			number_of_each_class.add(0);

		for (String app : MultiClassApp.appIds) {
			Log.printLine(map_of_each_class.get(app));
		}

		for (int[] each : test_map) {
			for (int idx : each) {
				Integer a = number_of_each_class.get(idx) + 1;
				number_of_each_class.remove(idx);
				number_of_each_class.add(idx, a);
			}
		}
		Log.printLine(number_of_each_class);

	}
}