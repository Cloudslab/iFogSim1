/*
 * Title:        iFogSim Toolkit
 * Description:  iFogSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 *
 */

package org.fog.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.placement.ModulePlacement;
import org.fog.placement.ModulePlacementPolicy;
import org.fog.utils.AppModuleAddress;
import org.fog.utils.Config;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.Logger;
import org.fog.utils.TimeKeeper;

public class FogBroker extends PowerDatacenterBroker{

	class ModuleLinks {
		Map<AppModule, Integer> modulesToDispatch;
		Map<Integer, AppModuleAddress> endpointConnection;
		public Map<AppModule, Integer> getModulesToDispatch() {
			return modulesToDispatch;
		}
		public void setModulesToDispatch(Map<AppModule, Integer> modulesToDispatch) {
			this.modulesToDispatch = modulesToDispatch;
		}
		public Map<Integer, AppModuleAddress> getEndpointConnection() {
			return endpointConnection;
		}
		public void setEndpointConnection(
				Map<Integer, AppModuleAddress> endpointConnection) {
			this.endpointConnection = endpointConnection;
		}
	}

	private static final String LOG_TAG = "FOG_BROKER";
	
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
		schedule(0, Config.MAX_SIMULATION_TIME, FogEvents.STOP_SIMULATION);
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
			System.out.println("Sending charac req to "+fogDeviceId);
			send(fogDeviceId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
		for (Integer sensorId : getSensorIds()) {
			System.out.println("Sending charac req to "+sensorId);
			send(sensorId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
		for (Integer actuatorId : getActuatorIds()) {
			System.out.println("Sending charac req to "+actuatorId);
			send(actuatorId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
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
		System.out.println("Received characteristics from "+srcId);
		if (getFogDeviceIds().contains(srcId)) {
			processFogDeviceResourceCharacteristics(ev);
		} else if (getSensorIds().contains(srcId)) {
			processSensorResourceCharacteristics(ev);
		} else if (getActuatorIds().contains(srcId)) {
			processActuatorResourceCharacteristics(ev);
		} else {
			// New entity joining maybe
		}
		
		if (getFogDeviceCharacteristics().size() + getSensorCharacteristics().size() + getActuatorCharacteristics().size()
				== getFogDeviceIds().size() + getSensorIds().size() + getActuatorIds().size()) {
			// All devices responded
			for (String appId : getApplications().keySet()) {
				deployApplication(appId);
			}
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
		
		send(getId(), delay, FogEvents.APP_SUBMIT, application.getAppId());
	}
	
	protected void deployApplication(String appId) {
		Application application = getApplications().get(appId);
		ModulePlacementPolicy modulePlacementPolicy = getAppModulePlacementPolicy().get(application.getAppId());
		for(Integer fogDeviceId : fogDeviceIds){
			// TODO Check necessity of this step
			sendNow(fogDeviceId, FogEvents.ACTIVE_APP_UPDATE, application);
		}
		
		List<FogDeviceCharacteristics> fogDeviceCharacteristics = new ArrayList<FogDeviceCharacteristics>();
		for (Integer f : getFogDeviceCharacteristics().keySet())
			fogDeviceCharacteristics.add(getFogDeviceCharacteristics().get(f));
		List<SensorCharacteristics> sensorCharacteristics = new ArrayList<SensorCharacteristics>();
		for (Integer s : getSensorCharacteristics().keySet())
			sensorCharacteristics.add(getSensorCharacteristics().get(s));
		List<ActuatorCharacteristics> actuatorCharacteristics = new ArrayList<ActuatorCharacteristics>();
		for (Integer a : getActuatorCharacteristics().keySet())
			actuatorCharacteristics.add(getActuatorCharacteristics().get(a));
		
		List<ModulePlacement> placements = modulePlacementPolicy.computeModulePlacements(fogDeviceCharacteristics, sensorCharacteristics, actuatorCharacteristics);
		
		 ModuleLinks moduleLinks = linkModules(placements, application);
		 
		 Map<AppModule, Integer> modulesToDispatch = moduleLinks.getModulesToDispatch();
		 
		for (Entry<AppModule, Integer> dispatch : modulesToDispatch.entrySet()) {
			sendNow(dispatch.getValue(), FogEvents.APP_SUBMIT, application);
			sendNow(dispatch.getValue(), FogEvents.LAUNCH_MODULE, dispatch.getKey());
		}

		for (Integer endpointId : moduleLinks.getEndpointConnection().keySet()) {
			sendNow(endpointId, FogEvents.ENDPOINT_CONNECTION, moduleLinks.getEndpointConnection().get(endpointId));
		}
	}
	
	private ModuleLinks linkModules(List<ModulePlacement> placements,
			Application application) {
		// TODO This function needs thorough testing
		Map<AppModule, Integer> dispatchMapping = new HashMap<AppModule, Integer>();
		ModuleLinks moduleLinks = new ModuleLinks();
		moduleLinks.setEndpointConnection(new HashMap<Integer, AppModuleAddress>());
		for (ModulePlacement placement : placements) {
			Map<String, AppModule> linkedModules = new HashMap<String, AppModule>();
			for (Entry<String, Integer> mapping : placement.getPlacementMap().entrySet()) {
				AppModule moduleToDispatch = new AppModule(application.getModuleByName(mapping.getKey()));
				linkedModules.put(mapping.getKey(), moduleToDispatch);
			}
			
			for (AppEdge e : application.getEdges()) {
				String srcModule = e.getSource();
				String dstModule = e.getDestination();

				if (e.getEdgeType() == AppEdge.MODULE) {

					int dstVmId = linkedModules.get(dstModule).getId();
					int dstDeviceId = placement.getMappedDeviceId(dstModule);
					linkedModules.get(srcModule).addDestModule(e.getTupleType(), new AppModuleAddress(dstVmId, dstDeviceId));
					
				} else if (e.getEdgeType() == AppEdge.SENSOR) {
					AppModuleAddress addr = new AppModuleAddress(linkedModules.get(dstModule).getId(), placement.getMappedDeviceId(dstModule));
					for (Integer sensorId : placement.getSensorIds().get(srcModule)) {
						moduleLinks.getEndpointConnection().put(sensorId, addr);
					}	
				} else if (e.getEdgeType() == AppEdge.ACTUATOR) {
					String actuatorType = dstModule;
					AppModuleAddress addr = new AppModuleAddress(linkedModules.get(srcModule).getId(), placement.getMappedDeviceId(srcModule));
					for (Integer actuatorId : placement.getActuatorIds().get(actuatorType)) {
						linkedModules.get(srcModule).subscribeActuator(actuatorId, e.getTupleType());
						moduleLinks.getEndpointConnection().put(actuatorId, addr);
					}	
				}
			}
			
			for (String moduleName : linkedModules.keySet()) {
				dispatchMapping.put(linkedModules.get(moduleName), placement.getMappedDeviceId(moduleName));
			}
		}
		
		moduleLinks.setModulesToDispatch(dispatchMapping);
		return moduleLinks;
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()) {
		case FogEvents.APP_SUBMIT:
			deployApplication(ev.getData().toString());
			break;
		case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
			processResourceCharacteristicsRequest(ev);
			break;
		case CloudSimTags.RESOURCE_CHARACTERISTICS:
			processResourceCharacteristics(ev);
			break;
		}
	}

	private String getStringForLoopId(int loopId){
		for(String appId : getApplications().keySet()){
			Logger.debug(LOG_TAG, "FOG_BROKER", "Has application : "+appId);
			Application app = getApplications().get(appId);
			for(AppLoop loop : app.getLoops()){
				if(loop.getLoopId() == loopId)
					return loop.getModules().toString();
			}
		}
		return null;
	}
	
	private void printTimeDetails() {
		System.out.println("=========================================");
		System.out.println("APPLICATION LOOP DELAYS");
		System.out.println("=========================================");
		for(Integer loopId : TimeKeeper.getInstance().getLoopIdToTupleIds().keySet()){
			for (String appId : getApplications().keySet()) {
				Application app = getApplications().get(appId);
				for (AppLoop loop : app.getLoops()) {
					if (loop.getLoopId() == loopId)
						System.out.println(getStringForLoopId(loopId) + " ---> "+TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loopId));		
				}
			}
			
		}
		System.out.println("=========================================");
		System.out.println("TUPLE CPU EXECUTION DELAY");
		System.out.println("=========================================");
		
		for(String tupleType : TimeKeeper.getInstance().getTupleTypeToAverageCpuTime().keySet()){
			System.out.println(tupleType + " ---> "+TimeKeeper.getInstance().getTupleTypeToAverageCpuTime().get(tupleType));
		}
		
		System.out.println("=========================================");
	}
	
	@Override
	public void shutdownEntity() {
		printTimeDetails();
		printEnergyConsumptionDetails();
	}
	
	private void printEnergyConsumptionDetails() {
		System.out.println("=========================================");
		System.out.println("FOG DEVICE ENERGY CONSUMPTION");
		System.out.println("=========================================");
		for (int devId : getFogDeviceIds()) {
			FogDevice dev = (FogDevice) CloudSim.getEntity(devId);
			System.out.format("%s : %f kJ\n", dev.getName(), dev.getPower()/1000);
		}
		System.out.println("=========================================");
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
