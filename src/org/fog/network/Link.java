/*
 * Title:        iFogSim Toolkit
 * Description:  iFogSim (Fog Simulation) Toolkit for Modeling and Simulation of Fog Computing
 */
package org.fog.network;

import java.util.LinkedList;
import java.util.Queue;

import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;
import org.fog.utils.Logger;

/**
 * Point-to-point network link connecting two entities.
 * Each link is of the form (A----B) where A and B are entities connected by it.
 * Each link has two directions : North and South. For instance, A-->B can (arbitrarily) be North and B-->A South.
 * @author Harshit Gupta
 * @since iFogSim 2.0
 */
public class Link extends SimEntity {

	private static String LOG_TAG = "LINK";
	
	/**
	 * Queue holding packets to be sent North 
	 */
	protected Queue<Tuple> northTupleQueue;
	/**
	 * Queue holding packets to be sent South 
	 */
	protected Queue<Tuple> southTupleQueue;
	/**
	 * Flag indicating status of North direction
	 */
	boolean isNorthLinkBusy;
	/**
	 * Flag indicating status of South direction
	 */
	boolean isSouthLinkBusy;
	
	/**
	 * Latency in milliseconds, same for both North and South directions.
	 */
	private double latency;
	
	/**
	 * Bandwidth in Mbps.
	 * If value of member = B, North BW = B and South BW = B independently.
	 */
	private double bandwidth;
	
	/**
	 * ID of entity on the North end.
	 */
	private int endpointNorth;
	/**
	 * ID of entity on the South end.
	 */
	private int endpointSouth;
	
	public Link(String name, double latency, double bandwidth, int endpointNorth, int endpointSouth) {
		super(name);
		setLatency(latency);
		setBandwidth(bandwidth);
		setEndpointNorth(endpointNorth);
		setEndpointSouth(endpointSouth);
		setNorthTupleQueue(new LinkedList<Tuple>());
		setSouthTupleQueue(new LinkedList<Tuple>());
	}
	
	public Link(String name) {
		super(name);
		setNorthTupleQueue(new LinkedList<Tuple>());
		setSouthTupleQueue(new LinkedList<Tuple>());
	}

	@Override
	public void startEntity() {
		
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
		
	}

	/**
	 * Handler for processing an incoming tuple.
	 * @param ev
	 */
	private void processTupleArrival(SimEvent ev) {
		Tuple tuple = (Tuple) ev.getData();
		if (ev.getSource() == endpointNorth)  // checks if tuple was received from the North endpoint
			sendSouth(tuple);  
		else if (ev.getSource() == endpointSouth)  // checks if tuple was received from the South endpoint
			sendNorth(tuple);
	}
	
	/**
	 * Updates the status of South queue.
	 */
	protected void updateSouthTupleQueue(){
		if(!getSouthTupleQueue().isEmpty()){  // if there are more tuples to send South
			Tuple tuple = getSouthTupleQueue().poll();  // get next tuple from South queue
			sendSouthFreeLink(tuple);  // send tuple South
		}else{
			setSouthLinkBusy(false);  // if no more tuples to be sent South, mark South link as FREE
		}
	}
	
	/**
	 * Send a tuple in South direction. 
	 * Link bandwidth can be used for sending this tuple in a dedicated manner.
	 * @param tuple Tuple to be sent
	 */
	protected void sendSouthFreeLink(Tuple tuple){
		double sizeInBits = tuple.getCloudletFileSize() * 8;
		double bwInBitsPerSecond = getBandwidth() * 1024 * 1024;
		double transmissionDelay = 1000*(sizeInBits/bwInBitsPerSecond);
		Logger.debug(LOG_TAG, "SizeInBits = "+sizeInBits);
		Logger.debug(LOG_TAG, "Transmission delay = "+transmissionDelay );
		setSouthLinkBusy(true); // South link has begun sending this tuple. Marking it as busy so next tuples are queued until this is sent. 
		send(getId(), transmissionDelay , FogEvents.UPDATE_SOUTH_TUPLE_QUEUE);	// update South link once transmission is complete
		send(endpointSouth, transmissionDelay  + getLatency(), FogEvents.TUPLE_ARRIVAL, tuple);	// Sent tuple arrives at other end of link after given delay
	}
	
	/**
	 * Send a tuple in South direction to endpoint connected at South end of link.
	 * @param tuple Tuple to be sent
	 */
	protected void sendSouth(Tuple tuple){
		if(endpointSouth > 0){
			if(!isSouthLinkBusy()){
				// if South link is not busy sending a tuple already
				sendSouthFreeLink(tuple);	// send this tuple immediately
			}else{
				southTupleQueue.add(tuple);	// queue this tuple for later transmission
			}
		}
	}
	
	/**
	 * Updates the status of North queue.
	 */
	protected void updateNorthTupleQueue(){
		if(!getNorthTupleQueue().isEmpty()){  // if there are more tuples to send North
			Tuple tuple = getNorthTupleQueue().poll();  // get next tuple from North queue
			sendNorthFreeLink(tuple);  // send tuple North
		}else{
			setNorthLinkBusy(false);  // if no more tuples to be sent North, mark North link as FREE
		}
	}
	
	/**
	 * Send a tuple in North direction. 
	 * Link bandwidth can be used for sending this tuple in a dedicated manner.
	 * @param tuple Tuple to be sent
	 */
	protected void sendNorthFreeLink(Tuple tuple){
		double sizeInBits = tuple.getCloudletFileSize() * 8;
		double bwInBitsPerSecond = getBandwidth() * 1024 * 1024;
		double networkDelay = 1000*(sizeInBits/bwInBitsPerSecond);
		Logger.debug(LOG_TAG, "Transm	ission delay = "+networkDelay);
		setNorthLinkBusy(true);  // North link has begun sending this tuple. Marking it as busy so next tuples are queued until this is sent.
		send(getId(), networkDelay, FogEvents.UPDATE_NORTH_TUPLE_QUEUE);  // update North link once transmission is complete
		send(endpointNorth, networkDelay + getLatency(), FogEvents.TUPLE_ARRIVAL, tuple);  // Sent tuple arrives at other end of link after given delay
	}
	
	/**
	 * Send a tuple in North direction to endpoint connected at North end of link.
	 * @param tuple Tuple to be sent
	 */
	protected void sendNorth(Tuple tuple){
		if(endpointNorth > 0){
			if(!isNorthLinkBusy()){
				// if North link is not busy sending a tuple already
				sendNorthFreeLink(tuple);  // send this tuple immediately
			}else{
				northTupleQueue.add(tuple);	// queue this tuple for later transmission
			}
		}
	}
	
	/**
	 * Get other endpoint of the link
	 * @param endpoint given endpoint
	 * @return
	 */
	public int getOtherEndpoint(int endpoint) {
		if (getEndpointNorth() == endpoint)
			return getEndpointSouth();
		else if (getEndpointSouth() == endpoint)
			return getEndpointNorth();
		else 
			return -1;
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
