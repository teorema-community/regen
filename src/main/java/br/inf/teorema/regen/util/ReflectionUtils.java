package br.inf.teorema.regen.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import br.inf.teorema.regen.model.MapDiff;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ReflectionUtils {

	public static boolean isOrExtends(Class<?> clazz, Class<?> superClazz) {
        return superClazz.isAssignableFrom(clazz);
    }
    
    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        Class<?> superClass = type.getSuperclass();
        if (isEntityOrMappedSuperClass(superClass)) {        	
            getAllFields(fields, superClass);
        }

        return fields;
    }
    
    public static List<Field> getAllFields(Class<?> type) {    	
        return getAllFields(new ArrayList<Field>(), type);
    }
    
    public static Field getField(Class<?> type, String name) throws NoSuchFieldException {
		try {
			return type.getDeclaredField(name);
		} catch (NoSuchFieldException e) {
			Class<?> superClass = type.getSuperclass();
			
			if (isEntityOrMappedSuperClass(superClass)) { 
				return getField(superClass, name);
			} else {
				throw e;
			}
		}
    }
    
    public static Object getFieldValue(Object obj, String name) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
    	Field field = getField(obj.getClass(), name);
    	field.setAccessible(true);
    	return field.get(obj);
    }
    
    public static <T> T setFieldValue(T obj, String name, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, SecurityException, ParseException {
    	Field field = getField(obj.getClass(), name);
    	field.setAccessible(true);    	
    	field.set(obj, convertValueIfNeeded(field, value));
    	
    	return obj;
    }
    
    public static boolean isEntityOrMappedSuperClass(Class<?> clazz) {
    	return clazz != null && (clazz.isAnnotationPresent(Entity.class) || clazz.isAnnotationPresent(MappedSuperclass.class));
    }
    
    public static boolean isDate(Field field) {
    	return field.getType().equals(Date.class);
    }
    
    public static boolean isDate(Class<?> clazz, String fieldName) throws NoSuchFieldException {
    	return isDate(getField(clazz, fieldName));
    }
    
    public static Field getLastField(String name, Class<?> clazz) throws NoSuchFieldException, SecurityException {
		if (name.contains(".") ) {
			String firstFieldName = name.substring(0, name.indexOf("."));
			Field firstField = getField(clazz, firstFieldName);
			Class<?> firstFieldClass = getFieldEntityOrType(firstField);
			return getLastField(name.substring(name.indexOf(".") + 1), firstFieldClass);
		} else {
			return getField(clazz, name);
		}		
	}
    
    public static Class<?> getFieldEntityOrType(Field field) {
		if ((
			field.isAnnotationPresent(OneToMany.class)
			|| field.isAnnotationPresent(ManyToMany.class)
		) && isOrExtendsIterable(field.getType())) {
			ParameterizedType listType = (ParameterizedType) field.getGenericType();
			Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];
			return listClass;
		}
		
		return field.getType();
	}
    
    public static boolean isOrExtendsIterable(Class<?> clazz) {
		return Iterable.class.isAssignableFrom(clazz);
	}
    
    public static List<Field> getFields(String name, Class<?> clazz) throws NoSuchFieldException, SecurityException {
		List<Field> fields = new ArrayList<Field>();
		
		while (!name.isEmpty()) {
			String fieldName = name;
			int index = name.indexOf(".");
			if (index > - 1) {
				fieldName = name.substring(0, index);
			}
			
			Field field = getField(clazz, fieldName);
			fields.add(field);
			
			if (index > -1 && name.length() > index + 1) {
				clazz = getFieldEntityOrType(field);
				name = name.substring(index + 1);
			} else {
				name = "";
			}
		}
		
		return fields;
	}
    
    public static Field getPKField(Class<?> clazz) {
		List<Field> fields = getAllFields(clazz);
		for (Field f: fields) {
			if (ReflectionUtils.isId(f)) {
				return f;
			}
		}
		
		return null;
	}
    
    public static List<Field> getPKFields(Class<?> clazz) {
		return getPKFields(clazz, null);
	}
	
	public static List<Field> getPKFields(Class<?> clazz, Class<? extends Annotation> annotationToFilter) {
		List<Field> properties = new ArrayList<Field>();
		
		List<Field> fields = getAllFields(clazz);
		for (Field f: fields) {
			if (f.isAnnotationPresent(EmbeddedId.class)) {
				String pkFieldName = f.getName();
				Class<?> pkClass = f.getType();
				List<Field> pkFields = getAllFields(pkClass);
				
				for (Field pkField: pkFields) {
					if (annotationToFilter == null || f.isAnnotationPresent(annotationToFilter)) {
						properties.add(f);
					}
				}
			} else {
				if (annotationToFilter == null || f.isAnnotationPresent(annotationToFilter)) {
					properties.add(f);
				}
			}
		}
		
		return properties;
	}
	
	public static List<String> getPKFieldsNames(Class<?> clazz) {
		return getPKFieldsNames(clazz, null);
	}
	
	public static List<String> getPKFieldsNames(Class<?> clazz, Class<? extends Annotation> annotationToFilter) {
		List<String> properties = new ArrayList<String>();
		
		List<Field> fields = getAllFields(clazz);
		for (Field f: fields) {
			if (f.isAnnotationPresent(EmbeddedId.class)) {
				String pkFieldName = f.getName();
				Class<?> pkClass = f.getType();
				List<Field> pkFields = getAllFields(pkClass);
				
				for (Field pkField: pkFields) {
					if (annotationToFilter == null || f.isAnnotationPresent(annotationToFilter)) {
						properties.add(pkFieldName + "." + pkField.getName());
					}
				}
			} else {
				if (annotationToFilter == null || f.isAnnotationPresent(annotationToFilter)) {
					properties.add(f.getName());
				}
			}
		}
		
		return properties;
	}
	
	public static Class<?> getPKType(Class<?> clazz) {
		return getPKField(clazz).getType();
	}
	
	public static String getColumnName(Field field) {
		if (field.isAnnotationPresent(Column.class)) {
			return field.getAnnotation(Column.class).name();
		}
		
		if (field.isAnnotationPresent(JoinColumn.class)) {
			return field.getAnnotation(JoinColumn.class).name();
		}
		
		return null;
	}
	
	public static String getColumnName(String name, Class<?> clazz, boolean allFields) throws NoSuchFieldException, SecurityException {
		if (!allFields) {
			return getColumnName(getLastField(name, clazz));
		}
		
		List<Field> fields = getFields(name, clazz);
		StringBuilder columnName = new StringBuilder();
		int i = 0;
		for (Field f: fields) {
			if (f.isAnnotationPresent(Column.class)) {
				columnName.append(f.getAnnotation(Column.class).name());
				
				if (i < fields.size() - 1) {
					columnName.append(".");
				}
			}
			
			i++;
		}
		
		return columnName.toString();
	}

	public static List<Method> getGetters(Class<?> clazz){
	    Method[] methods = clazz.getMethods();
	    List<Method> getters = new ArrayList<Method>();
	    
	    for(Method method : methods){
	    	if(isGetter(method)) getters.add(method);
	    }
	    
	    return getters;
	}
	
	public static List<Method> getSetters(Class<?> clazz){
	    Method[] methods = clazz.getMethods();
	    List<Method> setters = new ArrayList<Method>();
	    
	    for(Method method : methods){
	    	if(isSetter(method)) setters.add(method); 
	    }
	    
	    return setters;
	}

	public static boolean isGetter(Method method){
		if(!method.getName().startsWith("get"))      return false;
		if(method.getParameterTypes().length != 0)   return false;  
		if(void.class.equals(method.getReturnType())) return false;
		return true;
	}

	public static boolean isSetter(Method method){
		if(!method.getName().startsWith("set")) return false;
		if(method.getParameterTypes().length != 1) return false;
		return true;
	}
	
	@SafeVarargs
	public static Map<String, Object> filterPropertiesMapByAnnotation(Map<String, Object> properties, Class<?> clazz, 
			Class<? extends Annotation>... annotationToFilter) throws NoSuchFieldException, SecurityException {
		List<String> list = new ArrayList<String>();
		list.addAll(properties.keySet());
		List<String> filteredKeys = filterPropertiesListByAnnotation(list, clazz, annotationToFilter);
		Map<String, Object> filteredMap = new HashMap<String, Object>();
		
		int i = 0;
		for (Object value: properties.values()) {
			if (i >= filteredKeys.size()) {
				break;
			}
			
			filteredMap.put(filteredKeys.get(i), value);
			i++;
		}
		
		return filteredMap;
	}
	
	@SafeVarargs
	public static List<String> filterPropertiesListByAnnotation(List<String> properties, Class<?> clazz, 
			Class<? extends Annotation>... annotationToFilter) throws NoSuchFieldException, SecurityException {
		List<String> filteredList = new ArrayList<String>();
		
		for(String p: properties) {
			Field field = ReflectionUtils.getLastField(p, clazz);	
			for (Class<? extends Annotation> a: annotationToFilter) {
				if (field.isAnnotationPresent(a)) {
					filteredList.add(p);
					break;
				} 
			}
		}
		
		return filteredList;
	}
	
	public static Field getFirstField(String name, Class<?> clazz) throws NoSuchFieldException, SecurityException {
		return getLastField(getFirstNReferences(name, 1), clazz);
	}
	
	public static String getTableName(Class<?> clazz) {
		return clazz.getAnnotation(Table.class).name();
	}
	
	public static boolean isEntity(Class<?> clazz) {
		return clazz.isAnnotationPresent(Entity.class);
	}
	
	public static boolean isEmbeddable(Class<?> clazz) {
		return clazz.isAnnotationPresent(Embeddable.class);
	}
	
	public static boolean isId(Field field) {
		return field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class);
	}
	
	public static Field getIdField(Class<?> clazz) { 
		List<Field> fields = getAllFields(clazz);
		for (Field f: fields) {
			if (f.isAnnotationPresent(Id.class) || f.isAnnotationPresent(EmbeddedId.class)) {
				return f;				
			}
		}
		
		return null;
	}
	
	public static Object validateFieldValue(Object obj) {
		if (obj != null) {
			if (ObjectUtils.isOrExtendsList(obj.getClass())) {
				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) obj;
				
				for (Object o: list) {
					o = validateFieldValue(o);
				}
				
				return list;
			}
			
			if (obj instanceof String) {
				return StringUtils.removerAcentos(obj.toString()).toUpperCase();	
			} 
		}
		
		return obj;
	}
	
	public static Multimap<String, Object> validateNestedFields(Map<String, Object> map, Class<?> clazz) throws NoSuchFieldException, SecurityException, 
			IllegalArgumentException, IllegalAccessException {
		return validateNestedFields(map, clazz, null);
	}
	
	/*
	 * Transforma referências JSON em referências pontuadas
	 * Exemplo:
	 * input:
	 * pk: {
	 * 		itemReduzido: 000002		
	 * }
	 * output: 
	 * pk.itemReduzido: 000002
	 */
	@SuppressWarnings("unchecked")
	public static Multimap<String, Object> validateNestedFields(Map<String, Object> map, Class<?> clazz, String backReference) throws NoSuchFieldException, SecurityException, 
			IllegalArgumentException, IllegalAccessException {
		Multimap<String, Object> validatedMap = ArrayListMultimap.create();
		
		if (map != null && !map.isEmpty()) {
			for (Entry<String, Object> pair : map.entrySet()) {
				String fieldName = pair.getKey().toString();
				Field lastField = ReflectionUtils.getLastField(fieldName, clazz);
				Class<?> fieldClass = ReflectionUtils.getFieldEntityOrType(lastField);
				
				if (ReflectionUtils.isEntity(fieldClass) || ReflectionUtils.isEmbeddable(fieldClass)) {
					Object value = pair.getValue();
					List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();
					
					if (ObjectUtils.isOrExtendsList(value.getClass())) {
						values = (List<Map<String, Object>>) value;
					} else {
						values.add((Map<String, Object>) pair.getValue());
					}
					
					String key = fieldName;
					
					if (backReference != null) {
						key = backReference + "." + fieldName;
					} 
					
					for (Map<String, Object> v: values) {
						validatedMap.putAll(validateNestedFields(v, fieldClass, key));
					}
				} else {
					String key = pair.getKey();
					
					if (backReference != null) {
						key = backReference + "." + pair.getKey();
					}
					
					validatedMap.put(key, pair.getValue());
				}
			}
		}
		
		return validatedMap;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> joinMultipleValues(Multimap<String, Object> values) {
		Map<String, Object> joinedMap = new HashMap<String, Object>();
		
		for (Entry<String, Object> c: values.entries()) {
			Object currentValue = joinedMap.get(c.getKey());
			
			if (currentValue == null) {
				joinedMap.put(c.getKey(), c.getValue());
			} else if (!currentValue.equals(c.getValue())) {
				List<Object> newValues = new ArrayList<Object>();				
				
				if (ObjectUtils.isOrExtendsList(currentValue.getClass())) {
					newValues = (List<Object>) currentValue;
				} else {
					newValues.add(currentValue);
				}
				
				newValues.add(c.getValue());
				joinedMap.put(c.getKey(), newValues);
			}
		}
		
		return joinedMap;
	}
	
	public static boolean containsReference(Set<String> set, String str) {
		List<String> list = new ArrayList<String>();
		list.addAll(set);
		return containsReference(list, str);
	}
	
	public static boolean containsReference(List<String> list, String str) {
		String[] splittedString = str.split("\\.");
		
		for (String item: list) {
			String[] splittedItem = item.split("\\.");
			
			for (String s: splittedString) {
				for (String si: splittedItem) {
					if (s.contains(si) || si.contains(s)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public static String getFirstNReferences(String name, int numberOfReferences) {
		if (org.apache.commons.lang3.StringUtils.countMatches(name, ".") <= numberOfReferences - 1) {
			return name;
		}
		
		StringBuilder sb = new StringBuilder();
		
		while (numberOfReferences > 0) {	
			if (!sb.toString().isEmpty()) {
				sb.append(".");
			}
			
			int firstDotIndex = name.indexOf(".");
			
			if (firstDotIndex > 0) {	
				String firstFieldName = name.substring(0, firstDotIndex);
				sb.append(firstFieldName);
				if (name.length() > firstDotIndex) {
					name = name.substring(firstDotIndex + 1);
				} else {
					break;
				}
			} else {
				sb.append(name);
				return sb.toString();
			}
			
			numberOfReferences--;
		}
		
		return sb.toString();
	}
	
	public static String getLastNReferences(String name, int numberOfReferences) {
		if (org.apache.commons.lang3.StringUtils.countMatches(name, ".") <= numberOfReferences - 1) {
			return name;
		}
		
		StringBuilder sb = new StringBuilder();
		
		while (numberOfReferences > 0) {
			if (!sb.toString().isEmpty()) { 
				sb.insert(0, ".");
			}
			
			int firstDotIndex = name.lastIndexOf(".");
			
			if (firstDotIndex > 0) {	
				String firstFieldName = name.substring(firstDotIndex + 1);
				sb.insert(0, firstFieldName);
				name = name.substring(0, firstDotIndex);
			} else {
				sb.insert(0, name);
				return sb.toString();
			}
			
			numberOfReferences--;
		}
		
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T patch(Map<String, Object> map, T entity, Class<?> clazz) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, 
			SecurityException, NoSuchMethodException, IllegalArgumentException, ParseException, InstantiationException {
		if (map != null) {
			if (!map.isEmpty() && entity == null) {
				entity = (T) clazz.newInstance();
			}
			
			for (Entry<String, Object> entry: map.entrySet()) {
				Field firstField = ReflectionUtils.getFirstField(entry.getKey(), clazz);
				Class<?> firstFieldClass = firstField.getType();
				
				if (ReflectionUtils.isEntity(firstFieldClass)) {
					Map<String, Object> nestedMap = (Map<String, Object>) entry.getValue();				
					Object oldValue = getFieldValue(entity, firstField.getName());
					Object newValue = patch(nestedMap, oldValue, firstFieldClass);
					entity = setFieldValue(entity, entry.getKey(), newValue);			
				} else if (ObjectUtils.isOrExtendsIterable(firstFieldClass)) {
					Class<?> nestedClass = ReflectionUtils.getFieldEntityOrType(firstField);
					
					List<Object> oldList = (List<Object>) getFieldValue(entity, firstField.getName());
					List<Map<String, Object>> newList = (List<Map<String, Object>>) entry.getValue();
					
					for (Map<String, Object> newMap: newList) {		
						Object newEntity = (Object) ObjectUtils.mapToPojo(newMap, nestedClass);
						Object newEntityPK = getPK(newEntity);
						
						if (newEntityPK == null) {
							oldList.add(newEntity);
						} else {
							boolean found = false;
							
							int i = 0;
							for (Object oldEntity: oldList) {							
								if (getPK(oldEntity).equals(newEntityPK)) {
									oldList.set(i, patch(newMap, oldEntity, nestedClass));
									found = true;
									break;
								} 
								
								i++;
							}
							
							if (!found) {
								oldList.add(newEntity);
							}
						}
					}
					
					entity = setFieldValue(entity, entry.getKey(), oldList);
				} else if (entry.getValue() != null && ObjectUtils.isOrExtendsMap(entry.getValue().getClass())) {
					entity = setFieldValue(entity, entry.getKey(), ObjectUtils.mapToPojo((Map<String, Object>) entry.getValue(), firstFieldClass));
				} else {
					entity = setFieldValue(entity, entry.getKey(), entry.getValue());
				}
			}
		} else {
			return null;
		}
		
		return entity;
	}
	
	public static Object getPK(Object entity) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		Class<?> clazz = entity.getClass();		
		List<Field> fields = getAllFields(clazz);
		
		for (Field f: fields) {
			if (f.isAnnotationPresent(Id.class) || f.isAnnotationPresent(EmbeddedId.class)) {
				return getFieldValue(entity, f.getName());
			}
		}
		
		throw new NullPointerException();
	}
	
	public static MapDiff getObjectDiff(Object oldObject, Object newObject) {
		MapDiff diff = new MapDiff();
		
		Map<String, Object> oldObjectMap = null;
		Map<String, Object> newObjectMap = null;
		
		if (oldObject != null) {
			oldObjectMap = ObjectUtils.objectToMap(oldObject);
		}
		
		if (newObject != null) {
			newObjectMap = ObjectUtils.objectToMap(newObject);
		}
		
		if (oldObject == null && newObject != null) {
			diff.setHasDiff(true);
			diff.setNewMap(newObjectMap);
		} else if (newObject == null && oldObject != null) {
			diff.setHasDiff(true);
			diff.setOldMap(oldObjectMap);
		} else if (oldObject != null && newObject != null) {
			diff = getMapDiff(oldObjectMap, newObjectMap);
			diff.setHasDiff(diff.isHasDiff());
		}
		
		return diff;
	}
	
	@SuppressWarnings("unchecked")
	public static MapDiff getMapDiff(Map<String, Object> inputOldMap, Map<String, Object> inputNewMap) {		
		Map<String, Object> oldMap = new HashMap<String, Object>();
		Map<String, Object> newMap = new HashMap<String, Object>();
		boolean atLeastOneDiff = false;
		
		for (Entry<String, Object> entry : inputOldMap.entrySet()) {
			Object oldValue = entry.getValue();
			Object newValue = inputNewMap.get(entry.getKey());
			
			Object newOldValue = null;
			Object newNewValue = null;
			boolean hasDiff = false;
			
			if (oldValue != null && newValue != null) {
				if (oldValue instanceof Collection<?> || newValue instanceof Collection<?>) {
					List<Map<String, Object>> oldList = (List<Map<String, Object>>) oldValue;
					List<Map<String, Object>> newList = (List<Map<String, Object>>) newValue;
					
					if (!oldList.equals(newList)) {
						hasDiff = true;
						newOldValue = oldList;
						newNewValue = newList;
					}
				} else if (oldValue instanceof Map<?, ?> || newValue instanceof Map<?, ?>) {
					MapDiff innerDiff = getMapDiff((Map<String, Object>) oldValue, (Map<String, Object>) newValue);
					
					hasDiff = innerDiff.isHasDiff();
					if (innerDiff.isHasDiff()) {
						newOldValue = innerDiff.getOldMap();
						newNewValue = innerDiff.getNewMap();
					}
				} else if (!oldValue.equals(newValue)) {
					hasDiff = true;
					newOldValue = oldValue;
					newNewValue = newValue;
				}
			} else if (
				(oldValue != null && newValue == null) 
				|| (oldValue == null && newValue != null)
			) {
				hasDiff = true;
				newOldValue = oldValue;
				newNewValue = newValue;
			}
			
			if (hasDiff) {
				atLeastOneDiff = hasDiff;
				oldMap.put(entry.getKey(), newOldValue);
				newMap.put(entry.getKey(), newNewValue);
			}
		}
		
		for(Entry<String, Object> entry : inputNewMap.entrySet()) {
			if (!oldMap.containsKey(entry.getKey())) {
				atLeastOneDiff = true;
				oldMap.put(entry.getKey(), null);
				newMap.put(entry.getKey(), entry.getValue());
			}
		}
		
		return new MapDiff(oldMap, newMap, atLeastOneDiff);
	}
	
	public static MapDiff getEntityDiff(Object oldEntity, Object newEntity) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {		
		Map<String, Object> outputOldMap = null;
		Map<String, Object> outputNewMap = null;
		boolean atLeastOneDiff = false;
		
		if (oldEntity != null && newEntity != null) {
			return innerGetEntityDiff(oldEntity, newEntity);		
		} else if (oldEntity == null) {
			atLeastOneDiff = true;
			outputNewMap = ObjectUtils.objectToMap(newEntity);
		} else if (newEntity == null) {
			atLeastOneDiff = true;
			outputOldMap = ObjectUtils.objectToMap(oldEntity);
		}
		
		return new MapDiff(outputOldMap, outputNewMap, atLeastOneDiff);
	}
	
	@SuppressWarnings("unchecked")
	private static MapDiff innerGetEntityDiff(Object oldValue, Object newValue) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
		Map<String, Object> outputOldMap = new HashMap<String, Object>();
        Map<String, Object> outputNewMap = new HashMap<String, Object>();
        boolean atLeastOneDiff = false;

        Set<String> oldValueKeys = ObjectUtils.objectToMap(oldValue).keySet();
        Set<String> newValueKeys = ObjectUtils.objectToMap(newValue).keySet();
        List<Field> fields = ReflectionUtils.getAllFields(oldValue.getClass()).stream()
                .filter(f -> (oldValueKeys.contains(f.getName()) || newValueKeys.contains(f.getName())) && !f.isAnnotationPresent(Transient.class))
                .collect(Collectors.toList());

        for (Field field : fields) {
            field.setAccessible(true);
            Object oldV = field.get(oldValue);
            Object newV = field.get(newValue);

            if (oldV == null && newV != null) {
                atLeastOneDiff = true;
                outputNewMap.put(field.getName(), newV);
            } else if (oldV != null && newV == null) {
                atLeastOneDiff = true;
                outputOldMap.put(field.getName(), oldV);
            } else if (oldV != null && newV != null) {
            	oldV = getTimeIfDate(oldV);
                newV = getTimeIfDate(newV);
                
                if (ReflectionUtils.isOrExtendsIterable(oldV.getClass()) || ReflectionUtils.isOrExtendsIterable(newV.getClass())) {
                    List<Object> intputOldList = (List<Object>) oldV;
                    List<Object> intputNewList = (List<Object>) newV;

                    if (intputOldList.isEmpty() && intputNewList.isEmpty()) {
                        continue;
                    }

                    List<Map<String, Object>> outputOldList = new ArrayList<Map<String, Object>>();
                    List<Map<String, Object>> outputNewList = new ArrayList<Map<String, Object>>();

                    String idFieldName = ReflectionUtils.getIdField(ReflectionUtils.getFieldEntityOrType(field)).getName();
                    List<Object> idsVerifies = new ArrayList<Object>();
                    boolean innerAtLeastOneDiff = false;

                    for (Object oldEntity : intputOldList) {
                        Object id = getFieldValue(oldEntity, idFieldName);

                        if (id != null) {
                            boolean found = false;

                            for (Object newEntity : intputNewList) {
                                Object newEntityId = getFieldValue(newEntity, idFieldName);

                                if (newEntityId != null && newEntityId.equals(id)) {
                                    found = true;
                                    MapDiff innerDiff = innerGetEntityDiff(oldEntity, newEntity);

                                    if (innerDiff.isHasDiff()) {
                                        innerAtLeastOneDiff = true;

                                        innerDiff.getOldMap().put(idFieldName, id);
                                        innerDiff.getNewMap().put(idFieldName, newEntityId);

                                        outputOldList.add(innerDiff.getOldMap());
                                        outputNewList.add(innerDiff.getNewMap());
                                    }

                                    break;
                                }
                            }

                            if (!found) {
                                innerAtLeastOneDiff = true;
                                outputOldList.add(ObjectUtils.objectToMap(oldEntity));
                            }

                            idsVerifies.add(id);
                        }
                    }

                    for (Object newEntity : intputNewList) {
                        Object newEntityId = getFieldValue(newEntity, idFieldName);

                        if (!idsVerifies.contains(newEntityId)) {
                            innerAtLeastOneDiff = true;
                            outputNewList.add(ObjectUtils.objectToMap(newEntity));
                            idsVerifies.add(newEntityId);
                        }
                    }

                    if (innerAtLeastOneDiff) {
                        atLeastOneDiff = true;
                        outputOldMap.put(field.getName(), outputOldList);
                        outputNewMap.put(field.getName(), outputNewList);
                    }
                } else if (ReflectionUtils.isEntity(oldV.getClass()) || ReflectionUtils.isEntity(newV.getClass())) {
                    MapDiff innerDiff = innerGetEntityDiff(oldV, newV);

                    if (innerDiff.isHasDiff()) {
                        atLeastOneDiff = true;
                        outputOldMap.put(field.getName(), innerDiff.getOldMap());
                        outputNewMap.put(field.getName(), innerDiff.getNewMap());
                    }
                } else if (!oldV.equals(newV)) {
                    atLeastOneDiff = true;
                    outputOldMap.put(field.getName(), oldV);
                    outputNewMap.put(field.getName(), newV);
                }
            }
        }

        return new MapDiff(outputOldMap, outputNewMap, atLeastOneDiff);
	}
	
	public static Object getTimeIfDate(Object obj) {
        if (obj != null) {
            if (obj.getClass().equals(Date.class)) {
                return ((Date) obj).getTime();
            } else if (obj.getClass().equals(java.sql.Date.class)) {
                return ((java.sql.Date) obj).getTime();
            } else if (obj.getClass().equals(Timestamp.class)) {
                return ((Timestamp) obj).getTime();
            }
        }

        return obj;
    }
	
	public static Object convertValueIfNeeded(Field field, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ParseException {		
		if (value != null && !value.toString().isEmpty() && !value.getClass().equals(field.getType())) {
			if (field.getType().equals(UUID.class)) {
				value = UUID.fromString(value.toString());
			} else if (isOrExtends(field.getType(), Number.class)) {
	    		value = ObjectUtils.parseNumber(field.getType(), value);
			} else if (value.getClass().equals(String.class)) {
				if (field.getType().equals(Date.class)) {
					value = DateUtils.parseDate(value.toString());
				} else if (field.getType().equals(java.sql.Date.class)) {
					value = new java.sql.Date(DateUtils.parseDate(value.toString()).getTime());
				} else if (field.getType().equals(Time.class)) {
					value = new Time(DateUtils.parseDate(value.toString()).getTime());
				}
			} else if (isOrExtends(value.getClass(), Number.class)) {
				if (field.getType().equals(Date.class)) {
					value = new Date(Long.parseLong(value.toString()));
				} else if (field.getType().equals(java.sql.Date.class)) {
					value = new java.sql.Date(Long.parseLong(value.toString()));
				} else if (field.getType().equals(Time.class)) {
					value = new Time(Long.parseLong(value.toString()));
				}
			}
    	}
		
		return convertValueToEnumIfNeeded(field.getType(), value);
	}
	
	public static Object convertValueToEnumIfNeeded(Class<?> fieldType, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (fieldType.isEnum() && value != null && !(value instanceof Enum<?>)) {
			value = fieldType.getDeclaredMethod("valueOf", String.class).invoke(null, value.toString());
		}
		
		return value;
	}
	
}
