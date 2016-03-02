package org.fog.entities;

import java.util.List;

import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.fog.utils.FogEvents;
import org.fog.utils.GeoCoverage;

public class FogDeviceNoNetworkDelay extends FogDevice{

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
		if(parentId > 0){
			sendNow(getId(), FogEvents.UPDATE_TUPLE_QUEUE);
			sendNow(parentId, FogEvents.TUPLE_ARRIVAL, tuple);
		}
	}

}
