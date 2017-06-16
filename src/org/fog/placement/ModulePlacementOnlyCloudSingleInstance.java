/*
 * Title:        iFogSim Toolkit
 * Description:  iFogSim (Fog Simulation) Toolkit for Modeling and Simulation of Fog Computing
 *
 */
package org.fog.placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.ActuatorCharacteristics;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.SensorCharacteristics;

/**
 * Module Placement policy that places modules only on the cloud. Creates one single instance of application for all sensor-actuator pair.
 * Ideal for scenarios where all information needs to be processed by one entity, e.g. IoT in smart city applications
 * 
 * @author Harshit Gupta
 * @since iFogSim 2.0
 */
public class ModulePlacementOnlyCloudSingleInstance extends ModulePlacementPolicy {
	
	/**
	 * List of sensors considered for placement
	 */
	private List<Sensor> sensors;
	
	/**
	 * List of actuators considered for placement
	 */
	private List<Actuator> actuators;
	
	public ModulePlacementOnlyCloudSingleInstance(List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators, Application application){
		super();
		this.setFogDevices(fogDevices);
		this.setApplication(application);
		this.setSensors(sensors);
		this.setActuators(actuators);
		this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
		this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
	}
	
	@Override
	public List<ModulePlacement> computeModulePlacements(
			List<FogDeviceCharacteristics> fogDeviceCharacteristics,
			List<SensorCharacteristics> sensorCharacteristics,
			List<ActuatorCharacteristics> actuatorCharacteristics) {
			
		for (FogDeviceCharacteristics fc : fogDeviceCharacteristics) {
			getFogDeviceCharacteristics().put(fc.getId(), fc);
		}
		for (SensorCharacteristics sc : sensorCharacteristics) {
			getSensorCharacteristics().put(sc.getId(), sc);
		}
		for (ActuatorCharacteristics ac : actuatorCharacteristics) {
			getActuatorCharacteristics().put(ac.getId(), ac);
		}
		
		// Get the cloud datacenter by looking at isCloud() value for each fog device		
		FogDeviceCharacteristics cloud = null;
		for (Integer fc : getFogDeviceCharacteristics().keySet()) {
			if (getFogDeviceCharacteristics().get(fc).isCloudDatacenter())
				cloud = getFogDeviceCharacteristics().get(fc);
		}
		
		if (cloud == null) {
			// If there is no cloud datacenter, the placement fails
			return null;
		}
		
		List<ModulePlacement> placements = new ArrayList<ModulePlacement>();
		
		ModulePlacement placement = new ModulePlacement();
		
		// Adding all sensors to the placement instance, since there is only one instance globally
		for (int sensorId : getSensorCharacteristics().keySet()) {
			placement.addSensorId(getSensorCharacteristics().get(sensorId).getTupleType(), sensorId);	
		}
		
		// Adding all actuators to the placement instance, since there is only one instance globally
		for (int actuatorId : getActuatorCharacteristics().keySet()) {
			placement.addActuatorId(getActuatorCharacteristics().get(actuatorId).getActuatorType(), actuatorId);	
		}
		
		// Most important, mapping all application modules to the cloud datacenter
		for (AppModule module : getApplication().getModules()) {
			placement.addMapping(module.getName(), cloud.getId());
		}
		
		placements.add(placement);
		return placements;
	}
	
	public List<Actuator> getActuators() {
		return actuators;
	}

	public void setActuators(List<Actuator> actuators) {
		this.actuators = actuators;
	}

	public List<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}

}
