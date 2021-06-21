package br.inf.teorema.regen.repository.impl;

import br.inf.teorema.regen.model.Condition;
import br.inf.teorema.regen.model.SelectAndWhere;
import br.inf.teorema.regen.repository.GenericProjectionRepository;
import br.inf.teorema.regen.util.ObjectUtils;
import br.inf.teorema.regen.util.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public Page<Map<String, Object>> findAllBySpecificationAndProjections(Specification<T> specification, SelectAndWhere selectAndWhere, Pageable pageable, Class<T> clazz) throws NoSuchFieldException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> root = countQuery.from(clazz);
        Long count = count(criteriaBuilder, countQuery, specification, root, selectAndWhere.getWhere().getDistinct());

        CriteriaQuery<Tuple> tupleQuery = criteriaBuilder.createTupleQuery();
        root = tupleQuery.from(clazz);
        tupleQuery = applyProjection(tupleQuery, root, selectAndWhere.getSelect(), clazz);

        if (specification != null) {
            tupleQuery.where(specification.toPredicate(root, tupleQuery, criteriaBuilder));
        }

        List<Tuple> tuples = paginate(tupleQuery, pageable);
        List<Map<String, Object>> entities = parseResultList(tuples, selectAndWhere.getSelect(), clazz);

        Integer listSize = Math.max(1, entities.size());
        return new PageImpl<Map<String, Object>>(entities, new PageRequest(0, listSize), count);
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

    public <T> CriteriaQuery<Tuple> applyProjection(CriteriaQuery<Tuple> tupleQuery, Root<T> root, List<String> projections, Class<T> clazz) throws NoSuchFieldException {
        Map<String, Join<?, ?>> joins = new HashMap<>();
        List<Selection<?>> selections = new ArrayList<>();

        for (String projection : projections) {
            Selection<?> selection = null;
            List<Field> fields = ReflectionUtils.getFields(projection, clazz);

            if (fields.size() <= 1) {
                Field field = fields.get(0);

                if (ReflectionUtils.isEntity(field.getType())) {
                    selection = root.join(projection, JoinType.LEFT);
                } else {
                    selection = root.get(projection);
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
                        selection = join.get(fieldName);
                    }

                    i++;
                }
            }

            selections.add(selection.alias(projection));
        }

        tupleQuery.multiselect(selections.toArray(new Selection[selections.size()]));
        return tupleQuery;
    }

    public List<Map<String, Object>> parseResultList(List<Tuple> tuples, List<String> projections, Class<T> clazz) throws NoSuchFieldException {
        List<Map<String, Object>> entities = new ArrayList<Map<String, Object>>();

        for (Tuple tuple : tuples) {
            Map<String, Object> map = new HashMap<>();

            for (String projection : projections) {
                Map<String, Object> lastMap = map;
                List<Field> fields = ReflectionUtils.getFields(projection, clazz);

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
                    	Object value = tuple.get(projection, field.getType());
                    	
                    	if (value != null) {
                    		lastMap.put(fieldName, value);
                    	}
                    }

                    i++;
                }
            }

            map = ObjectUtils.removeNullAndEmptyValues(map);
            entities.add(map);
        }

        return entities;
    }

}

