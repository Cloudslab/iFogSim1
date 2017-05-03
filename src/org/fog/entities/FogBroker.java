package org.fog.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.placement.ModulePlacementPolicy;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;

public class FogBroker extends PowerDatacenterBroker{

	List<Integer> fogDeviceIds;
	List<Integer> sensorIds;
	List<Integer> actuatorIds;
	
	Map<Integer, FogDeviceCharacteristics> fogDeviceCharacteristics;
	Map<Integer, SensorCharacteristics> sensorCharacteristics;
	Map<Integer, ActuatorCharacteristics> actuatorCharacteristics;
	
	private Map<String, Application> applications;
	private Map<String, Double> appLaunchDelays;
	private Map<String, ModulePlacementPolicy> appModulePlacementPolicy;

	public FogBroker(String name) throws Exception {
		super(name);
		Log.printLine(getName() + " is starting...");
		System.out.println("Creating FogBroker with name "+name);
		setFogDeviceCharacteristics(new HashMap<Integer, FogDeviceCharacteristics>());
		setSensorCharacteristics(new HashMap<Integer, SensorCharacteristics>());
		setActuatorCharacteristics(new HashMap<Integer, ActuatorCharacteristics>());
		setApplications(new HashMap<String, Application>());
		setAppModulePlacementPolicy(new HashMap<String, ModulePlacementPolicy>());
		setAppLaunchDelays(new HashMap<String, Double>());
	}

	@Override
	public void startEntity() {
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
		for(String appId : getApplications().keySet()){
			if(getAppLaunchDelays().get(appId)==0)
				deployApplication(appId);
			else
				send(getId(), getAppLaunchDelays().get(appId), FogEvents.APP_SUBMIT, appId);
		}

	};
	
	/**
	 * Process a request for the characteristics of a FogBroker.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	@Override
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		// Send Resource Characteristics message to all Fog Devices
		for (Integer fogDeviceId : getFogDeviceIds()) {
			sendNow(fogDeviceId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
		for (Integer sensorId : getSensorIds()) {
			sendNow(sensorId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
		for (Integer actuatorId : getActuatorIds()) {
			sendNow(actuatorId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}

	/**
	 * Process the return of a request for the characteristics of a FogDevice/Actuator/Sensor.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		Integer srcId = ev.getSource();
		if (getFogDeviceIds().contains(srcId)) {
			processFogDeviceResourceCharacteristics(ev);
		} else if (getSensorIds().contains(srcId)) {
			processSensorResourceCharacteristics(ev);
		} else if (getActuatorIds().contains(actuatorIds)) {
			processActuatorResourceCharacteristics(ev);
		} else {
			// New entity joining maybe
		}
	}
	
	protected void processFogDeviceResourceCharacteristics(SimEvent ev) {
		fogDeviceCharacteristics.put(ev.getSource(), (FogDeviceCharacteristics)ev.getData());
	}
	
	protected void processSensorResourceCharacteristics(SimEvent ev) {
		sensorCharacteristics.put(ev.getSource(), (SensorCharacteristics)ev.getData());
	}
	
	protected void processActuatorResourceCharacteristics(SimEvent ev) {
		actuatorCharacteristics.put(ev.getSource(), (ActuatorCharacteristics)ev.getData());
	}
	
	public void submitApplication(Application application, double delay, ModulePlacementPolicy modulePlacement){
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getApplications().put(application.getAppId(), application);
		getAppModulePlacementPolicy().put(application.getAppId(), modulePlacement);
		getAppLaunchDelays().put(application.getAppId(), delay);
		// TODO Assign actuators to VMs 
		
		/*for(AppEdge edge : application.getEdges()){
			if(edge.getEdgeType() == AppEdge.ACTUATOR){
				String moduleName = edge.getSource();
				for(Actuator actuator : getActuators()){
					if(actuator.getActuatorType().equalsIgnoreCase(edge.getDestination()))
						application.getModuleByName(moduleName).subscribeActuator(actuator.getId(), edge.getTupleType());
				}
			}
		}*/
		
		send(getId(), delay, FogEvents.APP_SUBMIT, application.getAppId());
	}
	
	protected void deployApplication(String appId) {
		Application application = getApplications().get(appId);
		ModulePlacementPolicy modulePlacementPolicy = getAppModulePlacementPolicy().get(application.getAppId());
		for(Integer fogDeviceId : fogDeviceIds){
			// TODO Check necessity of this step
			sendNow(fogDeviceId, FogEvents.ACTIVE_APP_UPDATE, application);
		}
		
		Map<Integer, List<AppModule>> deviceToModuleMap = modulePlacementPolicy.getDeviceToModuleMap();
		for(Integer deviceId : deviceToModuleMap.keySet()){
			for(AppModule module : deviceToModuleMap.get(deviceId)){
				sendNow(deviceId, FogEvents.APP_SUBMIT, application);
				sendNow(deviceId, FogEvents.LAUNCH_MODULE, module);
			}
		}
	}
	
	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()) {
		case FogEvents.APP_SUBMIT:
			deployApplication(ev.getData().toString());
			break;
		case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
			System.out.println("Resource characteristics request received");
			processResourceCharacteristicsRequest(ev);
			break;
		case CloudSimTags.RESOURCE_CHARACTERISTICS:
			System.out.println("Resource characteristics received from ID "+ev.getSource()+" "+CloudSim.getEntityName(ev.getSource()));
			processResourceCharacteristics(ev);
			break;
		}
	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		
	}
	
	public void setFogDeviceIds(List<Integer> fogDeviceIds) {
		this.fogDeviceIds = fogDeviceIds;
	}
	
	public void setSensorIds(List<Integer> sensorIds) {
		this.sensorIds = sensorIds;
	}
	
	public void setActuatorIds(List<Integer> actuatorIds) {
		this.actuatorIds = actuatorIds;
	}
	
	public List<Integer> getFogDeviceIds() {
		return fogDeviceIds;
	}
	
	public List<Integer> getSensorIds() {
		return sensorIds;
	}
	
	public List<Integer> getActuatorIds() {
		return actuatorIds;
	}

	public Map<Integer, FogDeviceCharacteristics> getFogDeviceCharacteristics() {
		return fogDeviceCharacteristics;
	}

	public void setFogDeviceCharacteristics(
			Map<Integer, FogDeviceCharacteristics> fogDeviceCharacteristics) {
		this.fogDeviceCharacteristics = fogDeviceCharacteristics;
	}

	public Map<Integer, SensorCharacteristics> getSensorCharacteristics() {
		return sensorCharacteristics;
	}

	public void setSensorCharacteristics(
			Map<Integer, SensorCharacteristics> sensorCharacteristics) {
		this.sensorCharacteristics = sensorCharacteristics;
	}

	public Map<Integer, ActuatorCharacteristics> getActuatorCharacteristics() {
		return actuatorCharacteristics;
	}

	public void setActuatorCharacteristics(
			Map<Integer, ActuatorCharacteristics> actuatorCharacteristics) {
		this.actuatorCharacteristics = actuatorCharacteristics;
	}

	public Map<String, Application> getApplications() {
		return applications;
	}

	public void setApplications(Map<String, Application> applications) {
		this.applications = applications;
	}

	public Map<String, ModulePlacementPolicy> getAppModulePlacementPolicy() {
		return appModulePlacementPolicy;
	}

	public void setAppModulePlacementPolicy(Map<String, ModulePlacementPolicy> appModulePlacementPolicy) {
		this.appModulePlacementPolicy = appModulePlacementPolicy;
	}

	public Map<String, Double> getAppLaunchDelays() {
		return appLaunchDelays;
	}

	public void setAppLaunchDelays(Map<String, Double> appLaunchDelays) {
		this.appLaunchDelays = appLaunchDelays;
	}
}
