package br.inf.teorema.regen.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

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
		if (field.isAnnotationPresent(OneToMany.class) && isOrExtendsIterable(field.getType())) {
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
		Field[] fields = clazz.getDeclaredFields();
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
		
		Field[] fields = clazz.getDeclaredFields();
		for (Field f: fields) {
			if (f.isAnnotationPresent(EmbeddedId.class)) {
				String pkFieldName = f.getName();
				Class<?> pkClass = f.getType();
				Field[] pkFields = pkClass.getDeclaredFields();
				
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
		
		Field[] fields = clazz.getDeclaredFields();
		for (Field f: fields) {
			if (f.isAnnotationPresent(EmbeddedId.class)) {
				String pkFieldName = f.getName();
				Class<?> pkClass = f.getType();
				Field[] pkFields = pkClass.getDeclaredFields();
				
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
		List<Field> pkFields = getPKFields(clazz, Id.class);
		
		if (pkFields.size() == 1) {
			return pkFields.get(0);
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
			SecurityException, NoSuchMethodException {
		for (Entry<String, Object> entry: map.entrySet()) {
			Field firstField = ReflectionUtils.getFirstField(entry.getKey(), clazz);
			Class<?> firstFieldClass = firstField.getType();
			
			if (ReflectionUtils.isEntity(firstFieldClass)) {
				Map<String, Object> nestedMap = (Map<String, Object>) entry.getValue();				
				Object oldValue = PropertyUtils.getProperty(entity, firstField.getName());
				Object newValue = patch(nestedMap, oldValue, firstFieldClass);
				BeanUtils.setProperty(entity, entry.getKey(), newValue);				
			} else if (ObjectUtils.isOrExtendsIterable(firstFieldClass)) {
				Class<?> nestedClass = ReflectionUtils.getFieldEntityOrType(firstField);
				String pkName = ReflectionUtils.getPKField(nestedClass).getName();
				
				List<Object> oldList = (List<Object>) PropertyUtils.getProperty(entity, firstField.getName());
				List<Map<String, Object>> newList = (List<Map<String, Object>>) entry.getValue();
				List<Object> patchedList = oldList;
				
				for (Map<String, Object> newMap: newList) {
					
					int i = 0;
					for (Object oldEntity: oldList) {
						Object newEntity = (Object) ObjectUtils.mapToPojo(newMap, nestedClass);
						
						if (getPK(oldEntity).equals(getPK(newEntity))) {
							newMap.remove(pkName);
							oldList.set(i, patch(newMap, oldEntity, nestedClass));
							break;
						} 
						
						i++;
					}
				}
				
				BeanUtils.setProperty(entity, entry.getKey(), patchedList);
			} else if (ReflectionUtils.isId(firstField)) {
				throw new IllegalArgumentException("Não é permitido alterar chaves primárias. Remova o campo " + firstField.getName());
			} else {
				BeanUtils.setProperty(entity, entry.getKey(), entry.getValue());
			}
		}
		
		return entity;
	}
	
	public static Object getPK(Object entity) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Class<?> clazz = entity.getClass();		
		Field[] fields = clazz.getDeclaredFields();
		
		for (Field f: fields) {
			if (f.isAnnotationPresent(Id.class) || f.isAnnotationPresent(EmbeddedId.class)) {
				BeanUtils.getProperty(entity, f.getName());
			}
		}
		
		throw new NullPointerException();
	}
	
}
