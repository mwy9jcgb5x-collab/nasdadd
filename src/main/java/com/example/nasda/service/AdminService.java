//package com.example.nasda.service;
//
//import com.example.nasda.dto.*;
//import org.springframework.data.domain.Page;    // 👈 반드시 추가 (Pageable 임포트 해결용)
//import org.springframework.data.domain.Pageable; // 👈 반드시 추가
//import java.util.List;
//import java.util.Map;
//
//public interface AdminService {
//    // [1단계: 관리자 권한 확인]
//    boolean isAdmin(String userId);
//
//    // [2, 3단계: 신고 처리 - 페이징 적용된 "진짜" 메서드]
//    Page<PostReportDTO> getPendingPostReports(Pageable pageable);    // 👈 컨트롤러 image_055e84.png 에러 해결
//    Page<CommentReportDTO> getPendingCommentReports(Pageable pageable);
//
//    // [신고 상세 처리 및 상태 변경]
//    void processPostReport(Integer reportId, String action, String adminComment);
//    void processCommentReport(Integer reportId, String action, String adminComment);
//
//    // [유저 관리]
//    List<Map<String, Object>> getUserStatusList();
//
//    // [4단계: 금지어 관리]
//    List<ForbiddenWordDTO> getAllWords();
//    void registerWord(ForbiddenWordDTO wordDTO);
//    void modifyWord(ForbiddenWordDTO wordDTO);
//    void removeWord(Integer fno);
//    boolean checkForbiddenWords(String content);
//    ForbiddenWordDTO readOneWord(Integer id); // 단건 조회 추가
//
//    // [5단계: 카테고리 관리]
//    List<CategoryDTO> getAllCategories();
//    void registerCategory(CategoryDTO categoryDTO);
//    void modifyCategory(CategoryDTO categoryDTO);
//    void removeCategory(Integer categoryId);
//    CategoryDTO readOneCategory(Integer id); // 단건 조회 추가
//}

package com.example.nasda.service;

import com.example.nasda.dto.CategoryDTO;
import com.example.nasda.dto.CommentReportDTO;
import com.example.nasda.dto.ForbiddenWordDTO;
import com.example.nasda.dto.PostReportDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface AdminService {
    // [1단계: 관리자 권한 확인]
    boolean isAdmin(String userId);

    // [2, 3단계: 신고 처리 - 페이징 적용]
    Page<PostReportDTO> getPendingPostReports(Pageable pageable);
    Page<CommentReportDTO> getPendingCommentReports(Pageable pageable);

    // [신고 상세 처리 및 상태 변경]
    void processPostReport(Integer reportId, String action, String adminComment);
    void processCommentReport(Integer reportId, String action, String adminComment);

    // [유저 관리]
    List<Map<String, Object>> getUserStatusList();

    // [4단계: 금지어 관리]
    Page<ForbiddenWordDTO> getBannedWords(Pageable pageable); // 👈 페이징 추가
    List<ForbiddenWordDTO> getAllWords();
    void registerWord(ForbiddenWordDTO wordDTO);
    void modifyWord(ForbiddenWordDTO wordDTO);
    void removeWord(Integer fno);
    boolean checkForbiddenWords(String content);
    ForbiddenWordDTO readOneWord(Integer id);

    // [5단계: 카테고리 관리]
    Page<CategoryDTO> getCategories(Pageable pageable);      // 👈 페이징 추가
    List<CategoryDTO> getAllCategories();
    void registerCategory(CategoryDTO categoryDTO);
    void modifyCategory(CategoryDTO categoryDTO);
    void removeCategory(Integer categoryId);
    CategoryDTO readOneCategory(Integer id);
}