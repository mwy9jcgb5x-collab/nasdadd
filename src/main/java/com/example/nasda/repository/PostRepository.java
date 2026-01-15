package com.example.nasda.repository;


import com.example.nasda.domain.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Integer> {
    // 필요하다면 커스텀 쿼리 메서드를 추가할 수 있습니다.
    // 예: List<PostEntity> findByAuthor(UserEntity author);
}
