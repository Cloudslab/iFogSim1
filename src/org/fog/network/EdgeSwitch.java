/*
 * Title:        iFogSim Toolkit
 * Description:  iFogSim (Fog Simulation) Toolkit for Modeling and Simulation of Fog Computing
 */
package org.fog.network;

import org.fog.utils.GeoLocation;

/**
 * A switch on the network edge to which end-devices connect to.
 * Can be used to model WiFi APs, Cellular base stations, etc.
 * 
 * @author Harshit Gupta
 * @since iFogSim 2.0
 */
public class EdgeSwitch extends Switch{

	/**
	 * Geographical location of edge switch
	 */
	private GeoLocation geoLocation;
	
	public EdgeSwitch(String name) {
		super(name);
	}
	
	public boolean isCoreSwitch() {
		return false;
	}

	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(GeoLocation geoLocation) {
		this.geoLocation = geoLocation;
	}

}
