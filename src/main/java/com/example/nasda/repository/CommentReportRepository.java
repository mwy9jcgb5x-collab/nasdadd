package com.example.nasda.repository;

import com.example.nasda.domain.CommentReportEntity;
import com.example.nasda.domain.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentReportRepository extends JpaRepository<CommentReportEntity, Integer> {
    // 이 줄을 추가하세요
    List<CommentReportEntity> findByStatus(ReportStatus status);
}
