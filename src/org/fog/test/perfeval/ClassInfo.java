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

public class ClassInfo {
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

	public static int NUMBER_OF_APPS = 0;
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
	public static int CLASS1_MIPS = 3290000;
	public static int CLASS2_MIPS = 105840;
	public static int CLASS3_MIPS = 1000000;
	public static int CLASS4_MIPS = 59000;
	static int CLOUD_MIPS[] = { 727000, 275000, 225000, 225000 };
	static int FOG_MIPS = 84000;
	static int EDGE_MIPS[] = { 2636, 2280, 2225, 2225 };

	// class3
	static double EDGE_UPBW[] = { 2187000, 2187000, 2187000, 2187000 };
	static double EDGE_DOWNBW[] = { -1, -1, -1, -1 };
	static double FOG_UPBW[] = { -1, -1, -1, -1 };
	static double FOG_DOWNBW[] = { -1, -1, -1, -1 };
	static double CLOUD_UPBW[] = { -1, -1, -1, -1 };
	static double CLOUD_DOWNBW[] = { 1500000, 1500000, 1500000, 1500000 };

	static double CLASS1_INPUT_SIZE = 164000 / 1024;
	static double CLASS1_OUTPUT_SIZE = 161 / 1024;
	static double CLASS2_INPUT_SIZE = 14000000 / 1024;
	static double CLASS2_OUTPUT_SIZE = 615000 / 1024;
	static double CLASS3_INPUT_SIZE = 8200000 / 1024;
	static double CLASS3_OUTPUT_SIZE = 8200000 / 1024;
	static double CLASS4_INPUT_SIZE = 145536 / 1024;
	static double CLASS4_OUTPUT_SIZE = 13846510 / 1024;

	static int CLOUD_NETWORK = -1;
	public static int PACKET_LOSS = -1;

	static double SENSOR_TO_EDGE_LATENCY = 50;
	static double EDGE_TO_FOG_LATENCY = 100;
	static double FOG_TO_CLOUD_LATENCY = 1000;

	static int EDGE_MAXBW = 1250000;

	public static int getEDGE_MAXBW() {
		return EDGE_MAXBW;
	}

	public static void setEDGE_MAXBW(int eDGE_MAXBW) {
		EDGE_MAXBW = eDGE_MAXBW;
	}

	public static int getFOG_MAXBW() {
		return FOG_MAXBW;
	}

	public static void setFOG_MAXBW(int fOG_MAXBW) {
		FOG_MAXBW = fOG_MAXBW;
	}

	public static int getCLOUD_MAXBW() {
		return CLOUD_MAXBW;
	}

	public static void setCLOUD_MAXBW(int cLOUD_MAXBW) {
		CLOUD_MAXBW = cLOUD_MAXBW;
	}

	public static double[][] FOG_ALPHA = new double[][] { { 19.771, 12.213 }, { 0.6121, 0.424 }, { 10.861, 0.3797 },
			{ 0.0545, 0.04953 } };
	public static double[][] CLOUD_ALPHA = new double[][] { { 1.6374, 3.8421 }, { 0.0639, 0.3045 }, { 1.8113, 2.0932 },
			{ 0.0354, 0.254 } };
	static int FOG_MAXBW = 1250000;
	static int CLOUD_MAXBW = 10000000;

	static int using_fresult = -1;
	// class1
	// Upload throughput 10009.76563 8897.569444 6159.855769 4106.570513 544.7491497
	// 306.8127395 113.5859929
	// download throughput 180.7201868 174.6961806 112.3046875 74.86979167
	// 9.88846305 5.555708922 2.05256609

	// class2
	// Upload throughput 2902.733546 1660.618851 1088.52508 385.0147846 179.6094982
	// 59.86458972 51.54918558
	// download throughput 889.7569444 508.9711335 333.6588542 117.993308
	// 55.04912351 18.34970784 15.800735

	// class3
	// Upload throughput 2187.926913 1016.21986 309.4208849 99.67404157 51.00517516
	// 30.51292676 16.5436999
	// download throughput 1953.125 906.8870328 276.1314655 88.97569444 45.39576247
	// 27.23745748 14.82928241

	// class4
	// Upload throughput 14851.88802 6789.434524 1679.36543 720.0915404 412.0755058
	// 226.6742846 138.0233543
	// download throughput 1775.568182 782.9428494 200.7378472 86.03050595
	// 49.16028912 27.06577715 16.49900114

	// 4 7
	// [class_num][packet_loss]
	static double EDGE_UP_BW_ALL_CLASS[][] = { { 10009, 8897, 6159, 4106, 544, 306, 113 },
			{ 2902, 1660, 1088, 385, 179, 59, 51 }, { 2187, 1016, 309, 99, 51, 30, 16 },
			{ 14851, 6789, 1679, 720, 412, 226, 138 } };
	static double FOG_DOWN_BW_ALL_CLASS[][] = { { 180, 174, 112, 74, 9, 5, 2 }, { 889, 508, 333, 117, 55, 18, 15 },
			{ 1953, 906, 276, 88, 45, 27, 14 }, { 1775, 782, 200, 86, 49, 27, 16 } };

//	Upload throughput	125.1220703	586.6529304	625.6103516
//	download throughput	0.1572265625	1.965332031	2.382220644

//	Upload throughput	126.5914352	613.088565	2059.017319
//	download throughput	250.2441406	734.2126375	786.107248

//	Upload throughput	133.4635417	657.996097	1542.931118
//	download throughput	263.3282637	1407.348418	999.7269039

//	Upload throughput	123.9809783	617.2213203	720.0915404
//	download throughput	267.4523501	936.6898898	1790.969641
	// 4 3
	// [class_num][network type]
	static double FOG_UP_BW_ALL_CLASS[][] = { { 625, 586, 125 }, { 2059.017319, 613.088565, 126.5914352 },
			{ 1280, 650, 133 }, { 720.0915404, 617.2213203, 123.9809783 } };
	static double CLOUD_DOWN_BW_ALL_CLASS[][] = { { 2.38, 1.96, 0.15 }, { 786.107248, 734.2126375, 250.2441406 },
			{ 1250, 1400, 260 }, { 1790.969641, 936.6898898, 267.4523501 } };

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
		int class_num = CLASS_NUM - 1;
		ClassInfo.EDGE_UPBW[class_num] = EDGE_UP_BW_ALL_CLASS[class_num][0];
		ClassInfo.FOG_DOWNBW[class_num] = FOG_DOWN_BW_ALL_CLASS[class_num][0];
//		switch (ClassInfo.PACKET_LOSS) {
//		case 5:
//			ClassInfo.EDGE_UPBW[class_num] = EDGE_UP_BW_ALL_CLASS[class_num][1];
//			ClassInfo.FOG_DOWNBW[class_num] = FOG_DOWN_BW_ALL_CLASS[class_num][1];
//			break;
//		case 10:
//			ClassInfo.EDGE_UPBW[class_num] = EDGE_UP_BW_ALL_CLASS[class_num][2];
//			ClassInfo.FOG_DOWNBW[class_num] = FOG_DOWN_BW_ALL_CLASS[class_num][2];
//			break;
//		case 15:
//			ClassInfo.EDGE_UPBW[class_num] = EDGE_UP_BW_ALL_CLASS[class_num][3];
//			ClassInfo.FOG_DOWNBW[class_num] = FOG_DOWN_BW_ALL_CLASS[class_num][3];
//			break;
//		case 20:
//			ClassInfo.EDGE_UPBW[class_num] = EDGE_UP_BW_ALL_CLASS[class_num][4];
//			ClassInfo.FOG_DOWNBW[class_num] = FOG_DOWN_BW_ALL_CLASS[class_num][4];
//			break;
//		case 25:
//			ClassInfo.EDGE_UPBW[class_num] = EDGE_UP_BW_ALL_CLASS[class_num][5];
//			ClassInfo.FOG_DOWNBW[class_num] = FOG_DOWN_BW_ALL_CLASS[class_num][5];
//			break;
//		case 30:
//			ClassInfo.EDGE_UPBW[class_num] = EDGE_UP_BW_ALL_CLASS[class_num][6];
//			ClassInfo.FOG_DOWNBW[class_num] = FOG_DOWN_BW_ALL_CLASS[class_num][6];
//			break;
//		default:
//			ClassInfo.EDGE_UPBW[class_num] = EDGE_UP_BW_ALL_CLASS[class_num][0];
//			ClassInfo.FOG_DOWNBW[class_num] = FOG_DOWN_BW_ALL_CLASS[class_num][0];
//			break;
//		}

		ClassInfo.FOG_UPBW[class_num] = FOG_UP_BW_ALL_CLASS[class_num][CLOUD_NETWORK];
		ClassInfo.CLOUD_DOWNBW[class_num] = CLOUD_DOWN_BW_ALL_CLASS[class_num][CLOUD_NETWORK];
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

		origin_data = t.get(19).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Integer.valueOf(value));
		Config.MAX_SIMULATION_TIME = Integer.valueOf(value);

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

	public static void printTwoStringLog(String Purpose, String left, String l1, String right, String r1) {
		Log.printLine("[" + Purpose + "] " + left + ": " + l1 + " ," + right + " : " + r1);
	}

	public static void printOneStringLog(String Purpose, String left, String l1) {
		Log.printLine("[" + Purpose + "] " + left + ": " + l1);
	}

	public static Integer getKIndexofList(Integer k, Integer how_many, List<Integer> li) {
		Integer total = 0;
		Integer z = 0;
		Integer i = 0;
		List<Integer> temp = li;
		while (true) {
			z = temp.indexOf(k);
			if (i == how_many) {
				total += z;
				return total;
			} else {
				temp = temp.subList(z + 1, temp.size());
				total += z;
				total++;
				i++;
				continue;
			}
		}
	}

	public static Integer getWhichDevice(Integer idx_in_r) {
		Integer z = idx_in_r;
		if (z < ClassInfo.NUMBER_OF_CLASS1) {
			return 0;
		} else if (z >= ClassInfo.NUMBER_OF_CLASS1 && z < ClassInfo.NUMBER_OF_CLASS1 + ClassInfo.NUMBER_OF_CLASS2) {
			return 1;
		} else if (z >= ClassInfo.NUMBER_OF_CLASS1 + ClassInfo.NUMBER_OF_CLASS2
				&& z < ClassInfo.NUMBER_OF_CLASS1 + ClassInfo.NUMBER_OF_CLASS2 + ClassInfo.NUMBER_OF_CLASS3) {
			return 2;
		} else if (z >= ClassInfo.NUMBER_OF_CLASS1 + ClassInfo.NUMBER_OF_CLASS2 + ClassInfo.NUMBER_OF_CLASS3
				&& z < ClassInfo.NUMBER_OF_CLASS1 + ClassInfo.NUMBER_OF_CLASS2 + ClassInfo.NUMBER_OF_CLASS3
						+ ClassInfo.NUMBER_OF_CLASS4) {
			return 3;
		} else {
			return -1;
		}
	}

}