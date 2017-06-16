package org.fog.test.perfeval;

import org.fog.models.mobility.ConstantPositionMobilityModel;
import org.fog.models.mobility.ConstantVelocityMobilityModel;
import org.fog.models.mobility.MobilityModel;
import org.fog.models.mobility.Vector;
import org.fog.utils.GeoLocation;

public class MobilityTest {
	public static void main(String args[]) {
		GeoLocation geoLocation = new GeoLocation(10, 20);
		MobilityModel constantMobility = new ConstantPositionMobilityModel(geoLocation);
		System.out.println(constantMobility.getCurrentLocation(100));
		System.out.println(constantMobility.getCurrentLocation(200));
		System.out.println(constantMobility.getCurrentLocation(300));
		
		MobilityModel constantVelMobility = new ConstantVelocityMobilityModel(geoLocation, new Vector(1, 2));
		System.out.println(constantVelMobility.getCurrentLocation(10));
		System.out.println(constantVelMobility.getCurrentLocation(20));
		System.out.println(constantVelMobility.getCurrentLocation(30));
	}
}
