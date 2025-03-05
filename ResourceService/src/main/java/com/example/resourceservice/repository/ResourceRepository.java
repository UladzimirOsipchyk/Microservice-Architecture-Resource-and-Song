package com.example.resourceservice.repository;

import com.example.resourceservice.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
  List<Resource> findAllByIdIsIn(List<Long> ids);
}
