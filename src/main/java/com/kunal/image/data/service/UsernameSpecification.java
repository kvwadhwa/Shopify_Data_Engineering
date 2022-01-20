package com.kunal.image.data.service;

import com.kunal.image.data.entity.ImageEntity;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@AllArgsConstructor
public class UsernameSpecification implements Specification<ImageEntity> {

    private String username;

    @Override
    public Predicate toPredicate(Root<ImageEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.equal(root.get("username"), username);
    }
}
