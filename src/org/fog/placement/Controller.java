package org.fog.placement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationAbstract;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.FogDevice;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;

public class Controller extends SimEntity{

	public static double RESOURCE_MANAGE_INTERVAL = 100;
	public static double LATENCY_WINDOW = 1000;
	
	public static boolean ONLY_CLOUD = false;
		
	private List<FogDevice> fogDevices;
	
	private Map<String, Application> applications;
	private Map<String, Integer> appLaunchDelays;
	private ModuleMapping moduleMapping;

	public Controller(String name, List<FogDevice> fogDevices) {
		super(name);
		this.applications = new HashMap<String, Application>();
		this.setAppLaunchDelays(new HashMap<String, Integer>());
		this.setModuleMapping(null);
		for(FogDevice fogDevice : fogDevices){
			fogDevice.setControllerId(getId());
		}
		setFogDevices(fogDevices);
	}
	
	public Controller(String name, List<FogDevice> fogDevices, ModuleMapping moduleMapping) {
		super(name);
		this.applications = new HashMap<String, Application>();
		this.setAppLaunchDelays(new HashMap<String, Integer>());
		this.setModuleMapping(moduleMapping);
		for(FogDevice fogDevice : fogDevices){
			fogDevice.setControllerId(getId());
		}
		setFogDevices(fogDevices);
	}

	@Override
	public void startEntity() {
		for(String appId : applications.keySet()){
			if(getAppLaunchDelays().get(appId)==0)
				processAppSubmit(applications.get(appId));
			else
				send(getId(), getAppLaunchDelays().get(appId), FogEvents.APP_SUBMIT, applications.get(appId));
		}

		send(getId(), RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
		
		send(getId(), 10000, FogEvents.STOP_SIMULATION);
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
		case FogEvents.APP_SUBMIT:
			processAppSubmit(ev);
			break;
		case FogEvents.TUPLE_FINISHED:
			processTupleFinished(ev);
			break;
		case FogEvents.CONTROLLER_RESOURCE_MANAGE:
			manageResources();
			break;
		case FogEvents.STOP_SIMULATION:
			CloudSim.stopSimulation();
			printTimeDetails();
			printPowerDetails();
			System.exit(0);
			break;
		}
	}
	
	private void printPowerDetails() {
		// TODO Auto-generated method stub
		for(FogDevice fogDevice : getFogDevices()){
			
		}
	}

	private String getStringForLoopId(int loopId){
		for(String appId : getApplications().keySet()){
			Application app = getApplications().get(appId);
			for(AppLoop loop : app.getLoops()){
				if(loop.getLoopId() == loopId)
					return loop.getModules().toString();
			}
		}
		return null;
	}
	private void printTimeDetails() {
		for(Integer loopId : TimeKeeper.getInstance().getLoopIdToTupleIds().keySet()){
			System.out.println(getStringForLoopId(loopId));
			for(int tupleId : TimeKeeper.getInstance().getLoopIdToTupleIds().get(loopId)){
				Double startTime = 	TimeKeeper.getInstance().getEmitTimes().get(tupleId);
				Double endTime = 	TimeKeeper.getInstance().getEndTimes().get(tupleId);
				if(startTime == null || endTime == null)
					break;
				System.out.println(endTime-startTime);
			}
			
		}
		System.out.println("=========================================");
	}

	protected void manageResources(){
		send(getId(), RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
	}
	
	private void processTupleFinished(SimEvent ev) {
	}
	
	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		
	}
	
	public void submitApplication(Application application, int delay){
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getApplications().put(application.getAppId(), application);
		getAppLaunchDelays().put(application.getAppId(), delay);
	}
	
	private void processAppSubmit(SimEvent ev){
		Application app = (Application) ev.getData();
		processAppSubmit(app);
	}
	
	private void processAppSubmit(Application application){
		System.out.println(CloudSim.clock()+" Submitted application "+ application.getAppId());
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getApplications().put(application.getAppId(), application);
		
		Map<String, List<Integer>> allocationMap = null;
		
		ModulePlacement modulePlacement = (getModuleMapping()==null)?
				(new ModulePlacementOnlyCloud(getFogDevices(), application))
				:(new ModulePlacementMapping(getFogDevices(), application, getModuleMapping()));
				
		allocationMap = modulePlacement.getModuleToDeviceMap();
		
		for(FogDevice fogDevice : fogDevices){
			sendNow(fogDevice.getId(), FogEvents.ACTIVE_APP_UPDATE, application);
		}
		
		Map<Integer, List<AppModule>> deviceToModuleMap = modulePlacement.getDeviceToModuleMap();
		for(Integer deviceId : deviceToModuleMap.keySet()){
			for(AppModule module : deviceToModuleMap.get(deviceId)){
				sendNow(deviceId, FogEvents.APP_SUBMIT, application);
				sendNow(deviceId, FogEvents.LAUNCH_MODULE, module);
			}
		}
	}

	public List<FogDevice> getFogDevices() {
		return fogDevices;
	}

	public void setFogDevices(List<FogDevice> fogDevices) {
		this.fogDevices = fogDevices;
	}

	public Map<String, Integer> getAppLaunchDelays() {
		return appLaunchDelays;
	}

	public void setAppLaunchDelays(Map<String, Integer> appLaunchDelays) {
		this.appLaunchDelays = appLaunchDelays;
	}

	public Map<String, Application> getApplications() {
		return applications;
	}

	public void setApplications(Map<String, Application> applications) {
		this.applications = applications;
	}
	public ModuleMapping getModuleMapping() {
		return moduleMapping;
	}
	public void setModuleMapping(ModuleMapping moduleMapping) {
		this.moduleMapping = moduleMapping;
	}
	
	
	/*public static void printResults() {
		Log.enable();
		

		int numberOfHosts = hosts.size();
		int numberOfVms = vms.size();

		double totalSimulationTime = lastClock;
		double energy = datacenter.getPower() / (3600 * 1000);
		int numberOfMigrations = datacenter.getMigrationCount();

		Map<String, Double> slaMetrics = getSlaMetrics(vms);

		double slaOverall = slaMetrics.get("overall");
		double slaAverage = slaMetrics.get("average");
		double slaDegradationDueToMigration = slaMetrics.get("underallocated_migration");
		// double slaTimePerVmWithMigration = slaMetrics.get("sla_time_per_vm_with_migration");
		// double slaTimePerVmWithoutMigration =
		// slaMetrics.get("sla_time_per_vm_without_migration");
		// double slaTimePerHost = getSlaTimePerHost(hosts);
		double slaTimePerActiveHost = getSlaTimePerActiveHost(hosts);

		double sla = slaTimePerActiveHost * slaDegradationDueToMigration;

		List<Double> timeBeforeHostShutdown = getTimesBeforeHostShutdown(hosts);

		int numberOfHostShutdowns = timeBeforeHostShutdown.size();

		double meanTimeBeforeHostShutdown = Double.NaN;
		double stDevTimeBeforeHostShutdown = Double.NaN;
		if (!timeBeforeHostShutdown.isEmpty()) {
			meanTimeBeforeHostShutdown = MathUtil.mean(timeBeforeHostShutdown);
			stDevTimeBeforeHostShutdown = MathUtil.stDev(timeBeforeHostShutdown);
		}

		List<Double> timeBeforeVmMigration = getTimesBeforeVmMigration(vms);
		double meanTimeBeforeVmMigration = Double.NaN;
		double stDevTimeBeforeVmMigration = Double.NaN;
		if (!timeBeforeVmMigration.isEmpty()) {
			meanTimeBeforeVmMigration = MathUtil.mean(timeBeforeVmMigration);
			stDevTimeBeforeVmMigration = MathUtil.stDev(timeBeforeVmMigration);
		}

		Log.setDisabled(false);
		Log.printLine();
		Log.printLine(String.format("Number of hosts: " + numberOfHosts));
		Log.printLine(String.format("Number of VMs: " + numberOfVms));
		Log.printLine(String.format("Total simulation time: %.2f sec", totalSimulationTime));
		Log.printLine(String.format("Energy consumption: %.2f kWh", energy));
		Log.printLine(String.format("Number of VM migrations: %d", numberOfMigrations));
		Log.printLine(String.format("SLA: %.5f%%", sla * 100));
		Log.printLine(String.format(
				"SLA perf degradation due to migration: %.2f%%",
				slaDegradationDueToMigration * 100));
		Log.printLine(String.format("SLA time per active host: %.2f%%", slaTimePerActiveHost * 100));
		Log.printLine(String.format("Overall SLA violation: %.2f%%", slaOverall * 100));
		Log.printLine(String.format("Average SLA violation: %.2f%%", slaAverage * 100));
		// Log.printLine(String.format("SLA time per VM with migration: %.2f%%",
		// slaTimePerVmWithMigration * 100));
		// Log.printLine(String.format("SLA time per VM without migration: %.2f%%",
		// slaTimePerVmWithoutMigration * 100));
		// Log.printLine(String.format("SLA time per host: %.2f%%", slaTimePerHost * 100));
		Log.printLine(String.format("Number of host shutdowns: %d", numberOfHostShutdowns));
		Log.printLine(String.format(
				"Mean time before a host shutdown: %.2f sec",
				meanTimeBeforeHostShutdown));
		Log.printLine(String.format(
				"StDev time before a host shutdown: %.2f sec",
				stDevTimeBeforeHostShutdown));
		Log.printLine(String.format(
				"Mean time before a VM migration: %.2f sec",
				meanTimeBeforeVmMigration));
		Log.printLine(String.format(
				"StDev time before a VM migration: %.2f sec",
				stDevTimeBeforeVmMigration));
			if (datacenter.getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract) {
			PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerVmAllocationPolicyMigrationAbstract) datacenter
					.getVmAllocationPolicy();
				double executionTimeVmSelectionMean = MathUtil.mean(vmAllocationPolicy
					.getExecutionTimeHistoryVmSelection());
			double executionTimeVmSelectionStDev = MathUtil.stDev(vmAllocationPolicy
					.getExecutionTimeHistoryVmSelection());
			double executionTimeHostSelectionMean = MathUtil.mean(vmAllocationPolicy
					.getExecutionTimeHistoryHostSelection());
			double executionTimeHostSelectionStDev = MathUtil.stDev(vmAllocationPolicy
					.getExecutionTimeHistoryHostSelection());
			double executionTimeVmReallocationMean = MathUtil.mean(vmAllocationPolicy
					.getExecutionTimeHistoryVmReallocation());
			double executionTimeVmReallocationStDev = MathUtil.stDev(vmAllocationPolicy
					.getExecutionTimeHistoryVmReallocation());
			double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy
					.getExecutionTimeHistoryTotal());
			double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy
					.getExecutionTimeHistoryTotal());
			Log.printLine(String.format(
					"Execution time - VM selection mean: %.5f sec",
					executionTimeVmSelectionMean));
			Log.printLine(String.format(
					"Execution time - VM selection stDev: %.5f sec",
					executionTimeVmSelectionStDev));
			Log.printLine(String.format(
					"Execution time - host selection mean: %.5f sec",
					executionTimeHostSelectionMean));
			Log.printLine(String.format(
					"Execution time - host selection stDev: %.5f sec",
					executionTimeHostSelectionStDev));
			Log.printLine(String.format(
					"Execution time - VM reallocation mean: %.5f sec",
					executionTimeVmReallocationMean));
			Log.printLine(String.format(
						"Execution time - VM reallocation stDev: %.5f sec",
						executionTimeVmReallocationStDev));
			Log.printLine(String.format("Execution time - total mean: %.5f sec", executionTimeTotalMean));
			Log.printLine(String
					.format("Execution time - total stDev: %.5f sec", executionTimeTotalStDev));
			}
			Log.printLine();

		Log.setDisabled(true);
	}
*/
	
}
