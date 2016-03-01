package org.fog.dsp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.entities.FogDevice;
import org.fog.entities.StreamOperator;
import org.fog.utils.OperatorEdge;

public abstract class OperatorPlacement {

	private List<FogDevice> fogDevices;
	private StreamQuery streamQuery;
	private Map<String, Integer> operatorToDeviceMap;
	private FogDevice lowestDevice;
	
	public OperatorPlacement(List<FogDevice> fogDevices, StreamQuery streamQuery){
		setFogDevices(fogDevices);
		setStreamQuery(streamQuery);
	}
	
	protected FogDevice getLowestSuitableDevice(String operator){
		List<String> children = streamQuery.getAllChildren(operator);
		FogDevice highestChildrenHolder = getLowestDevice();
		for(String child : children){
			if(getDeviceById(operatorToDeviceMap.get(child)).getGeoCoverage().covers(highestChildrenHolder.getGeoCoverage()))
				highestChildrenHolder = getDeviceById(operatorToDeviceMap.get(child));
		}
		return highestChildrenHolder;
	}
	
	protected List<Integer> getFogDevicesForInitialPlacement(){
		List<Integer> devices = new ArrayList<Integer>();
		int lowest = getLowestCoveringFogDevice(getFogDevices(), getStreamQuery()).getId();
		int id = lowest;
		while(id > -1){
			devices.add(id);
			id = ((FogDevice)CloudSim.getEntity(id)).getParentId();
		}
		return devices;
	}
	
	protected boolean allChildrenMapped(String operator){
		if(getStreamQuery().isLeafOperator(operator))
			return true;
		boolean result = true;
		List<String> children = streamQuery.getAllChildren(operator);
		for(String child : children){
			if(!this.operatorToDeviceMap.containsKey(child)){
				result = false;
			}
		}
		return result;
	}
	
	protected FogDevice getDeviceById(int id){
		for(FogDevice dev : fogDevices){
			if(dev.getId() == id)
				return dev;
		}
		return null;
	}
	
	protected boolean canBeCreated(FogDevice fogDevice, StreamOperator streamOperator){
		return fogDevice.getVmAllocationPolicy().allocateHostForVm(streamOperator);
	}
	
	protected boolean isOperatorLeaf(StreamOperator operator){
		for(OperatorEdge edge :streamQuery.getOperatorEdges()){
			if(edge.getSrc().equals("sensor") && edge.getDst().equals(operator.getName()))
				return true;
		}
		return false;
	}
	
	protected FogDevice getLowestCoveringFogDevice(List<FogDevice> fogDevices, StreamQuery streamQuery){
		FogDevice coverer = null;
		//System.out.println("Fog Devices "+fogDevices);
		for(FogDevice fogDevice : fogDevices){
			//System.out.println("Device "+fogDevice.getGeoCoverage());
			//System.out.println("Query "+streamQuery.getGeoCoverage());
			if(fogDevice.getGeoCoverage().covers(streamQuery.getGeoCoverage())){
				if(coverer == null)
					coverer = fogDevice;
				else if(coverer.getGeoCoverage().covers(fogDevice.getGeoCoverage()))
					coverer = fogDevice;
			}
		}
		return coverer;
	}

	public List<FogDevice> getFogDevices() {
		return fogDevices;
	}

	public void setFogDevices(List<FogDevice> fogDevices) {
		this.fogDevices = fogDevices;
	}

	public StreamQuery getStreamQuery() {
		return streamQuery;
	}

	public void setStreamQuery(StreamQuery streamQuery) {
		this.streamQuery = streamQuery;
	}

	public FogDevice getLowestDevice() {
		return lowestDevice;
	}

	public void setLowestDevice(FogDevice lowestDevice) {
		this.lowestDevice = lowestDevice;
	}

	public Map<String, Integer> getOperatorToDeviceMap() {
		return operatorToDeviceMap;
	}

	public void setOperatorToDeviceMap(Map<String, Integer> operatorToDeviceMap) {
		this.operatorToDeviceMap = operatorToDeviceMap;
	}

	
}
