/*
 * Title:        iFogSim Toolkit
 * Description:  iFogSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 *
 */

package org.fog.network;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.utils.Logger;

public class PhysicalTopology {
	
	/**
	 * Singleton object that needs to be manipulated in the example script
	 */
	private static PhysicalTopology instance = null;
	
	public static PhysicalTopology getInstance() {
		if (instance == null) 
			instance = new PhysicalTopology();
		return instance;
	}
	
	private List<FogDevice> fogDevices;
	private List<Sensor> sensors;
	private List<Actuator> actuators;
	private List<Switch> switches;
	private List<Link> links;

	/**
	 * This function validates the physical topology on start of simulation. A valid topology adheres to following conditions :
	 * <ul>
	 * <li> No self-loop link should be present.
	 * <li> Each fog device should be connected to network by a unique link.
	 * 
	 * </ul>
	 * @return true if topology is valid
	 */
	public boolean validateTopology() {
		Map<Integer, Integer> devToNumLinks = new HashMap<Integer, Integer>();
		for (FogDevice dev : getFogDevices()) {
			devToNumLinks.put(dev.getId(), 0);
		}
		for (Link l : getLinks()) {
			if (l.getEndpointNorth() == l.getEndpointSouth())
				return false;
			if (devToNumLinks.containsKey(l.getEndpointNorth())) {
				if (devToNumLinks.get(l.getEndpointNorth()) == 1)
					return false;
				else 
					devToNumLinks.put(l.getEndpointNorth(), 1);
			}
			if (devToNumLinks.containsKey(l.getEndpointSouth())) {
				if (devToNumLinks.get(l.getEndpointSouth()) == 1)
					return false;
				else 
					devToNumLinks.put(l.getEndpointSouth(), 1);
			}
		}
		
		return true;
	}
	
	public void setUpEntities() {
		calculateRoutingTables();
		assignLinksToFogDevices();
	}
	
	public void calculateRoutingTables() {
		/**
		 * SwId --> { Dest --> (Next Hop, Num Hops) }
		 */
		Map<Integer, Map<Integer, Pair<Integer, Integer>>> routingTables;
		routingTables = new HashMap<Integer, Map<Integer, Pair<Integer, Integer>>>();
		
		int numNodes = getFogDevices().size();
		
		for (Switch sw : switches) {
			routingTables.put(sw.getId(), new HashMap<Integer, Pair<Integer, Integer>>());
			for (int adjEntity : sw.getAdjacentEntities()) {
				routingTables.get(sw.getId()).put(adjEntity, new Pair<Integer, Integer>(adjEntity, 0));
			}
		}
		
		while (!isRoutingConverged(routingTables, numNodes)) {
			for (Switch sw : switches) {
				Map<Integer, Pair<Integer, Integer>> table = routingTables.get(sw.getId());
				for (int neighbour : sw.getNeighbourSwitches()) {
					for (Integer dst : table.keySet()) {
						updateRoutingTable(routingTables, neighbour, dst, 
								sw.getId(), table.get(dst).getSecond()+1);
					}
				}
			}
		}
		
		for (Switch sw : getSwitches()) {
			Map<Integer, Pair<Integer, Integer>> routingTable = routingTables.get(sw.getId());
			for (Integer dst : routingTable.keySet()) {
				int nextHop = routingTable.get(dst).getFirst();
				Link link = getLink(sw.getId(), nextHop);
				if (link != null)
					sw.getSwitchingTable().put(dst, link.getId());
				else 
					Logger.error("PhysicalTopology", "Link connecting endpoints "+dst+" and "+nextHop+" not found.");
			}
		}
	}
	
	private void assignLinksToFogDevices() {
		for (FogDevice dev : getFogDevices()) {
			for (Link l : getLinks()) {
				if (l.getEndpointNorth() == dev.getId() || l.getEndpointSouth() == dev.getId())
					dev.setLinkId(l.getId());
			}
		}
	}

	/**
	 * Routing table is declared converged when each switch knows where to send a packet destined for any fog device.
	 * That is, size of routing table at each switch equals <b>numNodes</b>. 
	 * @param routingTables currently routing table to be checked for covergence
	 * @param numNodes number of fog devices in the system
	 * @return true if routing tables have converged
	 */
	private boolean isRoutingConverged(Map<Integer, Map<Integer, Pair<Integer, Integer>>> routingTables, int numNodes) {
		for (Integer swId : routingTables.keySet()) {
			if (routingTables.get(swId).size() < numNodes)
				return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param routingTables
	 * @param swId
	 * @param dst
	 * @param nextHop
	 * @param numHops
	 */
	private void updateRoutingTable(Map<Integer, Map<Integer, Pair<Integer, Integer>>> routingTables,
			int swId, int dst, int nextHop, int numHops) {
		if (routingTables.get(swId).containsKey(dst)) {
			int currHops = routingTables.get(swId).get(dst).getSecond();
			if (numHops < currHops) {
				routingTables.get(swId).put(dst, new Pair<Integer, Integer>(nextHop, numHops));
			}
		} else {
			routingTables.get(swId).put(dst, new Pair<Integer, Integer>(nextHop, numHops));
		}
	}
	
	/**
	 * Get link for connecting specified endpoints.
	 * @param endpoint1 ID of first endpoint
	 * @param endpoint2 ID of second endpoint
	 * @return the link connecting specified endpoints. If no such link, return null.
	 */
	private Link getLink(int endpoint1, int endpoint2) {
		Link res = null;
		for (Link l : getLinks()) {
			if ((l.getEndpointNorth() == endpoint1 && l.getEndpointSouth() == endpoint2) ||
					(l.getEndpointSouth() == endpoint1 && l.getEndpointNorth() == endpoint2))
				return res;
		}
		return null;
	}
	
	
	
	public List<FogDevice> getFogDevices() {
		return fogDevices;
	}
	public void setFogDevices(List<FogDevice> fogDevices) {
		this.fogDevices = fogDevices;
	}
	public List<Sensor> getSensors() {
		return sensors;
	}
	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}
	public List<Actuator> getActuators() {
		return actuators;
	}
	public void setActuators(List<Actuator> actuators) {
		this.actuators = actuators;
	}
	public List<Switch> getSwitches() {
		return switches;
	}
	public void setSwitches(List<Switch> switches) {
		this.switches = switches;
	}
	public List<Link> getLinks() {
		return links;
	}
	public void setLinks(List<Link> links) {
		this.links = links;
	}
	
	private PhysicalTopology() {
		setLinks(new ArrayList<Link>());
		setFogDevices(new ArrayList<FogDevice>());
		setSwitches(new ArrayList<Switch>());
	}

	public static void main(String args[]) {
		int num_user = 1; // number of cloud users
		Calendar calendar = Calendar.getInstance();
		boolean trace_flag = false; // mean trace events

		CloudSim.init(num_user, calendar, trace_flag);
		
		PhysicalTopology topo = new PhysicalTopology();
		int numSwitches = 10;
		
		List<Switch> sws = new ArrayList<Switch>();
		for (int i = 0;i<numSwitches;i++) {
			Switch sw = new Switch("sw-"+i);
			sws.add(sw);
		}
		
		int base = 100;
		for (int i = 0;i<numSwitches;i++) {
			sws.get(i).getAdjacentEntities().add(base++);
			sws.get(i).getAdjacentEntities().add(base++);
			int n = (i+1)%sws.size();
			int p = (sws.size()+(i-1))%sws.size();
			sws.get(i).getNeighbourSwitches().add(sws.get(n).getId());
			sws.get(i).getNeighbourSwitches().add(sws.get(p).getId());
		}
		
		topo.setSwitches(sws);
		topo.calculateRoutingTables();
	}
}