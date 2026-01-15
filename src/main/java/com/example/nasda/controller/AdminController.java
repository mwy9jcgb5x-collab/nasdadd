package com.example.nasda.controller;

import com.example.nasda.dto.*;
import com.example.nasda.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;

// 🚩 에러 방지를 위해 static import locale filter 줄을 제거했습니다.

@Controller
@RequestMapping("/admin")
@Log4j2
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ---------------------------------------------------------
    // 1 & 6단계: 대시보드 데이터 통합 출력
    // ---------------------------------------------------------
    @GetMapping("/dashboard")
    public String adminMain(Model model)  { // void -> String으로 변경
        log.info("대시보드 로딩 중...");

        try {
            model.addAttribute("categoryList", adminService.getAllCategories());
            model.addAttribute("wordList", adminService.getAllWords());
            model.addAttribute("reportList", adminService.getPendingPostReports());
            model.addAttribute("userList", Collections.emptyList());
        } catch (Exception e) {
            log.error("데이터 로딩 중 오류 발생: " + e.getMessage());
            model.addAttribute("categoryList", Collections.emptyList());
            model.addAttribute("wordList", Collections.emptyList());
            model.addAttribute("reportList", Collections.emptyList());
        }

        // 도현님, HTML 파일이 templates 폴더 바로 아래 있으면 "dashboard"
        // templates/admin/ 폴더 안에 있으면 "admin/dashboard" 라고 적으세요.
        return "admin/dashboard";
    }

    // ---------------------------------------------------------
    // 2 & 3단계: 신고 처리 (상태 변경 PENDING -> RESOLVED)
    // ---------------------------------------------------------
    @PostMapping("/report/process")
    public String processReport(@RequestParam("reportId") Integer reportId,
                                @RequestParam("action") String action,
                                RedirectAttributes rttr) {
        log.info("신고 처리 실행 - ID: {}, 조치: {}", reportId, action);

        adminService.processPostReport(reportId, action, "관리자 승인 처리");

        rttr.addFlashAttribute("result", "processed");
        return "redirect:/admin/dashboard";
    }

    // ---------------------------------------------------------
    // 4 & 5단계: 관리 항목(금지어, 카테고리) 등록 및 수정/삭제
    // ---------------------------------------------------------

    // [등록 화면] ✅ required = false 추가로 400 에러 해결
    @GetMapping("/register")
    public String registerGET(@RequestParam(value = "type", required = false, defaultValue = "word") String type,
                              Model model) {
        log.info("등록 페이지 진입 - type: " + type);
        model.addAttribute("type", type);

        // 🚩 수정 포인트: 폴더 구조가 templates/admin/register.html 이라면 아래처럼!
        return "admin/register";
    }
    // [등록 처리]
    @PostMapping("/register")
    public String registerPost(@RequestParam("type") String type,
                               CategoryDTO categoryDTO,
                               ForbiddenWordDTO wordDTO,
                               RedirectAttributes rttr) {
        if ("category".equals(type)) {
            adminService.registerCategory(categoryDTO);
            rttr.addAttribute("section", "categories"); // 카테고리 탭으로 이동
        } else if ("word".equals(type)) {
            adminService.registerWord(wordDTO);
            rttr.addAttribute("section", "banned"); // 금지어 탭으로 이동
        }
        return "redirect:/admin/dashboard";
    }

    // [수정 화면] ✅ id와 type 모두 required=false 설정 및 방어 코드 추가
    @GetMapping("/modify")
    public String modifyGET(@RequestParam(value = "type", required = false) String type,
                            @RequestParam(value = "id", required = false) Integer id,
                            Model model) {
        log.info("수정 페이지 진입 - type: {}, id: {}", type, id);

        if (type == null || id == null) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("type", type);

        // [AdminController.java 수정]

        if ("category".equals(type)) {
            // ✅ 이미 만드신 readOneCategory를 호출하세요
            model.addAttribute("dto", adminService.readOneCategory(id));
        } else if ("word".equals(type)) {
            // ✅ 이미 만드신 readOneWord 한 줄이면 끝납니다.
            model.addAttribute("dto", adminService.readOneWord(id));
        }

        // 🚩 수정 포인트: 폴더 구조가 templates/admin/modify.html 이라면 아래처럼!
        return "admin/modify";
    }
    // [수정 처리]
    @PostMapping("/modify")
    public String modifyPost(@RequestParam("type") String type,
                             CategoryDTO categoryDTO,
                             ForbiddenWordDTO wordDTO,
                             RedirectAttributes rttr) {
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
    public String delete(@RequestParam("type") String type,
                         @RequestParam("id") Integer id,
                         RedirectAttributes rttr) {

        if ("word".equals(type)) {
            adminService.removeWord(id);
            // ✅ 삭제 후 대시보드로 갈 때 'section=banned'라고 알려줌
            rttr.addAttribute("section", "banned");
        } else if ("category".equals(type)) {
            adminService.removeCategory(id);
            // ✅ 삭제 후 대시보드로 갈 때 'section=categories'라고 알려줌
            rttr.addAttribute("section", "categories");
        }

        return "redirect:/admin/dashboard";
    }
}