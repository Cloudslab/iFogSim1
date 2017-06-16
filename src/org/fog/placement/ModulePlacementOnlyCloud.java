/*
 * Title:        iFogSim Toolkit
 * Description:  iFogSim (Fog Simulation) Toolkit for Modeling and Simulation of Fog Computing
 *
 */
package org.fog.placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.ActuatorCharacteristics;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.SensorCharacteristics;

/**
 * Module Placement policy that places modules only on the cloud. Creates a separate instance of complete application for each sensor-actuator pair.
 * Ideal for scenarios where a personal application is involved, e.g. offloading processing from smartphone to Fog.
 * 
 * @author Harshit Gupta
 * @since iFogSim 2.0
 */
public class ModulePlacementOnlyCloud extends ModulePlacementPolicy {
	
	/**
	 * List of sensors considered for placement
	 */
	private List<Sensor> sensors;
	
	/**
	 * List of actuators considered for placement
	 */
	private List<Actuator> actuators;
	
	public ModulePlacementOnlyCloud(List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators, Application application){
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
		
		for (int sensorId : getSensorCharacteristics().keySet()) {
			// For every sensor, there is a new placement instance (containing 1 instance of each app module)
			ModulePlacement placement = new ModulePlacement();
			// Adding sensor to the module placement instance
			placement.addSensorId(getSensorCharacteristics().get(sensorId).getTupleType(), sensorId);
			// Getting the actuator associated to this sensor
			ActuatorCharacteristics actuator = getCorresponsingActuator(getSensorCharacteristics().get(sensorId));
			// Adding the corresponding actuator to placement module instance
			placement.addActuatorId(actuator.getActuatorType(), actuator.getId());
			// Most important, mapping all application modules to the cloud datacenter
			for (AppModule module : getApplication().getModules()) {
				placement.addMapping(module.getName(), cloud.getId());
			}
			// Adding this placement instance to the list of placement instances
			placements.add(placement);
		}
		
		return placements;
	}

	/**
	 * Get the actuator paired with given sensor. For example, the sensors and actuators in a smartphone are coupled with each other.
	 * In current implementation, association is checked by looking at names.
	 * @param sensorCharacteristics characteristics of sensor for whom corresponding actuator is required
	 * @return
	 */
	private ActuatorCharacteristics getCorresponsingActuator(
			SensorCharacteristics sensorCharacteristics) {
		// Look at the last part of name to check correspondence
		String suffix = CloudSim.getEntityName(sensorCharacteristics.getId()).substring(2);
		for (Entry<Integer, ActuatorCharacteristics> e : getActuatorCharacteristics().entrySet()) {
			if (CloudSim.getEntityName(e.getKey()).contains(suffix))
				return e.getValue();
		}
		return null;
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
