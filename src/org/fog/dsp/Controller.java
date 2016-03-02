package org.fog.dsp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.FogDevice;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.TupleFinishDetails;

public class Controller extends SimEntity{

	public static double RESOURCE_MANAGE_INTERVAL = 100;
	public static double LATENCY_WINDOW = 1000;
	
	public static boolean ONLY_CLOUD = false;
	
	private OperatorPlacementOnlyCloud operatorPlacement;
	
	private List<FogDevice> fogDevices;
	
	private Map<String, Application> applications;
	private Map<String, Integer> appLaunchDelays;

	public Controller(String name, List<FogDevice> fogDevices) {
		super(name);
		this.applications = new HashMap<String, Application>();
		this.setAppLaunchDelays(new HashMap<String, Integer>());
		for(FogDevice fogDevice : fogDevices){
			fogDevice.setControllerId(getId());
		}
		setFogDevices(fogDevices);
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		for(String appId : applications.keySet()){
			if(getAppLaunchDelays().get(appId)==0)
				processAppSubmit(applications.get(appId));
			else
				send(getId(), getAppLaunchDelays().get(appId), FogEvents.APP_SUBMIT, applications.get(appId));
		}

		send(getId(), RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
		case FogEvents.APP_SUBMIT:
			processAppSubmit(ev);
			break;
		case FogEvents.TUPLE_FINISHED:
			processTupleFinished(ev);
			break;
		case FogEvents.CONTROLLER_RESOURCE_MANAGE:
			manageResources();
			break;
		}
	}
	
	protected void manageResources(){
		send(getId(), RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
	}
	
	private void processTupleFinished(SimEvent ev) {
	}
	
	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		
	}
	
	public void submitApplication(Application application, int delay){
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getApplications().put(application.getAppId(), application);
		getAppLaunchDelays().put(application.getAppId(), delay);
	}
	
	private void processAppSubmit(SimEvent ev){
		Application app = (Application) ev.getData();
		processAppSubmit(app);
	}
	
	private void processAppSubmit(Application application){
		System.out.println(CloudSim.clock()+" Submitted application "+ application.getAppId());
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getApplications().put(application.getAppId(), application);
		
		Map<String, Integer> allocationMap = null;
		allocationMap = (new OperatorPlacementOnlyCloud(fogDevices, application)).getModuleToDeviceMap();
		
		for(FogDevice fogDevice : fogDevices){
			sendNow(fogDevice.getId(), FogEvents.ACTIVE_APP_UPDATE, application);
		}
		
		for(String moduleName : allocationMap.keySet()){
			AppModule module = application.getModuleByName(moduleName);
			
			sendNow(allocationMap.get(moduleName), FogEvents.APP_SUBMIT, application);
			
			sendNow(allocationMap.get(moduleName), FogEvents.LAUNCH_MODULE, module);
		}
	}
	
	/*public OperatorPlacement getOperatorPlacement() {
		return operatorPlacement;
	}

	public void setOperatorPlacement(OperatorPlacement operatorPlacement) {
		this.operatorPlacement = operatorPlacement;
	}*/

	public List<FogDevice> getFogDevices() {
		return fogDevices;
	}

	public void setFogDevices(List<FogDevice> fogDevices) {
		this.fogDevices = fogDevices;
	}

	public Map<String, Integer> getAppLaunchDelays() {
		return appLaunchDelays;
	}

	public void setAppLaunchDelays(Map<String, Integer> appLaunchDelays) {
		this.appLaunchDelays = appLaunchDelays;
	}

	public Map<String, Application> getApplications() {
		return applications;
	}

	public void setApplications(Map<String, Application> applications) {
		this.applications = applications;
	}
}
