package org.fog.dsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.fog.entities.FogDevice;
import org.fog.entities.StreamOperator;

public class OperatorPlacementNoOverbooking extends OperatorPlacement{

	public OperatorPlacementNoOverbooking(List<FogDevice> fogDevices, StreamQuery streamQuery){
		super(fogDevices, streamQuery);
		this.setFogDevices(fogDevices);
		this.setStreamQuery(streamQuery);
		
		List<String> leafOperators = streamQuery.getLeaves();
		setOperatorToDeviceMap(new HashMap<String, Integer>());
		for(String leafOperator : leafOperators){
			StreamOperator operator = streamQuery.getOperatorByName(leafOperator);
			
			FogDevice currentDevice = getLowestCoveringFogDevice(fogDevices, streamQuery);
			setLowestDevice(currentDevice);
			// begin placing the operators in some order
			
			while(true){
				if(currentDevice != null){
					if(canBeCreated(currentDevice, operator)){
						getOperatorToDeviceMap().put(operator.getName(), currentDevice.getId());
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
			if(!getOperatorToDeviceMap().containsKey(operator.getName()))
				remainingOperators.add(operator.getName());
		}
		
		while(remainingOperators.size() > 0){
			//System.out.println("BOOO");
			String operator = remainingOperators.get(0);
			if(allChildrenMapped(operator)){
				FogDevice currentDevice = getLowestSuitableDevice(operator);
				while(true){
					if(currentDevice != null){
						if(canBeCreated(currentDevice, streamQuery.getOperatorByName(operator))){
							getOperatorToDeviceMap().put(operator, currentDevice.getId());
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
}
