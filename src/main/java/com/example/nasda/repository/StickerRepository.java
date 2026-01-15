package com.example.nasda.repository;

import com.example.nasda.domain.StickerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StickerRepository extends JpaRepository<StickerEntity, Long> {}