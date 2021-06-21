package br.inf.teorema.regen.mediator;

import br.inf.teorema.regen.model.Condition;
import br.inf.teorema.regen.model.SelectAndWhere;
import br.inf.teorema.regen.repository.GenericProjectionRepository;
import br.inf.teorema.regen.specification.GenericSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConditionMediator<T> {

    public Page<T> findAllByCondition(Condition condition, Pageable pageable, JpaSpecificationExecutor<T> executor, Class<T> clazz) {
        return executor.findAll(new GenericSpecification<T>(condition, clazz), pageable);
    }

    public List<T> findAllByCondition(Condition condition, JpaSpecificationExecutor<T> executor, Class<T> clazz) {
        return executor.findAll(new GenericSpecification<T>(condition, clazz));
    }

    public Optional<T> findFirstByCondition(Condition condition, JpaSpecificationExecutor<T> executor, Class<T> clazz) {
        List<T> results = findAllByCondition(condition, new PageRequest(0, 1), executor, clazz).getContent();

        if (results.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(results.get(0));
        }
    }

    public Page<Map<String, Object>> findAllBySelectAndWhere(
        SelectAndWhere selectAndWhere,
        Pageable pageable,
        GenericProjectionRepository<T> projectionRepository,
        Class<T> clazz
    ) throws NoSuchFieldException {
        return projectionRepository.findAllBySpecificationAndProjections(selectAndWhere, pageable, clazz);
    }

    public List<Map<String, Object>> findAllBySelectAndWhere(
            SelectAndWhere selectAndWhere,
            GenericProjectionRepository<T> projectionRepository,
            Class<T> clazz
    ) throws NoSuchFieldException {
        return findAllBySelectAndWhere(selectAndWhere, null, projectionRepository, clazz).getContent();
    }

    public Optional<Map<String, Object>> findFirstBySelectAndWhere(
            SelectAndWhere selectAndWhere,
            GenericProjectionRepository<T> projectionRepository,
            Class<T> clazz
    ) throws NoSuchFieldException {
        List<Map<String, Object>> results = projectionRepository.findAllBySpecificationAndProjections(selectAndWhere, new PageRequest(0, 1), clazz).getContent();

        if (results.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(results.get(0));
        }
    }

}
