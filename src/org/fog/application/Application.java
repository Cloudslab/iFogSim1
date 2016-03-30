package org.fog.application;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.fog.entities.Tuple;
import org.fog.utils.FogUtils;
import org.fog.utils.GeoCoverage;

public class Application {
	
	private String appId;
	private int userId;
	private GeoCoverage geoCoverage;

	private List<AppModule> modules;
	private List<AppEdge> edges;
	
	private List<AppLoop> loops;

	public List<AppEdge> getPeriodicEdges(String srcModule){
		List<AppEdge> result = new ArrayList<AppEdge>();
		for(AppEdge edge : edges){
			if(edge.isPeriodic() && edge.getSource().equals(srcModule))
				result.add(edge);
		}
		return result;
	}
	
	public Application(String appId, List<AppModule> modules,
			List<AppEdge> edges, List<AppLoop> loops, GeoCoverage geoCoverage) {
		setAppId(appId);
		setModules(modules);
		setEdges(edges);
		setGeoCoverage(geoCoverage);
		setLoops(loops);
	}

	public AppModule getModuleByName(String name){
		for(AppModule module : modules){
			if(module.getName().equals(name))
				return module;
		}
		return null;
	}

	public List<Tuple> getResultantTuples(String moduleName, Tuple inputTuple, int sourceDeviceId){
		List<Tuple> tuples = new ArrayList<Tuple>();
		AppModule module = getModuleByName(moduleName);
		for(AppEdge edge : getEdges()){
			if(edge.getSource().equals(moduleName)){
				Pair<String, String> pair = new Pair<String, String>(inputTuple.getTupleType(), edge.getTupleType());
				
				if(module.getSelectivityMap().get(pair)==null)
					continue;
				double selectivity = module.getSelectivityMap().get(pair);
				if(Math.random() < selectivity){
					
					//TODO check if the edge is ACTUATOR, then create multiple tuples
					if(edge.getEdgeType() == AppEdge.ACTUATOR){
						for(Integer actuatorId : module.getActuatorSubscriptions().get(edge.getTupleType())){
							Tuple tuple = new Tuple(appId, FogUtils.generateTupleId(), edge.getDirection(),  
									(long) (edge.getTupleCpuLength()),
									inputTuple.getNumberOfPes(),
									(long) (edge.getTupleNwLength()),
									inputTuple.getCloudletOutputSize(),
									inputTuple.getUtilizationModelCpu(),
									inputTuple.getUtilizationModelRam(),
									inputTuple.getUtilizationModelBw()
									);
							tuple.setActualTupleId(inputTuple.getActualTupleId());
							tuple.setUserId(inputTuple.getUserId());
							tuple.setAppId(inputTuple.getAppId());
							tuple.setDestModuleName(edge.getDestination());
							tuple.setSrcModuleName(edge.getSource());
							tuple.setDirection(Tuple.ACTUATOR);
							tuple.setTupleType(edge.getTupleType());
							tuple.setSourceDeviceId(sourceDeviceId);
							tuple.setActuatorId(actuatorId);
							
							tuples.add(tuple);
						}
					}else{
						Tuple tuple = new Tuple(appId, FogUtils.generateTupleId(), edge.getDirection(),  
								(long) (edge.getTupleCpuLength()),
								inputTuple.getNumberOfPes(),
								(long) (edge.getTupleNwLength()),
								inputTuple.getCloudletOutputSize(),
								inputTuple.getUtilizationModelCpu(),
								inputTuple.getUtilizationModelRam(),
								inputTuple.getUtilizationModelBw()
								);
						tuple.setActualTupleId(inputTuple.getActualTupleId());
						tuple.setUserId(inputTuple.getUserId());
						tuple.setAppId(inputTuple.getAppId());
						tuple.setDestModuleName(edge.getDestination());
						tuple.setSrcModuleName(edge.getSource());
						tuple.setDirection(edge.getDirection());
						tuple.setTupleType(edge.getTupleType());
						tuples.add(tuple);
					}
				}
			}
		}
		return tuples;
	}
	
	public Tuple createTuple(AppEdge edge, int sourceDeviceId){
		AppModule module = getModuleByName(edge.getSource());
		if(edge.getEdgeType() == AppEdge.ACTUATOR){
			for(Integer actuatorId : module.getActuatorSubscriptions().get(edge.getTupleType())){
				Tuple tuple = new Tuple(appId, FogUtils.generateTupleId(), edge.getDirection(),  
						(long) (edge.getTupleCpuLength()),
						1,
						(long) (edge.getTupleNwLength()),
						100,
						new UtilizationModelFull(), 
						new UtilizationModelFull(), 
						new UtilizationModelFull()
						);
				tuple.setUserId(getUserId());
				tuple.setAppId(getAppId());
				tuple.setDestModuleName(edge.getDestination());
				tuple.setSrcModuleName(edge.getSource());
				tuple.setDirection(Tuple.ACTUATOR);
				tuple.setTupleType(edge.getTupleType());
				tuple.setSourceDeviceId(sourceDeviceId);
				tuple.setActuatorId(actuatorId);
				
				return tuple;
			}
		}else{
			Tuple tuple = new Tuple(appId, FogUtils.generateTupleId(), edge.getDirection(),  
					(long) (edge.getTupleCpuLength()),
					1,
					(long) (edge.getTupleNwLength()),
					100,
					new UtilizationModelFull(), 
					new UtilizationModelFull(), 
					new UtilizationModelFull()
					);
			//tuple.setActualTupleId(inputTuple.getActualTupleId());
			tuple.setUserId(getUserId());
			tuple.setAppId(getAppId());
			tuple.setDestModuleName(edge.getDestination());
			tuple.setSrcModuleName(edge.getSource());
			tuple.setDirection(edge.getDirection());
			tuple.setTupleType(edge.getTupleType());
			return tuple;
		}
		return null;
	}
	
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public List<AppModule> getModules() {
		return modules;
	}
	public void setModules(List<AppModule> modules) {
		this.modules = modules;
	}
	public List<AppEdge> getEdges() {
		return edges;
	}
	public void setEdges(List<AppEdge> edges) {
		this.edges = edges;
	}
	public GeoCoverage getGeoCoverage() {
		return geoCoverage;
	}
	public void setGeoCoverage(GeoCoverage geoCoverage) {
		this.geoCoverage = geoCoverage;
	}

	public List<AppLoop> getLoops() {
		return loops;
	}

	public void setLoops(List<AppLoop> loops) {
		this.loops = loops;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
}
