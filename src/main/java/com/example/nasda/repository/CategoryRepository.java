package com.example.nasda.repository;

import com.example.nasda.domain.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {

    // ✅ 이 한 줄이 없으면 Impl에서 백날 수정해도 빨간 줄 안 사라집니다!
    void deleteByCategoryName(String categoryName);
}