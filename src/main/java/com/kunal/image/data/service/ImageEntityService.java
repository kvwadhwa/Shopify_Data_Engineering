package com.kunal.image.data.service;

import com.kunal.image.data.entity.ImageEntity;
import java.util.Optional;

import com.kunal.image.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ImageEntityService {

    private ImageEntityRepository repository;

    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public ImageEntityService(@Autowired ImageEntityRepository repository) {
        this.repository = repository;
    }

    public Optional<ImageEntity> get(Integer id) {
        return repository.findById(id);
    }

    public ImageEntity update(ImageEntity entity) {
        entity.setUsername(getUsername());
        return repository.save(entity);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public Page<ImageEntity> listPublicImages(Pageable pageable) {
        return repository.findAll(Specification.where(new IsPublicSpecification()), pageable);
    }

    public Page<ImageEntity> listUserImages(Pageable pageable) {
        return repository.findAll(Specification.where(new UsernameSpecification(getUsername())), pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
