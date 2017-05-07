/*
 * Title:        iFogSim Toolkit
 * Description:  iFogSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 *
 */

package org.fog.entities;

import org.fog.utils.GeoLocation;

public class ActuatorCharacteristics {

	private int id;
	private GeoLocation geoLocation;
	private String actuatorType;
	
	public ActuatorCharacteristics(int id, GeoLocation geoLocation, String actuatorType) {
		setId(id);
		setGeoLocation(geoLocation);
		setActuatorType(actuatorType);
	}
	
	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(GeoLocation geoLocation) {
		this.geoLocation = geoLocation;
	}

	public String getActuatorType() {
		return actuatorType;
	}

	public void setActuatorType(String actuatorType) {
		this.actuatorType = actuatorType;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
}
