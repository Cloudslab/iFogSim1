package org.fog.entities;

import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.GeoLocation;
import org.fog.utils.TupleEmitTimes;

public class Sensor extends SimEntity{

	private int gatewayDeviceId;
	private GeoLocation geoLocation;
	private long length;
	private long fileSize;
	private long outputSize;
	private double lastTransmitTime = -1;
	private double transmitInterval;
	private String queryId;
	private int userId;
	private String sensorType;
	private int tupleCpuSize;
	private int tupleNwSize;
	private String spoutName;
	
	public Sensor(String name, int userId, String queryId, int gatewayDeviceId, GeoLocation geoLocation, double transmitInterval, int cpuLength, int nwLength, String type, String spoutName) {
		super(name);
		this.setQueryId(queryId);
		this.gatewayDeviceId = gatewayDeviceId;
		this.geoLocation = geoLocation;
		this.length = cpuLength;
		this.fileSize = nwLength;
		this.outputSize = 3;
		this.setTransmitInterval(transmitInterval);
		setUserId(userId);
		setSensorType(type);
		System.out.println(this.sensorType);
		setTupleCpuSize(cpuLength);
		setTupleNwSize(nwLength);
		setSpoutName(spoutName);
	}
	
	public void transmit(double delay){
		
		Tuple tuple = new Tuple(getQueryId(), FogUtils.generateTupleId(), length, 1, fileSize, outputSize, new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
		tuple.setUserId(getUserId());
		tuple.setSensorType(getSensorType());
		tuple.setActualTupleId(FogUtils.generateActualTupleId());
		//System.out.println((CloudSim.clock()+delay)+" : Sensor "+getName()+" sending actual tuple id "+tuple.getActualTupleId());
		//System.out.println(CloudSim.getEntityName(gatewayDeviceId));
		tuple.setDestOperatorId(getSpoutName());
		tuple.setSrcOperatorId("sensor-"+getSensorType()+"-");
		tuple.setEmitTime(CloudSim.clock()+delay);
		//TupleEmitTimes.getInstance().setEmitTime(tuple.getActualTupleId(), CloudSim.clock()+delay);
		send(gatewayDeviceId, delay, FogEvents.TUPLE_ARRIVAL,tuple);
		
		lastTransmitTime = CloudSim.clock();
	}
	
	@Override
	public void startEntity() {
		send(gatewayDeviceId, Math.random()*10+CloudSim.getMinTimeBetweenEvents(), FogEvents.SENSOR_JOINED, geoLocation);
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
		case FogEvents.TUPLE_ACK:
			//System.out.println("Tuple ack received at time \t"+CloudSim.clock());
			transmit(transmitInterval);
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

	public double getTransmitInterval() {
		return transmitInterval;
	}

	public void setTransmitInterval(double transmitInterval) {
		this.transmitInterval = transmitInterval;
	}

	public double getLastTransmitTime() {
		return lastTransmitTime;
	}

	public void setLastTransmitTime(double lastTransmitTime) {
		this.lastTransmitTime = lastTransmitTime;
	}

	public String getQueryId() {
		return queryId;
	}

	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getSensorType() {
		return sensorType;
	}

	public void setSensorType(String sensorType) {
		this.sensorType = sensorType;
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

	public String getSpoutName() {
		return spoutName;
	}

	public void setSpoutName(String spoutName) {
		this.spoutName = spoutName;
	}

}
