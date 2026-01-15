package com.example.nasda.dto;

import com.example.nasda.domain.ReportStatus;
import com.example.nasda.domain.ProcessResult;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentReportDTO {
    private Integer reportId;
    private String reason;
    private ReportStatus status;
    private String adminComment;
    private ProcessResult processResult;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}