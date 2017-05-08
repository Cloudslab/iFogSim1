/*
 * Title:        iFogSim Toolkit
 * Description:  iFogSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 *
 */

package org.fog.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.entities.Actuator;
import org.fog.entities.EndDevice;
import org.fog.entities.FogDevice;
import org.fog.utils.Logger;

public class PhysicalTopology {
	private static String LOG_TAG = "PHYSICAL_TOPO";
	
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
	private List<EndDevice> endDevices;
	private List<Switch> switches;
	private List<Link> links;

	public void addLink(int endpoint1, int endpoint2, double latency, double bandwidth) {
		getLinks().add(new Link("link-"+endpoint1+"-"+endpoint2, latency, bandwidth, endpoint1, endpoint2));
	}
	
	public void addFogDevice(FogDevice dev) {
		getFogDevices().add(dev);
	}

	public void addEndDevice(EndDevice dev) {
		getEndDevices().add(dev);
	}
	
	public void addSwitch(Switch sw) {
		getSwitches().add(sw);
	}
	
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
		assignLinksToFogDevices();
		assignLinksToEndDevices();
		calculateAdjacentEntities();
		calculateNeighbourSwitches();
		printAdjacentEntities();
		calculateRoutingTables();
	}
	
	private void calculateNeighbourSwitches() {
		for (Switch sw : getSwitches()) {
			for (Link l : getLinks()) {
				int neighbour = -1;
				if (l.getEndpointNorth() == sw.getId()) {
					neighbour = l.getEndpointSouth();
				} else if (l.getEndpointSouth() == sw.getId()) {
					neighbour = l.getEndpointNorth();
				}
				Switch neighbourSw = getSwitch(neighbour);
				if (neighbourSw != null && neighbour != -1) {
					sw.getNeighbourSwitches().add(neighbour);
				}
			}
		}
		
	}

	/**
	 * Calculates 
	 */
	private void calculateAdjacentEntities() {
		Logger.debug(LOG_TAG, "Calculating adjacent entities");
		// Actuators are just present in the routing table
		// They are used as the destination ID when sending tuples from modules
		// At end of routing, tuple would reach the EndDevice, which would then send it to Actuator
		for (FogDevice dev : getFogDevices()) {
			Link link = getLink(dev.getLinkId());
			int swId = link.getOtherEndpoint(dev.getId());
			Switch sw = getSwitch(swId);
			sw.getAdjacentEntities().add(dev.getId());
		}
		
		for (EndDevice dev : getEndDevices()) {
			Link link = getLink(dev.getLinkId());
			int swId = link.getOtherEndpoint(dev.getId());
			Switch sw = getSwitch(swId);
			sw.getAdjacentEndDevices().add(dev.getId());
		}
	}

	private Switch getSwitch(int id) {
		Switch res = null;
		for (Switch sw : getSwitches()) {
			if (sw.getId() == id) 
				res = sw;
		}
		return res;
	}
	
	private Link getLink(int id) {
		Link res = null;
		for (Link l : getLinks()) {
			if (l.getId() == id) 
				res = l;
		}
		return res;
	}
	
	private List<Actuator> getActuators() {
		List<Actuator> actuators = new ArrayList<Actuator>();
		for (EndDevice d : getEndDevices()) {
			for (Actuator a : d.getActuators())
				actuators.add(a);
		}
		return actuators;
	}
	
	public void calculateRoutingTables() {
		/**
		 * SwId --> { Dest --> (Next Hop, Num Hops) }
		 */
		Logger.debug(LOG_TAG, "Calculating routing tables");
		Map<Integer, Map<Integer, Pair<Integer, Integer>>> routingTables;
		routingTables = new HashMap<Integer, Map<Integer, Pair<Integer, Integer>>>();
		
		int numNodes = getFogDevices().size() + getActuators().size();
		
		for (Switch sw : switches) {
			routingTables.put(sw.getId(), new HashMap<Integer, Pair<Integer, Integer>>());
			for (int adjEntity : sw.getAdjacentEntities()) {
				routingTables.get(sw.getId()).put(adjEntity, new Pair<Integer, Integer>(adjEntity, 0));
			}
			for (int adjDevId : sw.getAdjacentEndDevices()) {
				EndDevice dev = getEndDevice(adjDevId);
				
				for (Actuator a : dev.getActuators())
					routingTables.get(sw.getId()).put(a.getId(), new Pair<Integer, Integer>(dev.getId(), 0));
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
		printRoutingTables(routingTables);
		for (Switch sw : getSwitches()) {
			Map<Integer, Pair<Integer, Integer>> routingTable = routingTables.get(sw.getId());
			for (Integer dst : routingTable.keySet()) {
				int nextHop = routingTable.get(dst).getFirst();
				Link link = getLink(sw.getId(), nextHop);
				if (link != null)
					sw.getSwitchingTable().put(dst, link.getId());
				else {
					Logger.error(LOG_TAG, "Sw : "+sw.getName());
					Logger.error(LOG_TAG, "Link connecting endpoints "+sw.getName()
							+" and "+CloudSim.getEntityName(nextHop)+" not found.");
				}
			}
		}
	}
	
	private EndDevice getEndDevice(int adjDevId) {
		EndDevice dev = null;
		for (EndDevice e : getEndDevices()) {
			if (e.getId() == adjDevId)
				dev = e;
		}
		return dev;
	}

	private void assignLinksToFogDevices() {
		for (FogDevice dev : getFogDevices()) {
			for (Link l : getLinks()) {
				if (l.getEndpointNorth() == dev.getId() || l.getEndpointSouth() == dev.getId())
					dev.setLinkId(l.getId());
			}
		}
	}

	private void assignLinksToEndDevices() {
		for (EndDevice dev : getEndDevices()) {
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
		Logger.debug(LOG_TAG, "numNodes = "+numNodes);
		for (Integer swId : routingTables.keySet()) {
			if (routingTables.get(swId).size() < numNodes)
				return false;
		}
		Logger.debug(LOG_TAG, "CONVERGED !");
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
		for (Link l : getLinks()) {
			if ((l.getEndpointNorth() == endpoint1 && l.getEndpointSouth() == endpoint2) ||
					(l.getEndpointSouth() == endpoint1 && l.getEndpointNorth() == endpoint2))
				return l;
		}
		return null;
	}
	
	private void printAdjacentEntities() {
		for (Switch sw : getSwitches()) {
			System.out.println(CloudSim.getEntityName(sw.getId()));
			for (Integer i : sw.getAdjacentEntities()) {
				System.out.print("\t");
				System.out.println(CloudSim.getEntityName(i));
			}
		}
	}
	
	private void printRoutingTables(Map<Integer, Map<Integer, Pair<Integer, Integer>>> routingTables) {
		System.out.println("------------------------------");
		for (Integer swId : routingTables.keySet()) {
			Map<Integer, Pair<Integer, Integer>> routingTable = routingTables.get(swId);
			System.out.println(CloudSim.getEntityName(swId));
			
			for (Integer dst : routingTable.keySet()) {
				System.out.print("\t");
				System.out.println(CloudSim.getEntityName(dst) +" ---> "+ CloudSim.getEntityName(routingTable.get(dst).getFirst()));
			}
		}
	}	
	
	public List<FogDevice> getFogDevices() {
		return fogDevices;
	}
	public void setFogDevices(List<FogDevice> fogDevices) {
		this.fogDevices = fogDevices;
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
		setEndDevices(new ArrayList<EndDevice>());
	}

/*	public static void main(String args[]) {
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
*/
	public List<EndDevice> getEndDevices() {
		return endDevices;
	}

	public void setEndDevices(List<EndDevice> endDevices) {
		this.endDevices = endDevices;
	}
}