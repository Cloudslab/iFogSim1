package org.fog.network;

import java.util.Queue;

import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;

public class Link extends SimEntity {

	protected Queue<Tuple> northTupleQueue;
	protected Queue<Tuple> southTupleQueue;
	boolean isNorthLinkBusy;
	boolean isSouthLinkBusy;
	
	/**
	 * Latency in milliseconds
	 */
	private double latency;
	
	/**
	 * Bandwidth in Mbps.
	 * If value of member = B, uplink BW = B and downlink BW = B independently.
	 */
	private double bandwidth;
	
	private int endpointNorth;
	
	private int endpointSouth;
	
	public Link(String name, double latency, double bandwidth, int endpointNorth, int endpointSouth) {
		super(name);
		setLatency(latency);
		setBandwidth(bandwidth);
		setEndpointNorth(endpointNorth);
		setEndpointSouth(endpointSouth);
	}
	
	public Link(String name) {
		super(name);
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processEvent(SimEvent ev) {
		int tag = ev.getTag();
		switch (tag) {
		case FogEvents.UPDATE_NORTH_TUPLE_QUEUE:
			updateNorthTupleQueue();
			break;
		case FogEvents.UPDATE_SOUTH_TUPLE_QUEUE:
			updateSouthTupleQueue();
			break;
		case FogEvents.TUPLE_ARRIVAL:
			processTupleArrival(ev);
			break;
		}		
	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		
	}

	private void processTupleArrival(SimEvent ev) {
		Tuple tuple = (Tuple) ev.getData();
		if (ev.getSource() == endpointNorth)
			sendSouth(tuple);
		else if (ev.getSource() == endpointSouth)
			sendNorth(tuple);
	}
	
	protected void updateSouthTupleQueue(){
		if(!getSouthTupleQueue().isEmpty()){
			Tuple tuple = getSouthTupleQueue().poll();
			sendSouthFreeLink(tuple);
		}else{
			setSouthLinkBusy(false);
		}
	}
	
	protected void sendSouthFreeLink(Tuple tuple){
		double networkDelay = tuple.getCloudletFileSize()/getBandwidth();
		setSouthLinkBusy(true);
		send(getId(), networkDelay, FogEvents.UPDATE_SOUTH_TUPLE_QUEUE);
		send(endpointSouth, networkDelay + getLatency(), FogEvents.TUPLE_ARRIVAL, tuple);
	}
	
	protected void sendSouth(Tuple tuple){
		if(endpointSouth > 0){
			if(!isSouthLinkBusy()){
				sendSouthFreeLink(tuple);
			}else{
				southTupleQueue.add(tuple);
			}
		}
	}

	protected void updateNorthTupleQueue(){
		if(!getNorthTupleQueue().isEmpty()){
			Tuple tuple = getNorthTupleQueue().poll();
			sendNorthFreeLink(tuple);
		}else{
			setNorthLinkBusy(false);
		}
	}
	
	protected void sendNorthFreeLink(Tuple tuple){
		double networkDelay = tuple.getCloudletFileSize()/getBandwidth();
		setNorthLinkBusy(true);
		send(getId(), networkDelay, FogEvents.UPDATE_NORTH_TUPLE_QUEUE);
		send(endpointNorth, networkDelay + getLatency(), FogEvents.TUPLE_ARRIVAL, tuple);
	}
	
	protected void sendNorth(Tuple tuple){
		if(endpointNorth > 0){
			if(!isNorthLinkBusy()){
				sendNorthFreeLink(tuple);
			}else{
				northTupleQueue.add(tuple);
			}
		}
	}
	
	public double getLatency() {
		return latency;
	}

	public void setLatency(double latency) {
		this.latency = latency;
	}

	public double getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(double bandwidth) {
		this.bandwidth = bandwidth;
	}

	public int getEndpointNorth() {
		return endpointNorth;
	}

	public void setEndpointNorth(int endpointNorth) {
		this.endpointNorth = endpointNorth;
	}

	public int getEndpointSouth() {
		return endpointSouth;
	}

	public void setEndpointSouth(int endpointSouth) {
		this.endpointSouth = endpointSouth;
	}

	public Queue<Tuple> getNorthTupleQueue() {
		return northTupleQueue;
	}

	public void setNorthTupleQueue(Queue<Tuple> northTupleQueue) {
		this.northTupleQueue = northTupleQueue;
	}

	public Queue<Tuple> getSouthTupleQueue() {
		return southTupleQueue;
	}

	public void setSouthTupleQueue(Queue<Tuple> southTupleQueue) {
		this.southTupleQueue = southTupleQueue;
	}

	public boolean isNorthLinkBusy() {
		return isNorthLinkBusy;
	}

	public void setNorthLinkBusy(boolean isNorthLinkBusy) {
		this.isNorthLinkBusy = isNorthLinkBusy;
	}

	public boolean isSouthLinkBusy() {
		return isSouthLinkBusy;
	}

	public void setSouthLinkBusy(boolean isSouthLinkBusy) {
		this.isSouthLinkBusy = isSouthLinkBusy;
	}
}
