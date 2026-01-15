package com.example.nasda.service;

import com.example.nasda.dto.*;
import java.util.List;

public interface AdminService {
    // [1단계: 관리자 권한 확인]
    boolean isAdmin(String userId);

    // [2, 3단계: 신고 처리 및 유저 제재/알림]
    List<PostReportDTO> getPendingPostReports();
    List<CommentReportDTO> getPendingCommentReports();
    void processPostReport(Integer reportId, String action, String adminComment);
    void processCommentReport(Integer reportId, String action, String adminComment);

    // [4단계: 금지어 관리]
    List<ForbiddenWordDTO> getAllWords();
    void registerWord(ForbiddenWordDTO wordDTO);
    void modifyWord(ForbiddenWordDTO wordDTO);
    void removeWord(Integer fno);
    boolean checkForbiddenWords(String content);

    // [5단계: 카테고리 관리]
    List<CategoryDTO> getAllCategories();
    void registerCategory(CategoryDTO categoryDTO);
    void modifyCategory(CategoryDTO categoryDTO);
    void removeCategory(Integer categoryId);

    // 공통: 단건 조회 (수정용) - [에러 해결을 위해 Impl의 이름과 맞춤]
    CategoryDTO readOneCategory(Integer id);
    ForbiddenWordDTO readOneWord(Integer id);
}
