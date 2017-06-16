package org.fog.models.mobility;

import org.fog.utils.GeoLocation;

public class ConstantVelocityMobilityModel extends MobilityModel {

	private Vector velocity;
	
	public ConstantVelocityMobilityModel(GeoLocation initialLocation, Vector velocity) {
		super(initialLocation);
		setVelocity(velocity);
	}

	private Vector getDisplacement(double time) {
		Vector disp = new Vector(getVelocity().getX()*time, getVelocity().getY()*time);
		return disp;
	}
	
	@Override
	protected GeoLocation getUpdatedLocation(double lastTime,
			GeoLocation lastLocation, double currTime) {
		Vector disp = getDisplacement(currTime-lastTime);
		return new GeoLocation(lastLocation.getLatitude()+disp.getX(), 
				lastLocation.getLongitude()+disp.getY());
	}

	public Vector getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector velocity) {
		this.velocity = velocity;
	}

}
