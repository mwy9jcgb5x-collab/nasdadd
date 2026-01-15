package com.example.nasda.repository;

import com.example.nasda.domain.PostImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImageEntity, Long> {

}