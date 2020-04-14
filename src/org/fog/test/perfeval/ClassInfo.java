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

	static int using_res_map = 0;
	// data[class_num][packet_loss][num_of_instance]
	static int[][][] res_map = {
			{ { 2, 2, 2, 2, 2, 2, 2, 2 }, { 2, 2, 2, 2, 2, 2, 2, 2 }, { 2, 2, 2, 2, 2, 2, 2, 2 },
					{ 2, 2, 2, 2, 2, 2, 2, 2 }, { 2, 2, 2, 2, 2, 2, 2, 2 }, { 2, 2, 2, 2, 2, 2, 2, 2 },
					{ 2, 2, 2, 2, 2, 2, 2, 2 }, { 2, 2, 2, 2, 2, 2, 2, 2 }, { 2, 2, 2, 2, 2, 2, 2, 2 },
					{ 2, 2, 2, 2, 2, 2, 2, 2 } },
			{ { 1, 1, 1, 0, 0, 0, 0, 0 }, { 1, 1, 0, 0, 0, 0, 0, 0 }, { 1, 1, 0, 0, 0, 0, 0, 0 },
					{ 1, 0, 0, 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0, 0, 0 },
					{ 1, 0, 0, 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0, 0, 0 },
					{ 1, 0, 0, 0, 0, 0, 0, 0 } },
			{ { 1, 1, 1, 1, 1, 1, 0, 0 }, { 2, 2, 2, 2, 2, 0, 0, 0 }, { 2, 2, 2, 2, 2, 0, 0, 0 },
					{ 2, 2, 2, 2, 0, 0, 0, 0 }, { 2, 2, 2, 2, 0, 0, 0, 0 }, { 2, 2, 2, 2, 0, 0, 0, 0 },
					{ 2, 2, 2, 0, 0, 0, 0, 0 }, { 2, 2, 2, 0, 0, 0, 0, 0 }, { 2, 2, 2, 0, 0, 0, 0, 0 },
					{ 2, 2, 2, 0, 0, 0, 0, 0 } },
			{ { 1, 1, 1, 0, 0, 0, 0, 0 }, { 1, 1, 1, 0, 0, 0, 0, 0 }, { 1, 1, 1, 0, 0, 0, 0, 0 },
					{ 1, 1, 1, 0, 0, 0, 0, 0 }, { 1, 1, 1, 0, 0, 0, 0, 0 }, { 1, 1, 1, 0, 0, 0, 0, 0 },
					{ 1, 1, 1, 0, 0, 0, 0, 0 }, { 1, 1, 1, 0, 0, 0, 0, 0 }, { 1, 1, 1, 0, 0, 0, 0, 0 },
					{ 1, 1, 1, 0, 0, 0, 0, 0 } }

	};
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
	public static int CLASS_NUM = -1;
	static int SINGLE_APP = 0;
	public static int CLASS1_MIPS = 3290000;
	public static int CLASS2_MIPS = 103657;
	public static int CLASS3_MIPS = 1000000;
	public static Double CLASS4_MIPS = 59000.0;
	//static int CLOUD_MIPS[] = { 727000, 275000, 225000, 225000 };
	static Double CLOUD_MIPS[] = { 727000.0, 225000.0, 225000.0, 225000.0 };
	static Double FOG_MIPS = 84000.0;
	//static int EDGE_MIPS[] = { 2636, 2280, 2225, 2225 };
	static Double EDGE_MIPS[] = { 2636.0, 2225.0, 2225.0, 2225.0 };
	
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
	
	static double FOG_APP_MIPS = 0;
	static int CLOUD_NETWORK = -1;
	public static int PACKET_LOSS = -1;

	public static double SENSOR_TO_EDGE_LATENCY = 50;
	public static double EDGE_TO_FOG_LATENCY = 100;
	public static double FOG_TO_CLOUD_LATENCY = 1000;

	public static List<Integer> RUNNING_REGION = new ArrayList<Integer>();

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

	// 4 7
	// [class_num][packet_loss]
	public static double EDGE_UP_BW_ALL_CLASS[][] = {
			{ 2675.428779, 2241.101014, 1101.790382, 372.8516181, 211.8471157, 144.0680162, 82.35566685, 54.03987559 },
			{ 9751.371281, 1172.833778, 357.8299514, 126.6433519, 56.02994085, 31.18328822, 18.76013232, 9.529634028 },
			{ 8451.608133, 963.6907877, 364.8351037, 134.0693566, 71.20281387, 39.78138099, 20.32544897, 11.73910663 },
			{ 2541.263685, 1855.318449, 468.5175955, 271.406657, 242.2419099, 137.7661747, 52.5864096, 66.61914306 } };
	public static double FOG_DOWN_BW_ALL_CLASS[][] = {
			{ 297.6648287, 252.7351913, 267.8020141, 296.6538915, 312.2672542, 250.4804246, 275.1120954, 271.9241828 },
			{ 4896.07569, 1995.689321, 1265.603496, 594.1109598, 389.4335991, 210.8467187, 123.4011969, 60.7593139 },
			{ 4363.968093, 1629.208241, 1013.226971, 375.1728756, 242.5775889, 168.0645707, 106.8656681, 63.36929717 },
			{ 8205.882611, 1499.198459, 830.5834582, 420.4083472, 242.894509, 125.8441799, 74.14563561, 39.55754545 } };

	public static double VIRT_EDGE_UP_BW_ALL_CLASS[][] = {
			{ 2675.428779, 2675.428779, 2675.428779, 2675.428779, 2675.428779, 2675.428779, 2675.428779, 2675.428779 },
			{ 9751.371281, 9751.371281, 9751.371281, 9751.371281, 9751.371281, 9751.371281, 9751.371281, 9751.371281 },
			{ 8451.608133, 8451.608133, 8451.608133, 8451.608133, 8451.608133, 8451.608133, 8451.608133, 8451.608133 },
			{ 2541.263685, 2541.263685,2541.263685,2541.263685, 2541.263685, 2541.263685,2541.263685,2541.263685 } };
	public static double VIRT_FOG_DOWN_BW_ALL_CLASS[][] = {
			{ 297.6648287, 297.6648287,297.6648287,297.6648287,297.6648287,297.6648287,297.6648287,297.6648287, },
			{ 4896.07569,4896.07569,4896.07569,4896.07569,4896.07569,4896.07569,4896.07569,4896.07569, },
			{ 4363.968093, 4363.968093,4363.968093,4363.968093,4363.968093,4363.968093,4363.968093,4363.968093, },
			{ 8205.882611,8205.882611,8205.882611,8205.882611,8205.882611,8205.882611,8205.882611,8205.882611, } };

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
		if(class_num == 4)
			class_num = 3;
		ClassInfo.EDGE_UPBW[class_num] = VIRT_EDGE_UP_BW_ALL_CLASS[class_num][0];
		ClassInfo.FOG_DOWNBW[class_num] = VIRT_FOG_DOWN_BW_ALL_CLASS[class_num][0];
		switch (ClassInfo.PACKET_LOSS) {
		case 2:
			ClassInfo.EDGE_UPBW[class_num] = VIRT_EDGE_UP_BW_ALL_CLASS[class_num][1];
			ClassInfo.FOG_DOWNBW[class_num] = VIRT_FOG_DOWN_BW_ALL_CLASS[class_num][1];
			break;
		case 4:
			ClassInfo.EDGE_UPBW[class_num] = VIRT_EDGE_UP_BW_ALL_CLASS[class_num][2];
			ClassInfo.FOG_DOWNBW[class_num] = VIRT_FOG_DOWN_BW_ALL_CLASS[class_num][2];
			break;
		case 6:
			ClassInfo.EDGE_UPBW[class_num] = VIRT_EDGE_UP_BW_ALL_CLASS[class_num][3];
			ClassInfo.FOG_DOWNBW[class_num] = VIRT_FOG_DOWN_BW_ALL_CLASS[class_num][3];
			break;
		case 8:
			ClassInfo.EDGE_UPBW[class_num] = VIRT_EDGE_UP_BW_ALL_CLASS[class_num][4];
			ClassInfo.FOG_DOWNBW[class_num] = VIRT_FOG_DOWN_BW_ALL_CLASS[class_num][4];
			break;
		case 10:
			ClassInfo.EDGE_UPBW[class_num] = VIRT_EDGE_UP_BW_ALL_CLASS[class_num][5];
			ClassInfo.FOG_DOWNBW[class_num] = VIRT_FOG_DOWN_BW_ALL_CLASS[class_num][5];
			break;
		case 12:
			ClassInfo.EDGE_UPBW[class_num] = VIRT_EDGE_UP_BW_ALL_CLASS[class_num][6];
			ClassInfo.FOG_DOWNBW[class_num] = VIRT_FOG_DOWN_BW_ALL_CLASS[class_num][6];
			break;
		case 14:
			ClassInfo.EDGE_UPBW[class_num] = VIRT_EDGE_UP_BW_ALL_CLASS[class_num][7];
			ClassInfo.FOG_DOWNBW[class_num] = VIRT_FOG_DOWN_BW_ALL_CLASS[class_num][7];
			break;
		default:
			ClassInfo.EDGE_UPBW[class_num] = VIRT_EDGE_UP_BW_ALL_CLASS[class_num][0];
			ClassInfo.FOG_DOWNBW[class_num] = VIRT_FOG_DOWN_BW_ALL_CLASS[class_num][0];
			break;
		}

		ClassInfo.FOG_UPBW[class_num] = FOG_UP_BW_ALL_CLASS[class_num][CLOUD_NETWORK];
		ClassInfo.CLOUD_DOWNBW[class_num] = CLOUD_DOWN_BW_ALL_CLASS[class_num][CLOUD_NETWORK];
	}

	public static void openConfigFile(String filepath) {
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
		configs = appendValue(configs, Double.valueOf(value));
		PACKET_LOSS = Integer.valueOf(value);

		origin_data = t.get(19).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Integer.valueOf(value));
		Config.MAX_SIMULATION_TIME = Integer.valueOf(value);

		origin_data = t.get(20).split("=");
		value = origin_data[origin_data.length - 1];
		configs = appendValue(configs, Integer.valueOf(value));
		using_res_map = Integer.valueOf(value);

		origin_data = t.get(21).split("=");
		value = origin_data[origin_data.length - 1];
		String[] runs = value.split(",");
		for (int x = 0; x < 3; x++) {
			RUNNING_REGION.add(x, Integer.valueOf(runs[x]));
		}
		
		origin_data = t.get(22).split("=");
		value = origin_data[origin_data.length - 1];		
		CLASS4_MIPS = Double.valueOf(value);
		
		origin_data = t.get(23).split("=");
		value = origin_data[origin_data.length - 1];
		CLOUD_MIPS[3] = Double.valueOf(value);
		
		origin_data = t.get(24).split("=");
		value = origin_data[origin_data.length - 1];
		FOG_MIPS = Double.valueOf(value);

		origin_data = t.get(25).split("=");
		value = origin_data[origin_data.length - 1];
		EDGE_MIPS[3] = Double.valueOf(value);
		
		origin_data = t.get(26).split("=");
		value = origin_data[origin_data.length - 1];
		FOG_APP_MIPS = Double.valueOf(value);
		setFogPacketLossAndCloudNetwork();
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
		System.out.println(idx_in_r);
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