package com.example.nasda.repository;

import com.example.nasda.domain.StickerCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StickerCategoryRepository extends JpaRepository<StickerCategoryEntity, Integer> {}