package org.fog.test.perfeval;

import org.fog.models.mobility.ConstantPositionMobilityModel;
import org.fog.models.mobility.MobilityModel;
import org.fog.utils.GeoLocation;

public class MobilityTest {
	public static void main(String args[]) {
		GeoLocation geoLocation = new GeoLocation(0, 0);
		MobilityModel constantMobility = new ConstantPositionMobilityModel(geoLocation);
		System.out.println(constantMobility.getCurrentLocation(100));
		System.out.println(constantMobility.getCurrentLocation(200));
		System.out.println(constantMobility.getCurrentLocation(300));
	}
}
