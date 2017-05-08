package org.fog.utils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;

public class Logger {
	
	public static final int ERROR = 1;
	public static final int DEBUG = 0;
	
	public static int LOG_LEVEL = Logger.DEBUG;
	private static DecimalFormat df = new DecimalFormat("#.00"); 

	public static boolean ENABLED = false;;
	
	private static Map<String, Boolean> tagEnabled = new HashMap<String, Boolean>();
	
	public static void setLogLevel(int level){
		Logger.LOG_LEVEL = level;
	}
	
	public static void enableTag(String tag) {
		getTagEnabled().put(tag, true);
	}
	
	public static void disableTag(String tag) {
		getTagEnabled().put(tag, false);
	}
	
	private static boolean shouldLog(String tag) {
		boolean result = false;
		if (ENABLED) {
			result = (getTagEnabled().containsKey(tag))?getTagEnabled().get(tag):true;
		} else {
			result = (getTagEnabled().containsKey(tag))?getTagEnabled().get(tag):false;
		}
		return result;
	}
	
	public static void debug(String tag, String name, String message){
		if (!shouldLog(tag)) return;
		
		if(Logger.LOG_LEVEL <= Logger.DEBUG)
			System.out.println(df.format(CloudSim.clock())+ " : " + tag + " : " + name + " : " + message);
	}
	public static void error(String tag, String name, String message){
		if (!shouldLog(tag)) return;
		
		if(Logger.LOG_LEVEL <= Logger.ERROR)
			System.out.println(df.format(CloudSim.clock())+" : "+name+" : "+message);
	}

	public static Map<String, Boolean> getTagEnabled() {
		return tagEnabled;
	}

	public static void setTagEnabled(Map<String, Boolean> tagEnabled) {
		Logger.tagEnabled = tagEnabled;
	}

	public static void debug(String tag, String message) {
		debug(tag, "", message);
	}
	
	public static void error(String tag, String message) {
		error(tag, "", message);
	}
	
}
