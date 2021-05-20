package br.inf.teorema.regen.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.util.*;

public class ObjectUtils {

	public static boolean isNullOrEmpty(Object obj) {
		if (isNull(obj) || isEmpty(obj)) {
			return true;
		} 
		
		return false;
	}
	
	public static boolean notNullOrEmpty(Object obj) {
		return !isNullOrEmpty(obj);
	}
	
	public static boolean isNull(Object obj) {
		return obj == null;
	}
	
	public static boolean notNull(Object obj) {
		return !isNull(obj);
	}
	
	public static boolean isEmpty(Object obj) {
		if (obj instanceof String) {
			String str = (String) obj;
			if (str.isEmpty()) {
				return true;
			}
		} else if (obj instanceof List<?>) {
			List<?> list = (List<?>) obj;
			if (list.isEmpty()) {
				return true;
			}
		} 
		
		return false;
	}
	
	public static boolean notEmpty(Object obj) {
		return !isEmpty(obj);
	}
	
	public static boolean isNullOrEmpty(Object... args) {
		List<Object> argsList = Arrays.asList(args);
		for (Object obj: argsList) {
			if (isNullOrEmpty(obj)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean notNullOrEmpty(Object... args) {
		return !isNullOrEmpty(args);
	}
	
	public static boolean isArray(Object obj) {
	    return obj!=null && obj.getClass().isArray();
	} 
	
	public static boolean isArrayOrList(Object obj) {
		return obj != null && (isArray(obj) || obj instanceof List<?> || obj instanceof ArrayList<?>); 
	}
	
	public static Map<String, Object> objectToMap(Object obj) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = new ObjectMapper().convertValue(obj, Map.class); 
		return map;
	}
	
	public static Map<String, Object> objectToMapIgnoreNull(Object obj) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = new ObjectMapper().setSerializationInclusion(Include.NON_NULL).convertValue(obj, Map.class); 
		return map;
	}
	
	public static Map<String, Object> objectToMapIgnoreNullOrEmpty(Object obj) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = new ObjectMapper().setSerializationInclusion(Include.NON_EMPTY).convertValue(obj, Map.class); 
		return map;
	}
	
	public static Map<String, Object> objectToMap(Object value, String key, boolean allowArrays, Class<?> clazz) throws NoSuchFieldException, SecurityException {
		if (key != null) {	
			if (!allowArrays && isArray(value)) {
				Object[] array = (Object[]) value;
				value = array[0];
			}
			
			Map<String, Object> lastMap = new HashMap<String, Object>();
			Map<String, Object> newMap;
			if (!key.isEmpty()) {
				List<Field> fields = ReflectionUtils.getFields(key, clazz);
				
				for (int i = fields.size() - 1; i > -1; i--) {
					newMap = new HashMap<String, Object>();
					Object v;
					
					if (i == fields.size() - 1) {
						v = value;					
					} else {
						v = lastMap;
					}
					
					newMap.put(fields.get(i).getName(), v);
					lastMap = newMap;
				}
			} else {
				lastMap.put(key, value);
			}
			
			return lastMap;
		} else {
			return objectToMap(value);
		}		
	}
	
	public static Map<String, Object> objectToMap(Object[] value, List<String> keys) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		int i = 0;
		for (String p: keys) {	
			map.put(p, value[i]);
			i++;
		}
		
		return map;
	}
	
	public static List<Map<String, Object>> objectListToMapList(List<Object> values, String key, Class<?> clazz) throws NoSuchFieldException, SecurityException {	
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		boolean allowArrays = true;
		
		if (key != null && !key.isEmpty()) {
			Field field = ReflectionUtils.getLastField(key, clazz);
			allowArrays = field.getType().isArray();
		}
		
		for(Object value: values) {
			list.add(objectToMap(value, key, allowArrays, clazz));
		}
		
		return list;
	}
	
	public static List<Map<String, Object>> objectListToMapList(List<Object> values, Class<?> clazz) throws NoSuchFieldException, SecurityException {	
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		for(Object value: values) {
			list.add(objectToMap(value, null, true, clazz));
		}
		
		return list;
	}
	
	public static List<Object[]> objectListToObjectArrayList(List<Object> srcValues) {
		List<Object[]> arrayValues = new ArrayList<Object[]>();
		
		for(Object srcObj: srcValues) {
			arrayValues.add((Object[]) srcObj);
		}
		
		return arrayValues;
	}
	
	/*
	 * Transforma um array de valores e um array de chaves num JSON
	 */
	public static List<Map<String, Object>> objectArrayListToMapList(List<Object[]> values, List<String> keys, Class<?> clazz) throws NoSuchFieldException, SecurityException {		
		int levels = 0;
		List<List<Field>> allFieldsList = new ArrayList<List<Field>>();
		
		for (String k: keys) {
			List<Field> fields = ReflectionUtils.getFields(k, clazz);
			allFieldsList.add(fields);
			
			if (fields.size() > levels) {
				levels = fields.size();
			}
		}
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		for(Object[] rawValue: values) {
			Map<String, Object> map = new HashMap<String, Object>();
						
			List<Map<String, Object>> lastMaps = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> newMaps = new ArrayList<Map<String, Object>>();
			Boolean[] tookPeek = new Boolean[keys.size()];
			
			for (int i = levels - 1; i > -1; i--) {									
				for (int j = 0; j < allFieldsList.size(); j++) {
					newMaps.add(j, new HashMap<String, Object>());
					List<Field> fields = allFieldsList.get(j);
					
					if (i < fields.size()) {
						Field field = fields.get(i);
						String fieldName = field.getName();
						Object v;
						
						if (tookPeek[j] == null || tookPeek[j] == false) {
							v = rawValue[j];					
							tookPeek[j] = true;
						} else {
							v = lastMaps.get(j);
							
							if (j < lastMaps.size()) {
								v = joinRepeatedValues(lastMaps, fieldName, v);
							} 
						}
							
						newMaps.get(j).put(fieldName, v);
						
						if (i == 0) {
							map.put(fieldName, v);
						}
					} else {
						tookPeek[j] = false;
					}
					
					if (j <= lastMaps.size() - 1) {
						lastMaps.remove(j);
					}
					
					lastMaps.add(j, newMaps.get(j));
				}
				
				newMaps.clear();
			}
			
			list.add(map);
		}
		
		return list;
	}
	
	/*
	 * Transforma uma lista de entidades e um array de chaves num JSON
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<Map<String, Object>> fullModelToMapList(List<T> entityResults, List<String> keys, Class<?> clazz) throws NoSuchFieldException, 
			SecurityException {		
		List<Object[]> filteredValues = new ArrayList<Object[]>();
		
		for (Object obj: entityResults) {
			Map<String, Object> map = objectToMap(obj);
			Object[] filteredValue = new Object[keys.size()];
			
			int i = 0;
			for (String p: keys) {
				String[] references = p.split("\\.");
				Map<String, Object> lastMap = map;
				
				int j = 0;
				for (String k: references) {
					Object value = null;
					if (lastMap != null) {
						value = lastMap.get(k);
					}
					
					if (j < references.length - 1 && lastMap != null) {
						lastMap = (Map<String, Object>) lastMap.get(k);
					} else {
						filteredValue[i] = value;
					}
					
					j++;
				}
				
				i++;
			}
			
			filteredValues.add(filteredValue);
		}
		
		return objectArrayListToMapList(filteredValues, keys, clazz);
	}
	
	public static List<String> extractListFromMap(Map<String, Object> map, List<String> list, String key) {
		if (map != null && map.containsKey(key) && ObjectUtils.isArrayOrList(map.get(key))) {
			@SuppressWarnings("unchecked")
			List<String> selectPropertiesFromMap = (List<String>) map.get(key);
			
			if (list != null) {
				list.addAll(selectPropertiesFromMap);
			} else {
				list = selectPropertiesFromMap;
			}
			
			map.remove(key);
		}
		
		return list;
	}
	
	public static boolean anyItemOfListContains(Set<String> set, String str) {
		return anyItemOfListContains(stringSetToStringList(set), str);
	}
	
	public static boolean anyItemOfListContains(List<String> list, String str) {
		for (String s: list) {
			if (s.contains(str) || str.contains(s)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isOrExtendsIterable(Class<?> clazz) {
		return Iterable.class.isAssignableFrom(clazz);
	}
	
	public static boolean isOrExtendsCollection(Class<?> clazz) {
		return Collection.class.isAssignableFrom(clazz);
	}
	
	public static boolean isOrExtendsList(Class<?> clazz) {
		return List.class.isAssignableFrom(clazz);
	}
	
	public static boolean isOrExtends(Class<?> childClass, Class<?> parentClass) {
		return parentClass.isAssignableFrom(childClass);
	}
	
	public static boolean isOrExtendsMap(Class<?> clazz) {
		return Map.class.isAssignableFrom(clazz);
	}
	
	public static List<String> stringSetToStringList(Set<String> set) {
		List<String> list = new ArrayList<String>();
		list.addAll(set);
		return list;
	}
	
	public static String listToSeparatedComaString(List<Object> list) {
		StringBuilder sb = new StringBuilder();
		
		int i = 0;
		for (Object o: list) {
			sb.append(o.toString());
			
			if (i < list.size() - 1) {
				sb.append(", ");
			}
			
			i++;
		}
		
		return sb.toString();
	}
	
	public static Object joinRepeatedValues(Map<String, Object> map, String key, Object v) {
		if (map != null && !map.isEmpty()) {
			Object previousValue = map.get(key);
			
			if (previousValue != null) {
				@SuppressWarnings("unchecked")
				Map<String, Object> previousMap = (Map<String, Object>) previousValue; 
				@SuppressWarnings("unchecked")
				Map<String, Object> newValue = (Map<String, Object>) v;
				previousMap.putAll(newValue);
				v = previousMap;
			} 
		}
		
		return v;
	}
	
	public static Object joinRepeatedValues(List<Map<String, Object>> mapList, String key, Object v) {
		if (mapList != null && !mapList.isEmpty()) {
			for (Map<String, Object> m: mapList) {
				v = joinRepeatedValues(m, key, v);
			}
		}
		
		return v;
	}
	
	public static List<Object> objectToList(Object value) {
		return Arrays.asList(new Object[] { value });
	}
	
	public static List<String> stringToList(String value) {
		return Arrays.asList(new String[] { value });
	}
	
	public static Object mapToPojo(Map<String, Object> map, Class<?> clazz) {
		return new ObjectMapper().convertValue(map, clazz);
	}
	
	public static Map<String, Object> listsToMap(List<String> keyList, List<Object> valueList) { 
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (keyList != null) {
			if (keyList.size() != valueList.size()) {
				throw new IllegalArgumentException(
					"Both list have to be of the same size"
				);
			}
			
			for (int i = 0; i < keyList.size(); i++) {
				if (keyList.get(i) == null) {
					throw new NullPointerException();
				}
				
				map.put(keyList.get(i), valueList.get(i)); 
		    }
		}
		
		return map;
	}

	public static Long getLongOrNull(Object value) {
		try {
			return Long.parseLong(String.valueOf(value));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Object parseNumber(Class<?> clazz, Object value) {
		if (value != null) {
			if (clazz.equals(Integer.class)) {
    			return Integer.parseInt(value.toString());
    		} else if (clazz.equals(Double.class)) {
    			return Double.parseDouble(value.toString());
    		} else if (clazz.equals(Short.class)) {
    			return Short.parseShort(value.toString());
    		} else if (clazz.equals(Float.class)) {
    			return Float.parseFloat(value.toString());
    		} else if (clazz.equals(Long.class)) {
    			return Long.parseLong(value.toString());
    		} else if (clazz.equals(Byte.class)) {
    			return Byte.parseByte(value.toString());
    		}
		}
		
		return null;
	}
	
}
