package org.fog.models.mobility;

import org.fog.utils.GeoLocation;

public class ConstantPositionMobilityModel extends MobilityModel {

	public ConstantPositionMobilityModel(GeoLocation initialLocation) {
		super(initialLocation);
	}

	@Override
	protected GeoLocation getUpdatedLocation(double lastTime,
			GeoLocation lastLocation, double currTime) {
		return getInitialLocation();
	}

}
