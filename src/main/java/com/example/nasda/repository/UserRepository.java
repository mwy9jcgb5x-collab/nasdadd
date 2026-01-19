package com.example.nasda.repository;

import com.example.nasda.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    // UserEntity의 ID가 Integer이므로 여기도 Integer여야 에러가 안 납니다!
    @Query(value = "SELECT user_id, nickname, status, suspension_end_date FROM users", nativeQuery = true)
    List<Map<String, Object>> findAllUserStatusRaw();
}

