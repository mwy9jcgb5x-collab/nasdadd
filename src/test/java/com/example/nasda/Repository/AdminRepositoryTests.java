package com.example.nasda.Repository;

import com.example.nasda.domain.*;
import com.example.nasda.repository.*;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

@SpringBootTest
@Log4j2
//@Transactional // ✅ 데이터 안 바꿔도 무한 재실행 가능하게 해주는 치트키
public class AdminRepositoryTests {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ForbiddenWordRepository forbiddenWordRepository;
    @Autowired private CommentReportRepository commentReportRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private PostRepository postRepository;

    private UserEntity commonUser;
    private CategoryEntity commonCategory;

    @BeforeEach
    void setUp() {
        // 모든 테스트의 기초: 유저와 카테고리 (데이터 고정)
        commonCategory = CategoryEntity.builder()
                .categoryName("고정 카테고리")
                .isActive(true)
                .build();
        categoryRepository.save(commonCategory);

        commonUser = UserEntity.builder()
                .nickname("관리자1")
                .email("admin_fixed1@test.com")
                .password("12345")
                .loginId("admin_fixed_id1")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(commonUser);
    }

    // 1. 카테고리 관리 (반복문)
    @Test
    void testCategory() {
        IntStream.rangeClosed(1, 10).forEach(i -> {
            categoryRepository.save(CategoryEntity.builder()
                    .categoryName("신규 카테고리_" + i)
                    .isActive(true)
                    .build());
        });
    }

    // 2. 금지어 관리 (반복문)
    @Test
    void testForbiddenWord() {
//        IntStream.rangeClosed(1, 10).forEach(i -> {
            forbiddenWordRepository.save(ForbiddenWordEntity.builder()
//                                                            .word("금지어_" + i)
                                                            .word("금지어_111")
                                                            .build());
//        });
    }

    // 3. 유저 관리
    @Test
    void testUser() {
        UserEntity user = UserEntity.builder()
                .email("test_user@nasda.com").loginId("test_user").nickname("테스터").password("1234").build();
        userRepository.save(user);
    }


    // 4. 게시글(Post) 생성 테스트
    @Test
    @Rollback(false) // 👈 여기에 추가하세요! (org.springframework.test.annotation.Rollback 임포트)
    void testPost() {
        PostEntity post = PostEntity.builder()
                .title("화면 확인용 테스트 글") // 제목을 알아보기 쉽게 바꿨어요
                .user(commonUser)
                .category(commonCategory)
                .description("이 글이 보이면 성공입니다.")
                .viewCount(0)
                .isMain(false)
                .build();
        postRepository.save(post);

        log.info("생성된 게시글 번호(postId): " + post.getPostId());
    }

    // 5. 댓글(Comment/Reply) 생성
    @Test
    void testComment() {
        PostEntity post = PostEntity.builder().title("댓글용").user(commonUser).category(commonCategory).build();
        postRepository.save(post);

        CommentEntity comment = CommentEntity.builder()
                .content("댓글 테스트")
                .user(commonUser)
                .post(post)
                .build();
        commentRepository.save(comment);
    }

    // 6. 신고(Report) 생성
    @Test
    void testReport() {
        PostEntity post = PostEntity.builder().title("신고용").user(commonUser).category(commonCategory).build();
        postRepository.save(post);
        CommentEntity comment = CommentEntity.builder().content("신고대상").user(commonUser).post(post).build();
        commentRepository.save(comment);

        CommentReportEntity report = CommentReportEntity.builder()
                .reason("부적절함")
                .status(ReportStatus.PENDING)
                .reporter(commonUser)
                .comment(comment)
                .build();
        commentReportRepository.save(report);
    }

    @Test
    public void testUpdate() {
        // 1. 수정 테스트를 위해 임시 데이터를 하나 먼저 저장합니다.
        CategoryEntity temp = CategoryEntity.builder()
                .categoryName("수정 전 이름")
                .isActive(true)
                .build();
        CategoryEntity saved = categoryRepository.save(temp); // DB가 번호를 새로 따줍니다.

        // 2. DB가 준 '진짜 번호'를 꺼냅니다.
        Integer realId = saved.getCategoryId();

        // 3. 그 번호를 그대로 사용해서 수정할 데이터를 만듭니다.
        CategoryEntity updateTarget = CategoryEntity.builder()
                .categoryId(realId) // 🚩 수동 번호(12) 대신 진짜 번호를 넣음!
                .categoryName("리포지토리에서 수정 성공")
                .isActive(true)
                .build();

        // 4. 저장 (JPA가 ID가 있는 것을 보고 Update 쿼리를 날립니다)
        categoryRepository.save(updateTarget);

        log.info("수정 완료된 ID: " + realId);
    }
}