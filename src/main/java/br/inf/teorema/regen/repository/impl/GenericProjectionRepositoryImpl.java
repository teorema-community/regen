package br.inf.teorema.regen.repository.impl;

import br.inf.teorema.regen.enums.FunctionType;
import br.inf.teorema.regen.model.Condition;
import br.inf.teorema.regen.model.Projection;
import br.inf.teorema.regen.model.SelectAndWhere;
import br.inf.teorema.regen.repository.GenericProjectionRepository;
import br.inf.teorema.regen.specification.GenericSpecification;
import br.inf.teorema.regen.util.ObjectUtils;
import br.inf.teorema.regen.util.ReflectionUtils;
import br.inf.teorema.regen.util.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GenericProjectionRepositoryImpl<T> implements GenericProjectionRepository<T> {

    private EntityManager entityManager = null;

    @Autowired
    public GenericProjectionRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public GenericProjectionRepositoryImpl() {}

    @Override
    public Page<Map<String, Object>> findAllBySpecificationAndProjections(SelectAndWhere selectAndWhere, Pageable pageable, Class<T> clazz) throws NoSuchFieldException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();        

        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> root = countQuery.from(clazz);
        
        Specification<T> specification = null;
        List<String> groupBy = null;
        
        if (selectAndWhere.getWhere() != null) {
        	groupBy = selectAndWhere.getWhere().getGroupBy();
        	selectAndWhere.getWhere().setGroupBy(null);
        	specification = new GenericSpecification<>(selectAndWhere.getWhere(), clazz);
        }
        
        Long count = count(criteriaBuilder, countQuery, specification, root, selectAndWhere.isDistinct());

        CriteriaQuery<Tuple> tupleQuery = criteriaBuilder.createTupleQuery();
        root = tupleQuery.from(clazz);
        List<Projection> projectionList = createProjections(criteriaBuilder, root, selectAndWhere.getSelect(), clazz, new HashMap<>());
        tupleQuery = applyProjection(tupleQuery, projectionList);

        if (selectAndWhere.getWhere() != null) {
        	selectAndWhere.getWhere().setGroupBy(groupBy);
        	specification = new GenericSpecification<>(selectAndWhere.getWhere(), clazz);
            tupleQuery.where(specification.toPredicate(root, tupleQuery, criteriaBuilder));
        }

        List<Tuple> tuples = paginate(tupleQuery, pageable);
        List<Map<String, Object>> entities = parseResultList(tuples, projectionList, clazz);

        return new PageImpl<Map<String, Object>>(entities, new PageRequest(pageable.getPageNumber(), pageable.getPageSize()), count);
    }
    
    @Override
	public Slice<Map<String, Object>> findAllBySpecificationAndProjectionsSliced(SelectAndWhere selectAndWhere,
			Pageable pageable, Class<T> clazz) throws NoSuchFieldException {
    	CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();        
        Specification<T> specification = null;

        CriteriaQuery<Tuple> tupleQuery = criteriaBuilder.createTupleQuery();
        Root<T> root = tupleQuery.from(clazz);
        List<Projection> projectionList = createProjections(criteriaBuilder, root, selectAndWhere.getSelect(), clazz, new HashMap<>());
        tupleQuery = applyProjection(tupleQuery, projectionList);

        if (selectAndWhere.getWhere() != null) {
        	specification = new GenericSpecification<>(selectAndWhere.getWhere(), clazz);
            tupleQuery.where(specification.toPredicate(root, tupleQuery, criteriaBuilder));
        }

        List<Tuple> tuples = paginate(tupleQuery, pageable);
        List<Map<String, Object>> entities = parseResultList(tuples, projectionList, clazz);

        return new SliceImpl<Map<String, Object>>(
    		entities, 
    		pageable, 
    		checkHasNext(criteriaBuilder, specification, pageable, clazz)
		);
	}

	private boolean checkHasNext(CriteriaBuilder criteriaBuilder, Specification<T> specification, Pageable pageable, Class<T> clazz) {
		if (pageable == null) {
			return false;
		}
		
		CriteriaQuery<Boolean> query = criteriaBuilder.createQuery(Boolean.class);
		Root<T> root = query.from(clazz);
		
		query.select(criteriaBuilder.literal(true));   

        if (specification != null) {
        	query.where(specification.toPredicate(root, query, criteriaBuilder));
        }
        
        TypedQuery<Boolean> typedQuery = entityManager.createQuery(query);        
        typedQuery.setFirstResult((pageable.getPageNumber() + 1) * pageable.getPageSize());
        typedQuery.setMaxResults(1);

        try {
        	return typedQuery.getSingleResult();
        } catch (NoResultException e) {
        	return false;
        }
	}

    public Long count(CriteriaBuilder criteriaBuilder, CriteriaQuery<Long> countQuery, Specification<T> specification, Root<T> root, Boolean distinct) {
    	if (distinct) {
    		countQuery.select(criteriaBuilder.countDistinct(root));
    	} else {
    		countQuery.select(criteriaBuilder.count(root));
    	}        

        if (specification != null) {
            countQuery.where(specification.toPredicate(root, countQuery, criteriaBuilder));
        }

        try {
        	return entityManager.createQuery(countQuery).getSingleResult();
        } catch (NoResultException e) {
        	return 0l;
        }
    }

    public List<Tuple> paginate(CriteriaQuery<Tuple> tupleQuery, Pageable pageable) {
        TypedQuery<Tuple> typedQuery = entityManager.createQuery(tupleQuery);

        if (pageable != null) {
            typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            typedQuery.setMaxResults(pageable.getPageSize());
        }

        return typedQuery.getResultList();
    }

    public <T> CriteriaQuery<Tuple> applyProjection(CriteriaQuery<Tuple> tupleQuery, List<Projection> projectionList) throws NoSuchFieldException {
    	List<Selection<?>> selections = new ArrayList<>();        

        for (Projection projection : projectionList) {
        	selections.add(projection.getExpression().alias(projection.getAlias()));
        }
    	
        tupleQuery.multiselect(selections.toArray(new Selection[selections.size()]));
        return tupleQuery;
    }
    
    public <T> List<Projection> createProjections(CriteriaBuilder criteriaBuilder, Root<T> root, List<String> projections, Class<T> clazz, Map<String, Join<?, ?>> joins) throws NoSuchFieldException {
        List<Projection> list = new ArrayList<>();

        for (String p : projections) {
            Projection projection = new Projection(p);
            
            if (projection.hasFunction()) {
            	List<Projection> subProjections = createProjections(criteriaBuilder, root, projection.getParameters(), clazz, joins);
            	projection.applyFunction(criteriaBuilder, root, subProjections);
            } else {
	            List<Field> fields = ReflectionUtils.getFields(projection.getName(), clazz);
	
	            if (fields.size() <= 1) {
	                Field field = fields.get(0);
	
	                if (ReflectionUtils.isEntity(field.getType())) {
	                	projection.setExpression(root.join(projection.getName(), JoinType.LEFT));
	                } else {
	                	projection.setExpression(root.get(projection.getName()));
	                }
	            } else {
	                Join<?, ?> join = null;
	                int i = 0;
	                for (Field field : fields) {
	                    String fieldName = field.getName();
	
	                    if (i == 0) {
	                        if (joins.containsKey(fieldName)) {
	                            join = joins.get(fieldName);
	                        } else {
	                            join = root.join(fieldName, JoinType.LEFT);
	                            joins.put(fieldName, join);
	                        }
	                    } else if (i < fields.size() - 1) {
	                        if (joins.containsKey(fieldName)) {
	                            join = joins.get(fieldName);
	                        } else {
	                            join = join.join(fieldName, JoinType.LEFT);
	                            joins.put(fieldName, join);
	                        }
	                    } else {
	                    	projection.setExpression(join.get(fieldName));
	                    }
	
	                    i++;
	                }
	            }
            }
            
            list.add(projection);
        }

        return list;
    }

    public List<Map<String, Object>> parseResultList(List<Tuple> tuples, List<Projection> projectionList, Class<T> clazz) throws NoSuchFieldException {
        List<Map<String, Object>> entities = new ArrayList<Map<String, Object>>();

        for (Tuple tuple : tuples) {
            Map<String, Object> map = new HashMap<>();

            for (Projection projection : projectionList) {
                Map<String, Object> lastMap = map;
                
        		if (projection.hasFunction()) {
        			map.put(projection.getAlias(), tuple.get(projection.getAlias(), Object.class));  					
        		} else {                
	                List<Field> fields = ReflectionUtils.getFields(projection.getName(), clazz);
	
	                int i = 0;
	                for (Field field : fields) {
	                    String fieldName = field.getName();
	
	                    if (i < fields.size() - 1) {
	                        if (lastMap.containsKey(fieldName)) {
	                            lastMap = (Map<String, Object>) lastMap.get(fieldName);
	                        } else {
	                            Map<String, Object> newMap = new HashMap<>();
	                            lastMap.put(fieldName, newMap);
	                            lastMap = newMap;
	                        }
	                    } else {
	                    	Object value = tuple.get(projection.getAlias(), field.getType());
	                    	
	                    	if (value != null) {
	                    		if (projection.isHasAlias()) {
	                    			fieldName = projection.getAlias();
	                    		}
	                    		
	                    		lastMap.put(fieldName, value);
	                    	}
	                    }
	
	                    i++;
	                }
        		}
            }

            map = ObjectUtils.removeNullAndEmptyValues(map);
            entities.add(map);
        }

        return entities;
    }

}

