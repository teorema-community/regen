package br.inf.teorema.regen.model;

import java.util.HashMap;
import java.util.Map;

public class MapDiff {

	private Map<String, Object> oldMap;
	private Map<String, Object> newMap;
	private boolean hasDiff = false;
	
	public MapDiff() {
		super();
		// TODO Auto-generated constructor stub
	}
	public MapDiff(Map<String, Object> oldMap, Map<String, Object> newMap) {
		super();
		this.oldMap = oldMap;
		this.newMap = newMap;
	}
	public MapDiff(Map<String, Object> oldMap, Map<String, Object> newMap, boolean hasDiff) {
		super();
		this.oldMap = oldMap;
		this.newMap = newMap;
		this.hasDiff = hasDiff;
	}
	
	public Map<String, Object> getOldMap() {
		if (oldMap == null) {
			oldMap = new HashMap<>();
		}
		
		return oldMap;
	}
	public void setOldMap(Map<String, Object> oldMap) {
		this.oldMap = oldMap;
	}
	public Map<String, Object> getNewMap() {
		if (newMap == null) {
			newMap = new HashMap<>();
		}
		
		return newMap;
	}
	public void setNewMap(Map<String, Object> newMap) {
		this.newMap = newMap;
	}
	public boolean isHasDiff() {
		return hasDiff;
	}
	public void setHasDiff(boolean hasDiff) {
		this.hasDiff = hasDiff;
	}
	
}
