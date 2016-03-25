package org.fog.entities;

import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.placement.Controller;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.GeoLocation;
import org.fog.utils.Logger;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.Distribution;

public class Sensor extends SimEntity{

	private int gatewayDeviceId;
	private GeoLocation geoLocation;
	private long length;
	private long fileSize;
	private long outputSize;
	private double lastTransmitTime = -1;
	private String appId;
	private int userId;
	private String tupleType;
	private String sensorName;
	private int tupleCpuSize;
	private int tupleNwSize;
	private String destModuleName;
	private Distribution transmitDistribution;
	private int controllerId;
	private Application app;
	
	public Sensor(String name, int userId, String appId, int gatewayDeviceId, GeoLocation geoLocation, 
			Distribution transmitDistribution, int cpuLength, int nwLength, String tupleType, String destModuleName) {
		super(name);
		this.setAppId(appId);
		this.gatewayDeviceId = gatewayDeviceId;
		this.geoLocation = geoLocation;
		this.length = cpuLength;
		this.fileSize = nwLength;
		this.outputSize = 3;
		this.setTransmitDistribution(transmitDistribution);
		setUserId(userId);
		setTupleCpuSize(cpuLength);
		setTupleNwSize(nwLength);
		setDestModuleName(destModuleName);
		setTupleType(tupleType);
		setSensorName(tupleType);
	}
	
	public void transmit(double delay){
		
		Tuple tuple = new Tuple(getAppId(), FogUtils.generateTupleId(), Tuple.UP, length, 1, fileSize, outputSize, 
				new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
		tuple.setUserId(getUserId());
		tuple.setTupleType(getTupleType());
		
		tuple.setDestModuleName(getDestModuleName());
		tuple.setSrcModuleName(getSensorName());
		System.out.println(getSensorName());
		//Logger.debug(getName(), "Sending tuple with tupleId = "+tuple.getCloudletId());
		
		int actualTupleId = updateTimings(getSensorName(), getDestModuleName());
		System.out.println(actualTupleId);
		tuple.setActualTupleId(actualTupleId);
		
		send(gatewayDeviceId, delay, FogEvents.TUPLE_ARRIVAL,tuple);
		
		lastTransmitTime = CloudSim.clock();
	}
	
	private int updateTimings(String src, String dest){
		Application application = getApp();
		for(AppLoop loop : application.getLoops()){
			if(loop.hasEdge(src, dest)){
				return TimeKeeper.getInstance().getUniqueId();
			}
		}
		return -1;
	}
	
	@Override
	public void startEntity() {
		send(gatewayDeviceId, Math.random()*10+CloudSim.getMinTimeBetweenEvents(), FogEvents.SENSOR_JOINED, geoLocation);
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
		case FogEvents.TUPLE_ACK:
			transmit(transmitDistribution.getNextValue());
			break;
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

	public double getLastTransmitTime() {
		return lastTransmitTime;
	}

	public void setLastTransmitTime(double lastTransmitTime) {
		this.lastTransmitTime = lastTransmitTime;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getTupleCpuSize() {
		return tupleCpuSize;
	}

	public void setTupleCpuSize(int tupleCpuSize) {
		this.tupleCpuSize = tupleCpuSize;
	}

	public int getTupleNwSize() {
		return tupleNwSize;
	}

	public void setTupleNwSize(int tupleNwSize) {
		this.tupleNwSize = tupleNwSize;
	}

	public String getTupleType() {
		return tupleType;
	}

	public void setTupleType(String tupleType) {
		this.tupleType = tupleType;
	}

	public String getSensorName() {
		return sensorName;
	}

	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getDestModuleName() {
		return destModuleName;
	}

	public void setDestModuleName(String destModuleName) {
		this.destModuleName = destModuleName;
	}

	public Distribution getTransmitDistribution() {
		return transmitDistribution;
	}

	public void setTransmitDistribution(Distribution transmitDistribution) {
		this.transmitDistribution = transmitDistribution;
	}

	public int getControllerId() {
		return controllerId;
	}

	public void setControllerId(int controllerId) {
		this.controllerId = controllerId;
	}

	public Application getApp() {
		return app;
	}

	public void setApp(Application app) {
		this.app = app;
	}

}
