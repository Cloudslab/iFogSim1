package org.fog.dsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fog.entities.FogDevice;
import org.fog.entities.StreamOperator;

public class OperatorPlacementSimple {

	private List<FogDevice> fogDevices;
	private StreamQuery streamQuery;
	private Map<String, Integer> operatorToDeviceMap;
	private FogDevice lowestDevice;
	
	public OperatorPlacementSimple(List<FogDevice> fogDevices, StreamQuery streamQuery){
		this.setFogDevices(fogDevices);
		this.setStreamQuery(streamQuery);
		
		List<String> leafOperators = streamQuery.getLeaves();
		this.operatorToDeviceMap = new HashMap<String, Integer>();
		for(String leafOperator : leafOperators){
			StreamOperator operator = streamQuery.getOperatorByName(leafOperator);
			
			FogDevice currentDevice = getLowestCoveringFogDevice(fogDevices, streamQuery);
			this.lowestDevice = currentDevice;
			// begin placing the operators in some order
			
			while(true){
				if(currentDevice != null){
					if(canBeCreated(currentDevice, operator)){
						operatorToDeviceMap.put(operator.getName(), currentDevice.getId());
						break;
					}else{
						currentDevice = getDeviceById(currentDevice.getParentId());
					}
				}else{
					break;
				}
			}
		}
		
		List<String> remainingOperators = new ArrayList<String>();
		
		for(StreamOperator operator : streamQuery.getOperators()){
			if(!operatorToDeviceMap.containsKey(operator.getName()))
				remainingOperators.add(operator.getName());
		}
		
		while(remainingOperators.size() > 0){

			String operator = remainingOperators.get(0);
			if(allChildrenMapped(operator)){
				FogDevice currentDevice = getLowestSuitableDevice(operator);
				while(true){

					if(currentDevice != null){
						if(canBeCreated(currentDevice, streamQuery.getOperatorByName(operator))){
							operatorToDeviceMap.put(operator, currentDevice.getId());
							System.out.println(operator + " placed");
							remainingOperators.remove(0);
							break;
						}else{
							//System.out.println("ASASASAS");
							currentDevice = getDeviceById(currentDevice.getParentId());
						}
					}else{
						break;
					}
				}
				
			}
		}
	}
	
	private FogDevice getLowestSuitableDevice(String operator){
		List<String> children = streamQuery.getAllChildren(operator);
		FogDevice highestChildrenHolder = getLowestDevice();
		for(String child : children){
			if(getDeviceById(operatorToDeviceMap.get(child)).getGeoCoverage().covers(highestChildrenHolder.getGeoCoverage()))
				highestChildrenHolder = getDeviceById(operatorToDeviceMap.get(child));
		}
		return highestChildrenHolder;
	}
	
	private boolean allChildrenMapped(String operator){
		boolean result = true;
		List<String> children = streamQuery.getAllChildren(operator);
		for(String child : children){
			if(!this.operatorToDeviceMap.containsKey(child)){
				result = false;
			}
		}
		return result;
	}
	
	private FogDevice getDeviceById(int id){
		for(FogDevice dev : fogDevices){
			if(dev.getId() == id)
				return dev;
		}
		return null;
	}
	
	private boolean canBeCreated(FogDevice fogDevice, StreamOperator streamOperator){
		return fogDevice.getVmAllocationPolicy().allocateHostForVm(streamOperator);
	}
	
	private FogDevice getLowestCoveringFogDevice(List<FogDevice> fogDevices, StreamQuery streamQuery){
		FogDevice coverer = null;
		System.out.println("Fog Devices "+fogDevices);
		for(FogDevice fogDevice : fogDevices){
			System.out.println("Device "+fogDevice.getGeoCoverage());
			System.out.println("Query "+streamQuery.getGeoCoverage());
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
