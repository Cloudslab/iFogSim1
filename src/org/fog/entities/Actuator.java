/*
 * Title:        iFogSim Toolkit
 * Description:  iFogSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 *
 */

package org.fog.entities;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.utils.FogEvents;
import org.fog.utils.GeoLocation;
import org.fog.utils.Logger;
import org.fog.utils.TimeKeeper;

public class Actuator extends SimEntity{
	private static String LOG_TAG = "ACTUATOR";
	
	private int gatewayDeviceId;
	private double latency;
	private GeoLocation geoLocation;
	private String appId;
	private int userId;
	private String actuatorType;
	private Application app; // TODO remove
	private ActuatorCharacteristics characteristics;
	private Application application;
	private int endDeviceId;
	
	public Actuator(String name, int userId, String appId, int gatewayDeviceId, double latency, GeoLocation geoLocation, String actuatorType, String srcModuleName) {
		super(name);
		this.setAppId(appId);
		this.gatewayDeviceId = gatewayDeviceId;
		this.geoLocation = geoLocation;
		setUserId(userId);
		setActuatorType(actuatorType);
		setLatency(latency);
		setCharacteristics(new ActuatorCharacteristics(getId(), geoLocation, actuatorType));
		setEndDeviceId(-1);
	}
	
	public Actuator(String name, int userId, String appId, String actuatorType, Application application) {
		super(name);
		this.setAppId(appId);
		setUserId(userId);
		setActuatorType(actuatorType);
		setApplication(application);
		setCharacteristics(new ActuatorCharacteristics(getId(), null, actuatorType));
		setEndDeviceId(-1);
	}

	public Actuator(String name, int userId, String appId, String actuatorType) {
		super(name);
		this.setAppId(appId);
		setUserId(userId);
		setActuatorType(actuatorType);
		setCharacteristics(new ActuatorCharacteristics(getId(), null, actuatorType));
		setEndDeviceId(-1);
	}

	
	@Override
	public void startEntity() {
		//sendNow(gatewayDeviceId, FogEvents.ACTUATOR_JOINED, getLatency());
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
		case CloudSimTags.RESOURCE_CHARACTERISTICS:
			int srcId = ((Integer) ev.getData()).intValue();
			sendNow(srcId, ev.getTag(), getCharacteristics());
			break;
		case FogEvents.TUPLE_ARRIVAL:
			processTupleArrival(ev);
			break;
		}		
	}

	private void processTupleArrival(SimEvent ev) {
		Tuple tuple = (Tuple)ev.getData();
		Logger.debug(LOG_TAG, getName(), "Received tuple "+tuple.getCloudletId()+"on "+tuple.getDestModuleName());
		String srcModule = tuple.getSrcModuleName();
		String destModule = tuple.getDestModuleName();
		Application app = getApplication();
		
		for(AppLoop loop : app.getLoops()){
			if(loop.hasEdge(srcModule, destModule) && loop.isEndModule(destModule)){
				
				Double startTime = TimeKeeper.getInstance().getEmitTimes().get(tuple.getActualTupleId());
				if(startTime==null)
					break;
				if(!TimeKeeper.getInstance().getLoopIdToCurrentAverage().containsKey(loop.getLoopId())){
					TimeKeeper.getInstance().getLoopIdToCurrentAverage().put(loop.getLoopId(), 0.0);
					TimeKeeper.getInstance().getLoopIdToCurrentNum().put(loop.getLoopId(), 0);
				}
				double currentAverage = TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loop.getLoopId());
				int currentCount = TimeKeeper.getInstance().getLoopIdToCurrentNum().get(loop.getLoopId());
				double delay = CloudSim.clock()- TimeKeeper.getInstance().getEmitTimes().get(tuple.getActualTupleId());
				TimeKeeper.getInstance().getEmitTimes().remove(tuple.getActualTupleId());
				double newAverage = (currentAverage*currentCount + delay)/(currentCount+1);
				TimeKeeper.getInstance().getLoopIdToCurrentAverage().put(loop.getLoopId(), newAverage);
				TimeKeeper.getInstance().getLoopIdToCurrentNum().put(loop.getLoopId(), currentCount+1);
				break;
			}
		}
	}

	@Override
	public void shutdownEntity() {
		
	}

	public int getGatewayDeviceId() {
		return gatewayDeviceId;
	}

	public void setGatewayDeviceId(int gatewayDeviceId) {
		this.gatewayDeviceId = gatewayDeviceId;
	}

	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(GeoLocation geoLocation) {
		this.geoLocation = geoLocation;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getActuatorType() {
		return actuatorType;
	}

	public void setActuatorType(String actuatorType) {
		this.actuatorType = actuatorType;
	}

	public Application getApp() {
		return app;
	}

	public void setApp(Application app) {
		this.app = app;
	}

	public double getLatency() {
		return latency;
	}

	public void setLatency(double latency) {
		this.latency = latency;
	}

	public ActuatorCharacteristics getCharacteristics() {
		return characteristics;
	}

	public void setCharacteristics(ActuatorCharacteristics characteristics) {
		this.characteristics = characteristics;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public int getEndDeviceId() {
		return endDeviceId;
	}

	public void setEndDeviceId(int endDeviceId) {
		this.endDeviceId = endDeviceId;
	}

}
