package org.fog.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.dsp.Controller;
import org.fog.dsp.StreamQuery;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.FogDeviceCollector;
import org.fog.entities.Sensor;
import org.fog.entities.StreamOperator;
import org.fog.policy.StreamOperatorAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.scheduler.TupleScheduler;
import org.fog.utils.FogUtils;
import org.fog.utils.GeoCoverage;
import org.fog.utils.OperatorEdge;

public class LargeSimulation {

	static int sensorTupleCpuSize = 100;
	static int sensorTupleNwSize = 1000;
	public static void main(String[] args) {

		Log.printLine("Starting FogTest...");

		try {
			Log.disable();
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			CloudSim.init(num_user, calendar, trace_flag);

			String queryId = "query";
			
			FogBroker broker = new FogBroker("broker");
			
			int transmitInterval = 50;

			StreamQuery query = createStreamQuery(queryId, broker.getId(), transmitInterval);

			List<FogDevice> fogDevices = createFogDevices(broker.getId());
			
			
			for(int i=0;i<8;i++){
				for(int j=0;j<8;j++){
					int id = i*8+j;
					createSensor("sensor-TYPE-"+i+"-"+j+"-0", query, broker.getId(), CloudSim.getEntityId("gateway-"+id), transmitInterval, sensorTupleCpuSize, sensorTupleNwSize);
					createSensor("sensor-TYPE-"+i+"-"+j+"-1", query, broker.getId(), CloudSim.getEntityId("gateway-"+id), transmitInterval, sensorTupleCpuSize, sensorTupleNwSize);
					createSensor("sensor-TYPE-"+i+"-"+j+"-2", query, broker.getId(), CloudSim.getEntityId("gateway-"+id), transmitInterval, sensorTupleCpuSize, sensorTupleNwSize);
					createSensor("sensor-TYPE-"+i+"-"+j+"-3", query, broker.getId(), CloudSim.getEntityId("gateway-"+id), transmitInterval, sensorTupleCpuSize, sensorTupleNwSize);
				}
			}

			Controller controller = new Controller("master-controller", fogDevices);
			controller.submitStreamQuery(query, 0);
			
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			Log.printLine("CloudSimExample1 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	private static void createSensor(String sensorName, StreamQuery query, int userId, int parentId, int transmitInterval, int tupleCpuSize, int tupleNwSize){
		Sensor sensor0 = new Sensor(sensorName, userId, query.getQueryId(), parentId, null, transmitInterval, tupleCpuSize, tupleNwSize, "TYPE", "spout");
		query.registerSensor(sensor0);
		
	}
	
	private static List<FogDevice> createFogDevices(int userId) {
		List<FogDevice> gws = new ArrayList<FogDevice>();
		for(int i=0;i<8;i++){
			for(int j=0;j<8;j++){
				int id = i*8+j;
				gws.add(createFogDevice("gateway-"+id, 1000, new GeoCoverage(i*10, (i+1)*10, j*10, (j+1)*10), 1000, 1));
			}
		}
		List<FogDevice> level1Devices = new ArrayList<FogDevice>();
		
		for(int i=0;i<8;i+=2){
			for(int j=0;j<8;j+=2){
				FogDevice device = createFogDevice("level1-"+i+"-"+j, 10000, new GeoCoverage(i*10, (i+2)*10, j*10, (j+2)*10), 1000, 1);
				level1Devices.add(device);
				gws.get(i*8+j).setParentId(device.getId());
				gws.get((i+1)*8+j).setParentId(device.getId());
				gws.get(i*8+j+1).setParentId(device.getId());
				gws.get((i+1)*8+j+1).setParentId(device.getId());
			}
		}
		List<FogDevice> level2Devices = new ArrayList<FogDevice>();
		level2Devices.add(createFogDevice("level2-0", 10000, new GeoCoverage(0, 40, 0, 40), 10000, 1));
		level2Devices.add(createFogDevice("level2-1", 10000, new GeoCoverage(0, 40, 40, 80), 10000, 1));
		level2Devices.add(createFogDevice("level2-2", 10000, new GeoCoverage(40, 80, 0, 40), 10000, 1));
		level2Devices.add(createFogDevice("level2-3", 10000, new GeoCoverage(40, 80, 40, 80), 10000, 1));
		level1Devices.get(0).setParentId(level2Devices.get(0).getId());
		level1Devices.get(1).setParentId(level2Devices.get(0).getId());
		level1Devices.get(2).setParentId(level2Devices.get(1).getId());
		level1Devices.get(3).setParentId(level2Devices.get(1).getId());
		level1Devices.get(4).setParentId(level2Devices.get(0).getId());
		level1Devices.get(5).setParentId(level2Devices.get(0).getId());
		level1Devices.get(6).setParentId(level2Devices.get(1).getId());
		level1Devices.get(7).setParentId(level2Devices.get(1).getId());
		level1Devices.get(8).setParentId(level2Devices.get(2).getId());
		level1Devices.get(9).setParentId(level2Devices.get(2).getId());
		level1Devices.get(10).setParentId(level2Devices.get(3).getId());
		level1Devices.get(11).setParentId(level2Devices.get(3).getId());
		level1Devices.get(12).setParentId(level2Devices.get(2).getId());
		level1Devices.get(13).setParentId(level2Devices.get(2).getId());
		level1Devices.get(14).setParentId(level2Devices.get(3).getId());
		level1Devices.get(15).setParentId(level2Devices.get(3).getId());
		
		final FogDevice cloud = createFogDevice("cloud", FogUtils.MAX, new GeoCoverage(-FogUtils.MAX, FogUtils.MAX, -FogUtils.MAX, FogUtils.MAX), 0.00001, 10);
		for(FogDevice dev : level2Devices)dev.setParentId(cloud.getId());
		
		cloud.setParentId(-1);
		
		List<FogDevice> fogDevices = new ArrayList<FogDevice>(){{add(cloud);}};
		fogDevices.addAll(gws);fogDevices.addAll(level1Devices);fogDevices.addAll(level2Devices);
		return fogDevices;
	}

	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 */
	private static FogDevice createFogDevice(String name, int mips, GeoCoverage geoCoverage, double uplinkBandwidth, double latency) {

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

		int hostId = FogUtils.generateEntityId();
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;

		Host host = new Host(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerOverbooking(bw),
				storage,
				peList,
				new StreamOperatorScheduler(peList)
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
				costPerStorage, costPerBw, geoCoverage);

		FogDevice fogdevice = null;
		try {
			fogdevice = new FogDeviceCollector(name, geoCoverage, characteristics, new StreamOperatorAllocationPolicy(hostList), storageList, 0, uplinkBandwidth, latency);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fogdevice;
	}
	
	private static StreamQuery createStreamQuery(String queryId, int userId, int transmitInterval){
		int mips = 1;
		long size = 10000; // image size (MB)
		int ram = 512; // vm memory (MB)
		long bw = 1000;
		String vmm = "Xen"; // VMM name
		final StreamOperator spout = new StreamOperator(FogUtils.generateEntityId(), "spout", null, "TYPE", queryId, userId, mips, ram, bw, size, vmm, new TupleScheduler(mips, 1), 1, 1, 10, 100, 256/((double)transmitInterval));
		final StreamOperator bolt = new StreamOperator(FogUtils.generateEntityId(), "bolt", null, null, queryId, userId, mips, ram, bw, size, vmm, new TupleScheduler(mips, 1), 1, 1, 20, 200, 256/((double)transmitInterval));
		List<StreamOperator> operators = new ArrayList<StreamOperator>(){{add(spout);add(bolt);}};
		Map<String, String> edges = new HashMap<String, String>(){{put("spout", "bolt");}};
		GeoCoverage geoCoverage = new GeoCoverage(0, 80, 0, 80);
		List<OperatorEdge> operatorEdges = new ArrayList<OperatorEdge>(){{add(new OperatorEdge("sensor-TYPE-", "spout", 1));add(new OperatorEdge("spout", "bolt", 0.1));}};
		StreamQuery query = new StreamQuery(queryId, operators, edges, geoCoverage, operatorEdges);
		return query;
	}
}