package com.example.nasda.repository;

import java.util.List;

import com.example.nasda.domain.PostReportEntity;
import com.example.nasda.domain.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReportRepository extends JpaRepository<PostReportEntity, Integer>
 {

    List<PostReportEntity> findByStatus(ReportStatus status);
}