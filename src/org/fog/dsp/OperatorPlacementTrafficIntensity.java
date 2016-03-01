package org.fog.dsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.entities.FogDevice;
import org.fog.entities.StreamOperator;
import org.fog.utils.OperatorEdge;

public class OperatorPlacementTrafficIntensity extends OperatorPlacement{

	/**
	 * Map of fog device id to traffic intensity before placement
	 */
	protected Map<Integer, Double> trafficIntensity;
	
	/**
	 * Map of fog device id to traffic intensity after placement
	 */
	protected Map<Integer, Double> trafficIntensityAfterPlacement;
	
	/**
	 * Map of stream operator ID to output rate of tuples
	 */
	protected Map<String, Double> operatorOutputRate;
	
	public OperatorPlacementTrafficIntensity(List<FogDevice> fogDevices, StreamQuery streamQuery){
		super(fogDevices, streamQuery);
		this.setFogDevices(fogDevices);
		this.setStreamQuery(streamQuery);
		setTrafficIntensity(new HashMap<Integer, Double>());
		setTrafficIntensityAfterPlacement(new HashMap<Integer, Double>());
		setOperatorOutputRate(new HashMap<String, Double>());
		
		calculateOutputRates();
		
		for(FogDevice fogDevice : getFogDevices()){
			trafficIntensity.put(fogDevice.getId(), fogDevice.getTrafficIntensity());
			trafficIntensityAfterPlacement.put(fogDevice.getId(), fogDevice.getTrafficIntensity());
		}
				
		List<String> leafOperators = streamQuery.getLeaves();
		setOperatorToDeviceMap(new HashMap<String, Integer>());
		for(String leafOperator : leafOperators){
			StreamOperator operator = streamQuery.getOperatorByName(leafOperator);
			
			FogDevice currentDevice = getLowestCoveringFogDevice(fogDevices, streamQuery);
			setLowestDevice(currentDevice);
			// begin placing the operators in some order
			
			while(true){
				System.out.println("AASASAS");
				if(currentDevice != null){
					System.out.println("XXXXXXXXXXXx");
					if(canBeCreated(currentDevice, operator)){
						getOperatorToDeviceMap().put(operator.getName(), currentDevice.getId());
						trafficIntensityAfterPlacement.put(currentDevice.getId(), getTrafficIntensityAfterPlacement(currentDevice, operator));
						break;
					}else{
						currentDevice = getDeviceById(currentDevice.getParentId());
					}
				}else{
					System.out.println("OOOOOOOOOOOO");
					break;
				}
			}
			System.out.println("55555555555");
		}
		
		List<String> remainingOperators = new ArrayList<String>();
		
		for(StreamOperator operator : streamQuery.getOperators()){
			if(!getOperatorToDeviceMap().containsKey(operator.getName()))
				remainingOperators.add(operator.getName());
		}
		
		while(remainingOperators.size() > 0){
			String operator = remainingOperators.get(0);
			//System.out.println(operator);

			if(allChildrenMapped(operator)){
				
				FogDevice currentDevice = (getStreamQuery().isLeafOperator(operator))?getLowestCoveringFogDevice(fogDevices, streamQuery):getLowestSuitableDevice(operator);
				while(true){
					System.out.println("AAAAAAAA");
					if(currentDevice != null){
						if(canBeCreated(currentDevice, streamQuery.getOperatorByName(operator))){
							getOperatorToDeviceMap().put(operator, currentDevice.getId());
							System.out.println(operator + " placed on device "+currentDevice.getName());
							remainingOperators.remove(0);
							break;
						}else{
							currentDevice = getDeviceById(currentDevice.getParentId());
						}
					}else{
						break;
					}
				}
				
			}
		}
		System.out.println("KHATAM !!!!!!!!!");
		System.out.println(getOperatorToDeviceMap());
		
		System.out.println(trafficIntensityAfterPlacement);

	}

	private double getTrafficIntensityAfterPlacement(FogDevice fogDevice,
			StreamOperator streamOperator){
		double currentIntensity = trafficIntensityAfterPlacement.get(fogDevice.getId());
		System.out.println("Current intensity of "+fogDevice.getName()+" = "+currentIntensity);
		double uplinkBw = fogDevice.getUplinkBandwidth();
		double currentTraffic = currentIntensity*uplinkBw;
		
		List<String> children = getStreamQuery().getAllChildren(streamOperator.getName());
		for(String child : children){
			if(getOperatorToDeviceMap().containsKey(child) && getOperatorToDeviceMap().get(child)==fogDevice.getId()){
				currentTraffic -= operatorOutputRate.get(child)*getStreamQuery().getOperatorByName(child).getTupleFileLength();
			}
		}
		
		double outputRate = operatorOutputRate.get(streamOperator.getName());
		double tupleFileLength = streamOperator.getTupleFileLength();
		
		System.out.println("Output rate : "+outputRate);
		System.out.println("Tuple file length : "+tupleFileLength);
		
		double newIntensity = (currentTraffic+(outputRate*tupleFileLength))/uplinkBw;
		
		return newIntensity;
	}
	
	@Override
	protected boolean canBeCreated(FogDevice fogDevice,
			StreamOperator streamOperator) {
		// GET THE TRAFFIC INTENSITY OF THE DEVICE AND CALCULATE WHETHER THE OPERATOR CAN BE CREATED HERE OR NOT
		String parentOperator = getStreamQuery().getParentOperator(streamOperator.getName());
		
		if(getOperatorToDeviceMap().containsKey(parentOperator) && getOperatorToDeviceMap().get(parentOperator) == fogDevice.getId())
			return true;
		
		double newIntensity = getTrafficIntensityAfterPlacement(fogDevice, streamOperator);
		System.out.println("INtensity for "+streamOperator.getName()+" = "+newIntensity);
		if(newIntensity <= 1)
			if(fogDevice.getVmAllocationPolicy().allocateHostForVm(streamOperator))
				return true;
		
		return false;
	}
	
	protected double calculateOutputRate(String operator){
		if(getStreamQuery().isLeafOperator(operator)){
			double outputRate = getStreamQuery().getOperatorByName(operator).getSensorRate()*getStreamQuery().getSelectivity(operator, "sensor");
			System.out.println("Output rate of "+operator+" = "+outputRate);
			return outputRate;
		}
		double outputRate = 0;
		List<String> childOperators = getStreamQuery().getAllChildren(operator);
		for(String childOperator : childOperators){
			if(!operatorOutputRate.containsKey(childOperator)){
				return -1;
			}
			else{
				System.out.println(operator);
				System.out.println(getStreamQuery().getSelectivity(operator, childOperator));
				System.out.println(operatorOutputRate.get(childOperator));
				outputRate += getStreamQuery().getSelectivity(operator, childOperator)*operatorOutputRate.get(childOperator);
			}
		}
		
		return outputRate;
	}
	protected void calculateOutputRates(){
		while(true){
			for(StreamOperator operator : getStreamQuery().getOperators()){
				double outputRate = calculateOutputRate(operator.getName());
				System.out.println(outputRate);
				if(outputRate > 0){
					operatorOutputRate.put(operator.getName(), outputRate);
					
				}
			}
			
			boolean allNotDone = false;
			for(StreamOperator operator : getStreamQuery().getOperators()){
				if(!operatorOutputRate.containsKey(operator.getName()))
					allNotDone = true;
			}
			if(!allNotDone)
				break;
		}
	}

	public Map<Integer, Double> getTrafficIntensity() {
		return trafficIntensity;
	}

	public void setTrafficIntensity(Map<Integer, Double> trafficIntensity) {
		this.trafficIntensity = trafficIntensity;
	}

	public Map<Integer, Double> getTrafficIntensityAfterPlacement() {
		return trafficIntensityAfterPlacement;
	}

	public void setTrafficIntensityAfterPlacement(
			Map<Integer, Double> trafficIntensityAfterPlacement) {
		this.trafficIntensityAfterPlacement = trafficIntensityAfterPlacement;
	}

	public Map<String, Double> getOperatorOutputRate() {
		return operatorOutputRate;
	}

	public void setOperatorOutputRate(Map<String, Double> operatorOutputRate) {
		this.operatorOutputRate = operatorOutputRate;
	}
	
}
