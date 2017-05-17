/*
 * Title:        iFogSim Toolkit
 * Description:  iFogSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 *
 */
package org.fog.placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.fog.placement.ModulePlacementPolicy;

public class ModulePlacementOnlyCloud extends ModulePlacementPolicy {
	
	private List<Sensor> sensors;
	private List<Actuator> actuators;
	private int cloudId;
	
	public ModulePlacementOnlyCloud(List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators, Application application){
		super();
		this.setFogDevices(fogDevices);
		this.setApplication(application);
		this.setSensors(sensors);
		this.setActuators(actuators);
		this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
		this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
		this.setModuleInstanceCountMap(new HashMap<Integer, Map<String, Integer>>());
		this.cloudId = CloudSim.getEntityId("cloud");
	}
	
	/*@Override
	protected void mapModules() {
		List<AppModule> modules = getApplication().getModules();
		for(AppModule module : modules){
			FogDevice cloud = getDeviceById(cloudId);
			createModuleInstanceOnDevice(module, cloud);
		}
	}*/

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
		
		FogDeviceCharacteristics cloud = null;
		for (Integer fc : getFogDeviceCharacteristics().keySet()) {
			if (getFogDeviceCharacteristics().get(fc).isCloudDatacenter())
				cloud = getFogDeviceCharacteristics().get(fc);
		}
		
		if (cloud == null)
			return null;
		
		List<ModulePlacement> placements = new ArrayList<ModulePlacement>();
		
		for (int sensorId : getSensorCharacteristics().keySet()) {
			ModulePlacement placement = new ModulePlacement();
			placement.addSensorId(getSensorCharacteristics().get(sensorId).getTupleType(), sensorId);
			ActuatorCharacteristics actuator = getCorresponsingActuator(getSensorCharacteristics().get(sensorId));
			placement.addActuatorId(actuator.getActuatorType(), actuator.getId());
			for (AppModule module : getApplication().getModules()) {
				placement.addMapping(module.getName(), cloud.getId());
			}
			placements.add(placement);
		}
		
		return placements;
	}

	private ActuatorCharacteristics getCorresponsingActuator(
			SensorCharacteristics sensorCharacteristics) {
		String suffix = CloudSim.getEntityName(sensorCharacteristics.getId()).substring(2);
		for (Entry<Integer, ActuatorCharacteristics> e : getActuatorCharacteristics().entrySet()) {
			if (CloudSim.getEntityName(e.getKey()).contains(suffix))
				return e.getValue();
		}
		return null;
	}
}
