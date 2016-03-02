package org.fog.application;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.fog.entities.Tuple;
import org.fog.utils.FogUtils;
import org.fog.utils.GeoCoverage;

public class Application {
	
	private String appId;
	private GeoCoverage geoCoverage;

	private List<AppModule> modules;
	private List<AppEdge> edges;

	public AppModule getModuleByName(String name){
		for(AppModule module : modules){
			if(module.getName().equals(name))
				return module;
		}
		return null;
	}

	public List<Tuple> getResultantTuples(String moduleName, Tuple inputTuple){
		List<Tuple> tuples = new ArrayList<Tuple>();
		AppModule module = getModuleByName(moduleName);
		for(AppEdge edge : getEdges()){
			if(edge.getSource().equals(moduleName)){
				Pair<String, String> pair = new Pair<String, String>(inputTuple.getTupleType(), edge.getTupleType());
				double selectivity = module.getSelectivityMap().get(pair);
				if(Math.random() < selectivity){
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
					tuples.add(tuple);
				}
			}
		}
		return tuples;
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

	
	
	

}
