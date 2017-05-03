package org.fog.placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.ActuatorCharacteristics;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.SensorCharacteristics;
import org.fog.entities.Tuple;

public class ModulePlacementOnlyCloudNew extends ModulePlacementPolicy {
	
	private List<Sensor> sensors;
	private List<Actuator> actuators;
	private int cloudId;
	
	public ModulePlacementOnlyCloudNew(List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators, Application application){
		super();
		this.setFogDevices(fogDevices);
		this.setApplication(application);
		this.setSensors(sensors);
		this.setActuators(actuators);
		this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
		this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
		this.setModuleInstanceCountMap(new HashMap<Integer, Map<String, Integer>>());
		this.cloudId = CloudSim.getEntityId("cloud");
		//mapModules();
		//computeModuleInstanceCounts();
	}
	
	private void computeModuleInstanceCounts(){
		FogDevice cloud = getDeviceById(CloudSim.getEntityId("cloud"));
		getModuleInstanceCountMap().put(cloud.getId(), new HashMap<String, Integer>());
		
		for(Sensor sensor : getSensors()){
			String sensorType = sensor.getSensorName();
			if(!getModuleInstanceCountMap().get(cloud.getId()).containsKey(sensorType))
				getModuleInstanceCountMap().get(cloud.getId()).put(sensorType, 0);
			getModuleInstanceCountMap().get(cloud.getId()).put(sensorType, getModuleInstanceCountMap().get(cloud.getId()).get(sensorType)+1);
		}
		
		for(Actuator actuator : getActuators()){
			String actuatorType = actuator.getActuatorType();
			if(!getModuleInstanceCountMap().get(cloud.getId()).containsKey(actuatorType))
				getModuleInstanceCountMap().get(cloud.getId()).put(actuatorType, 0);
			getModuleInstanceCountMap().get(cloud.getId()).put(actuatorType, getModuleInstanceCountMap().get(cloud.getId()).get(actuatorType)+1);
		}
		
		while(!isModuleInstanceCalculationComplete()){
			for(AppModule module : getApplication().getModules()){
				int maxInstances = 0;
				for(AppEdge edge : getApplication().getEdges()){
					if(!getModuleInstanceCountMap().get(cloudId).containsKey(edge.getSource()))
						continue;
					if(edge.getDestination().equals(module.getName()) && edge.getDirection()==Tuple.UP){
						maxInstances = Math.max(maxInstances, getModuleInstanceCountMap().get(cloudId).get(edge.getSource()));
					}
				}
				getModuleInstanceCountMap().get(cloudId).put(module.getName(), maxInstances);
			}
		}
		System.out.println(getModuleInstanceCountMap());
	}

	private boolean isModuleInstanceCalculationComplete() {
		for(AppModule module : getApplication().getModules()){
			if(!getModuleInstanceCountMap().get(cloudId).containsKey(module.getName()))
				return false;
		}
		return true;
	}

	@Override
	protected void mapModules() {
		List<AppModule> modules = getApplication().getModules();
		for(AppModule module : modules){
			FogDevice cloud = getDeviceById(cloudId);
			createModuleInstanceOnDevice(module, cloud);
		}
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
			for (AppModule module : getApplication().getModules()) {
				placement.addMapping(module.getName(), cloud.getId());
				System.out.println("Cloud ID = "+cloud.getId());
			}
			placements.add(placement);
		}
		
		return placements;
	}
}
