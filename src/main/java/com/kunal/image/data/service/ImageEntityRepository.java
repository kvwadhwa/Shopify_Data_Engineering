package com.kunal.image.data.service;

import com.kunal.image.data.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ImageEntityRepository extends JpaRepository<ImageEntity, Integer>, JpaSpecificationExecutor<ImageEntity> { }