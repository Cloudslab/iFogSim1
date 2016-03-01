package org.fog.entities;

import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.utils.FogEvents;
import org.fog.utils.GeoCoverage;

public class FogDeviceNoNetworkDelay extends FogDeviceCollector{

	public FogDeviceNoNetworkDelay(String name, GeoCoverage geoCoverage,
			FogDeviceCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
			double schedulingInterval, double uplinkBandwidth, double latency)
			throws Exception {
		super(name, geoCoverage, characteristics, vmAllocationPolicy, storageList,
				schedulingInterval, uplinkBandwidth, latency);
	}
	
	@Override
	protected void sendUp(Tuple tuple){
		if(getActiveQueries().contains(tuple.getQueryId())){
			outputTupleTimes.add(CloudSim.clock());
			if(outputTupleTimesByOperator.containsKey(tuple.getSrcOperatorId()))
				outputTupleTimesByOperator.get(tuple.getSrcOperatorId()).add(CloudSim.clock());
			else{
				outputTupleTimesByOperator.put(tuple.getSrcOperatorId(), new LinkedList<Double>());
				outputTupleTimesByOperator.get(tuple.getSrcOperatorId()).add(CloudSim.clock());
			}
			
			outputTupleLengthsByOperator.put(tuple.getSrcOperatorId(), (double) tuple.getCloudletFileSize());
		}
		
		if(parentId > 0){
			//System.out.println("Sending tuple from gateway to cloud "+tuple.getActualTupleId());
			sendNow(getId(), FogEvents.UPDATE_TUPLE_QUEUE);
			sendNow(parentId, FogEvents.TUPLE_ARRIVAL, tuple);
		}
	}

}
