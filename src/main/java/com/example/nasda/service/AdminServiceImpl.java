package com.example.nasda.service;

import com.example.nasda.domain.*;
import com.example.nasda.dto.*;
import com.example.nasda.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class AdminServiceImpl implements AdminService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ForbiddenWordRepository wordRepository;
    private final PostReportRepository postReportRepository;
    private final PostRepository postRepository;
    private final NotificationRepository notificationRepository; // [알림 저장용 추가]

    // 1. 관리자 권한 확인
    @Override
    public boolean isAdmin(String userId) {
        UserEntity user = userRepository.findById(Integer.parseInt(userId)).orElseThrow();
        return user.getRole() == UserRole.ADMIN;
    }

    // 2. 신고 목록 조회
    @Override
    public List<PostReportDTO> getPendingPostReports() {
        return postReportRepository.findAll().stream()
                .map(e -> modelMapper.map(e, PostReportDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentReportDTO> getPendingCommentReports() {
        return List.of(); // 댓글 신고는 필요 시 구현
    }

    // 3. 신고 처리 및 유저 정지 + 알림 생성 (6단계 핵심 로직)
    @Override
    public void processPostReport(Integer reportId, String action, String adminComment) {
        PostReportEntity report = postReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고 내역이 없습니다."));

        if ("APPROVE".equals(action)) {
            // [A] 게시글 작성자 정지 처리 (UserStatus.SUSPENDED)
            UserEntity writer = report.getPost().getUser();
            UserEntity suspendedUser = UserEntity.builder()
                    .userId(writer.getUserId())
                    .loginId(writer.getLoginId())
                    .password(writer.getPassword())
                    .email(writer.getEmail())
                    .nickname(writer.getNickname())
                    .role(writer.getRole())
                    .status(UserStatus.SUSPENDED) // 정지 상태로 변경
                    .createdAt(writer.getCreatedAt())
                    .build();
            userRepository.save(suspendedUser);

            // [B] 신고된 원본 게시글 삭제
            postRepository.delete(report.getPost());

            // [C] 신고자(Reporter)에게 알림 데이터 생성 (설계도 3단계 반영)
            NotificationEntity notification = NotificationEntity.builder()
                    .receiver(report.getReporter()) // 알림 받을 사람: 신고자
                    .message("신고하신 게시물이 관리자에 의해 처리(삭제)되었습니다.")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification); // 알림 테이블 저장

            log.info("신고 승인 처리 완료: 신고자에게 알림 전송 및 작성자 정지");
        }
    }

    @Override
    public void processCommentReport(Integer reportId, String action, String adminComment) {}

    // 4. 금지어 관리
    @Override
    public List<ForbiddenWordDTO> getAllWords() {
        return wordRepository.findAll().stream()
                .map(e -> modelMapper.map(e, ForbiddenWordDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void registerWord(ForbiddenWordDTO wordDTO) {
        // ModelMapper 대신 직접 빌더로 변환해서 저장합니다.
        ForbiddenWordEntity entity = ForbiddenWordEntity.builder()
                .word(wordDTO.getWord()) // 확실하게 글자 필드에 글자를 넣음
                .build();

        wordRepository.save(entity);
    }

    @Override
    public void modifyWord(ForbiddenWordDTO dto) {
        // 1. 여기서 dto.getFno()를 사용해서 기존 데이터를 찾습니다.
        ForbiddenWordEntity word = wordRepository.findById(dto.getForbiddenwordId().intValue()).orElseThrow();

        wordRepository.save(ForbiddenWordEntity.builder()
                .wordId(word.getWordId()) // 🚩 2. 그런데 빌더에서는 wordId를 쓰고 있습니다!
                .word(dto.getWord())
                .build());
    }

    @Override
    public void removeWord(Integer id) {
        log.info("금지어 삭제 번호: " + id);
        // findById 후 삭제하는 방식은 아주 안전합니다. ✅
        wordRepository.findById(id).ifPresentOrElse(word -> {
            wordRepository.delete(word);
            log.info("삭제 완료!");
        }, () -> {
            log.error("삭제 실패: " + id + "번 금지어를 찾을 수 없습니다.");
        });
    }
    @Override
    public boolean checkForbiddenWords(String content) {
        return wordRepository.findAll().stream()
                .anyMatch(w -> content.contains(w.getWord()));
    }

    // 5. 카테고리 관리
    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(e -> modelMapper.map(e, CategoryDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void registerCategory(CategoryDTO dto) {
        categoryRepository.save(modelMapper.map(dto, CategoryEntity.class));
    }

    @Override
    public void modifyCategory(CategoryDTO dto) {
        // 1. 수정할 데이터가 실제로 있는지 확인 (없으면 여기서 멈춤)
        categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("ID " + dto.getCategoryId() + "번이 없습니다."));

        // 2. 엔티티를 수정하는 대신, 동일한 ID를 가진 새 엔티티를 빌더로 생성
        CategoryEntity updatedEntity = CategoryEntity.builder()
                .categoryId(dto.getCategoryId())    // ID가 같아야 Update가 됩니다.
                .categoryName(dto.getCategoryName())
                .isActive(true)                      // 기본값 적용
                .build();

        // 3. 덮어쓰기 (JPA가 Update 쿼리를 날림)
        categoryRepository.save(updatedEntity);
    }

    @Override
    public void removeCategory(Integer id) {
        categoryRepository.deleteById(id);
    }

    // 6. 단건 조회 (수정 페이지용)
    @Override
    public CategoryDTO readOneCategory(Integer id) {
        // 🚩 실습의 readOne과 똑같은 모양입니다. 대상만 categoryRepository일 뿐이에요.
        return categoryRepository.findById(id)
                .map(e -> modelMapper.map(e, CategoryDTO.class))
                .orElseThrow(() -> new NoSuchElementException("카테고리 없음: " + id));
    }

    @Override
    public ForbiddenWordDTO readOneWord(Integer id) {
        // 🚩 금지어도 똑같습니다. builder를 써서 새 필드명(forbiddenwordid)에 잘 담아줍니다.
        return wordRepository.findById(id)
                .map(e -> ForbiddenWordDTO.builder()
                        .forbiddenwordId(e.getWordId())
                        .word(e.getWord())
                        .build())
                .orElseThrow(() -> new NoSuchElementException("금지어 없음: " + id));
    }
}