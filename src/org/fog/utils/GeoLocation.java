package org.fog.utils;

public class GeoLocation {

	private double latitude;
	private double longitude;
	
	public GeoLocation(double latitude, double longitude){
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public double getDistanceFrom(GeoLocation l) {
		double dist = Math.sqrt(Math.pow(l.getLatitude() - getLatitude(), 2) 
				+ Math.pow(l.getLongitude() - getLongitude(), 2));
		return dist;
	}
	
	@Override
	public String toString() {
		return "("+getLatitude()+", "+getLongitude()+")";
	}
	
}
