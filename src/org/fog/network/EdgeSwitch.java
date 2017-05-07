package org.fog.network;

import org.fog.utils.GeoLocation;

public class EdgeSwitch extends Switch{

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
