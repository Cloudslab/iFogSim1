package org.fog.models.mobility;

import org.fog.utils.GeoLocation;

public abstract class MobilityModel {
	
	private GeoLocation initialLocation;
	private GeoLocation currentLocation;
	private double lastTime;
	
	public MobilityModel(GeoLocation initialLocation) {
		setInitialLocation(initialLocation);
		setCurrentLocation(initialLocation);
		setLastTime(0);
	}
	
	public abstract GeoLocation getUpdatedLocation(double lastTime, GeoLocation lastLocation, double currTime);
	
	public GeoLocation getCurrentLocation(double time) {
		updatePosition(time);
		return getCurrentLocation();
	}
	
	private void updatePosition(double time) {
		setCurrentLocation(getUpdatedLocation(getLastTime(), getCurrentLocation(), time));
		setLastTime(time);
	}

	public GeoLocation getInitialLocation() {
		return initialLocation;
	}

	public void setInitialLocation(GeoLocation initialLocation) {
		this.initialLocation = initialLocation;
	}

	public GeoLocation getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(GeoLocation currentLocation) {
		this.currentLocation = currentLocation;
	}

	public double getLastTime() {
		return lastTime;
	}

	public void setLastTime(double lastTime) {
		this.lastTime = lastTime;
	}
}
