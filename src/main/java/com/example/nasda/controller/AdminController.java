package com.example.nasda.controller;

import com.example.nasda.dto.*;
import com.example.nasda.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;

@Controller
@RequestMapping("/admin")
@Log4j2
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ---------------------------------------------------------
    // [미래 참고용 주석 블록] - 나중에 이 구조로 합치시면 됩니다.
    // ---------------------------------------------------------
    /*
    @GetMapping("/dashboard")
    public String adminMain(Model model,
                            @RequestParam(value = "section", defaultValue = "accounts") String section,
                            @RequestParam(value = "type", defaultValue = "post") String type,
                            @RequestParam(value = "postPage", defaultValue = "0") int postPage,
                            @RequestParam(value = "commentPage", defaultValue = "0") int commentPage,
                            @RequestParam(value = "userPage", defaultValue = "0") int userPage) {
        log.info("대시보드 통합 로딩 중...");

        try {
            // 1. 페이징 설정
            Pageable postPageable = PageRequest.of(postPage, 10, Sort.by("reportId").descending());
            Pageable commentPageable = PageRequest.of(commentPage, 10, Sort.by("reportId").descending());

            model.addAttribute("section", section);
            model.addAttribute("type", type);
            model.addAttribute("categoryList", adminService.getAllCategories());
            model.addAttribute("wordList", adminService.getAllWords());

            // 2. 신고 페이징 처리
            Page<PostReportDTO> postReportPage = adminService.getPendingPostReports(postPageable);
            model.addAttribute("postReportList", postReportPage.getContent());
            model.addAttribute("postTotalPages", postReportPage.getTotalPages());

            Page<CommentReportDTO> commentReportPage = adminService.getPendingCommentReports(commentPageable);
            model.addAttribute("commentReportList", commentReportPage.getContent());
            model.addAttribute("commentTotalPages", commentReportPage.getTotalPages());

        } catch (Exception e) {
            log.error("오류 발생: " + e.getMessage());
        }
        return "admin/dashboard";
    }

    // [미래 주석] 등록 처리 시 중복 체크 포함 버전
    @PostMapping("/register")
    public String registerPost(String type, CategoryDTO categoryDTO, ForbiddenWordDTO wordDTO, RedirectAttributes rttr) {
        try {
            if ("category".equals(type)) { adminService.registerCategory(categoryDTO); }
            else { adminService.registerWord(wordDTO); }
        } catch (RuntimeException e) {
            rttr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
    */

    // ---------------------------------------------------------
    // [현재 실행 코드] 탭 구분 및 임시 유저 로직 포함
    // ---------------------------------------------------------
    @GetMapping("/dashboard")
    public String adminMain(Model model,
                            @RequestParam(value = "section", defaultValue = "accounts") String section,
                            @RequestParam(value = "type", defaultValue = "post") String type,
                            @RequestParam(value = "postPage", defaultValue = "0") int postPage,
                            @RequestParam(value = "commentPage", defaultValue = "0") int commentPage){
        log.info("대시보드 실행 - 섹션: {}, 타입: {}", section, type);

        try {
            model.addAttribute("section", section);
            model.addAttribute("type", type);

            Pageable postPageable = PageRequest.of(postPage, 10, Sort.by("reportId").descending());
            Pageable commentPageable = PageRequest.of(commentPage, 10, Sort.by("reportId").descending());

            model.addAttribute("categoryList", adminService.getAllCategories());
            model.addAttribute("wordList", adminService.getAllWords());
            model.addAttribute("userList", adminService.getUserStatusList());

            Page<PostReportDTO> postReportPage = adminService.getPendingPostReports(postPageable);
            model.addAttribute("postReportList", postReportPage.getContent());
            model.addAttribute("postCurrentPage", postReportPage.getNumber());
            model.addAttribute("postTotalPages", postReportPage.getTotalPages());

            Page<CommentReportDTO> commentReportPage = adminService.getPendingCommentReports(commentPageable);
            model.addAttribute("commentReportList", commentReportPage.getContent());
            model.addAttribute("commentCurrentPage", commentReportPage.getNumber());
            model.addAttribute("commentTotalPages", commentReportPage.getTotalPages());

        } catch (Exception e) {
            log.error("데이터 로딩 중 오류 발생: " + e.getMessage());
            model.addAttribute("categoryList", Collections.emptyList());
            model.addAttribute("wordList", Collections.emptyList());
            model.addAttribute("postReportList", Collections.emptyList());
            model.addAttribute("commentReportList", Collections.emptyList());
            model.addAttribute("userList", Collections.emptyList());
        }

        return "admin/dashboard";
    }

    @PostMapping("/report/process")
    public String processReport(@RequestParam("reportId") Integer reportId,
                                @RequestParam("action") String action,
                                @RequestParam("type") String type,
                                RedirectAttributes rttr) {
        adminService.processPostReport(reportId, action, "관리자 승인 처리");
        rttr.addAttribute("section", "reports");
        rttr.addAttribute("type", type);
        rttr.addFlashAttribute("result", "processed");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/register")
    public String registerGET(@RequestParam(value = "type", required = false, defaultValue = "word") String type, Model model) {
        model.addAttribute("type", type);
        return "admin/register";
    }

    // 🚩 [수정] 등록 처리 시 중복 예외(RuntimeException) 캐치 로직 추가
    @PostMapping("/register")
    public String registerPost(@RequestParam("type") String type,
                               CategoryDTO categoryDTO,
                               ForbiddenWordDTO wordDTO,
                               RedirectAttributes rttr) {
        try {
            if ("category".equals(type)) {
                adminService.registerCategory(categoryDTO);
                rttr.addAttribute("section", "categories");
            } else if ("word".equals(type)) {
                adminService.registerWord(wordDTO);
                rttr.addAttribute("section", "banned");
            }
            rttr.addFlashAttribute("result", "success");
        } catch (RuntimeException e) {
            // 🚩 핵심: Service에서 던진 "이미 존재하는..." 메시지를 화면으로 전달
            log.error("등록 중 중복 발생: " + e.getMessage());
            rttr.addFlashAttribute("error", e.getMessage());
            rttr.addAttribute("section", "category".equals(type) ? "categories" : "banned");
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/modify")
    public String modifyGET(@RequestParam(value = "type", required = false) String type, @RequestParam(value = "id", required = false) Integer id, Model model) {
        if (type == null || id == null) return "redirect:/admin/dashboard";
        model.addAttribute("type", type);
        if ("category".equals(type)) model.addAttribute("dto", adminService.readOneCategory(id));
        else if ("word".equals(type)) model.addAttribute("dto", adminService.readOneWord(id));
        return "admin/modify";
    }

    @PostMapping("/modify")
    public String modifyPost(@RequestParam("type") String type, CategoryDTO categoryDTO, ForbiddenWordDTO wordDTO, RedirectAttributes rttr) {
        if ("category".equals(type)) {
            adminService.modifyCategory(categoryDTO);
            rttr.addAttribute("section", "categories");
        } else if ("word".equals(type)) {
            adminService.modifyWord(wordDTO);
            rttr.addAttribute("section", "banned");
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("type") String type, @RequestParam("id") Integer id, RedirectAttributes rttr) {
        if ("word".equals(type)) {
            adminService.removeWord(id);
            rttr.addAttribute("section", "banned");
        } else if ("category".equals(type)) {
            adminService.removeCategory(id);
            rttr.addAttribute("section", "categories");
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/user-check")
    @ResponseBody
    public java.util.List<java.util.Map<String, Object>> checkUserStatus() {
        return adminService.getUserStatusList();
    }
}