package com.example.nasda.repository;

import com.example.nasda.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    // UserEntity의 ID가 Integer이므로 여기도 Integer여야 에러가 안 납니다!
}