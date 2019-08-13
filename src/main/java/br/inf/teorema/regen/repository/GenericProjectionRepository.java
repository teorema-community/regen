package br.inf.teorema.regen.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.inf.teorema.regen.model.SelectAndWhere;

import java.util.List;
import java.util.Map;

public interface GenericProjectionRepository<T> {
    public Page<Map<String, Object>> findAllBySpecificationAndProjections(Specification<T> specification, SelectAndWhere selectAndWhere, Pageable pageable, Class<T> clazz) throws NoSuchFieldException;
}
