package org.fog.models.mobility;

import org.fog.utils.GeoLocation;

public abstract class MobilityModel {
	
	/**
	 * Initial location
	 */
	private GeoLocation initialLocation;
	
	/**
	 * Current location
	 */
	private GeoLocation currentLocation;
	
	/**
	 * Time when the current location was last updated
	 */
	private double lastTime;
	
	public MobilityModel(GeoLocation initialLocation) {
		setInitialLocation(initialLocation);
		setCurrentLocation(initialLocation);
		setLastTime(0);
	}
	
	/**
	 * Returns the location at currTime based on the last location-time information
	 * @param lastTime time at which last location was recorded
	 * @param lastLocation last location recorded
	 * @param currTime current time at which location is required
	 * @return location at <b>currTime</b>
	 */
	protected abstract GeoLocation getUpdatedLocation(double lastTime, GeoLocation lastLocation, double currTime);
	
	/**
	 * Return location at given time
	 * @param time
	 * @return
	 */
	public GeoLocation getCurrentLocation(double time) {
		updatePosition(time);
		return getCurrentLocation();
	}
	
	/**
	 * Update the current position at the given time
	 * @param time
	 */
	private void updatePosition(double time) {
		setCurrentLocation(getUpdatedLocation(getLastTime(), getCurrentLocation(), time));
		setLastTime(time);
	}

	protected GeoLocation getInitialLocation() {
		return initialLocation;
	}

	protected void setInitialLocation(GeoLocation initialLocation) {
		this.initialLocation = initialLocation;
	}

	protected GeoLocation getCurrentLocation() {
		return currentLocation;
	}

	protected void setCurrentLocation(GeoLocation currentLocation) {
		this.currentLocation = currentLocation;
	}

	protected double getLastTime() {
		return lastTime;
	}

	protected void setLastTime(double lastTime) {
		this.lastTime = lastTime;
	}
}
