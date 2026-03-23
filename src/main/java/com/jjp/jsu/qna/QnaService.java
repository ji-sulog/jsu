package com.jjp.jsu.qna;

import com.jjp.jsu.common.BadRequestException;
import com.jjp.jsu.common.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaPostRepository postRepo;
    private final QnaReplyRepository replyRepo;
    private final QnaPostHistoryRepository historyRepo;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    /* ── 유틸 ──────────────────────────────────────────────────────── */

    public String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    /* ── 목록 ──────────────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<QnaPostSummaryResponse> getPublicList() {
        return postRepo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QnaPostSummaryResponse> getAllList() {
        return postRepo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    private QnaPostSummaryResponse toSummary(QnaPost p) {
        return new QnaPostSummaryResponse(
                p.getId(),
                p.getTitle(),
                p.getStatus(),
                p.isPublicPost(),
                p.getCreatedAt().format(FMT),
                p.isDeleted()
        );
    }

    /* ── 글쓰기 ────────────────────────────────────────────────────── */

    @Transactional
    public QnaIdResponse createPost(QnaCreatePostRequest request) {
        validateCreatePostRequest(request);

        String passwordHash = (request.password() != null && !request.password().isBlank())
                ? hash(request.password()) : null;

        QnaPost post = postRepo.save(new QnaPost(
                request.title(),
                normalizeContent(request.content()),
                request.isPublic(),
                passwordHash
        ));

        return new QnaIdResponse(post.getId(), "OK");
    }

    /* ── 상세 조회 ─────────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public QnaPostDetailResponse getDetail(Long postId, String password, boolean isAdmin) {
        QnaPost post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        validateReadAccess(post, password, isAdmin);

        List<QnaReplyResponse> replies = replyRepo.findByPostOrderByCreatedAtAsc(post)
                .stream()
                .map(r -> new QnaReplyResponse(
                        r.getId(),
                        r.getContent(),
                        r.getCreatedAt().format(FMT)
                ))
                .toList();

        List<QnaPostHistoryResponse> histories = isAdmin
                ? historyRepo.findByPostOrderByChangedAtDesc(post).stream()
                .map(h -> new QnaPostHistoryResponse(
                        h.getId(),
                        h.getAction(),
                        h.getTitle(),
                        h.getContent(),
                        h.getChangedAt().format(FMT)
                ))
                .toList()
                : List.of();

        return new QnaPostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.isDeleted() ? "삭제된 게시물입니다." : post.getContent(),
                post.getStatus(),
                post.isPublicPost(),
                post.getCreatedAt().format(FMT),
                post.getUpdatedAt().format(FMT),
                post.isDeleted(),
                !post.isDeleted() && !post.isPublicPost(),
                replies,
                histories
        );
    }

    @Transactional
    public QnaStatusResponse updatePost(Long postId, String password, boolean isAdmin, QnaUpdatePostRequest request) {
        validateUpdateRequest(request);

        QnaPost post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        validateEditableAccess(post, password, isAdmin);
        saveHistory(post, "UPDATED");
        post.update(request.title(), normalizeContent(request.content()));
        postRepo.save(post);
        return new QnaStatusResponse("OK");
    }

    @Transactional
    public QnaStatusResponse deletePost(Long postId, String password, boolean isAdmin) {
        QnaPost post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        validateEditableAccess(post, password, isAdmin);
        saveHistory(post, "DELETED");
        post.markDeleted();
        postRepo.save(post);
        return new QnaStatusResponse("OK");
    }

    /* ── 답변 등록 (관리자 전용) ───────────────────────────────────── */

    @Transactional
    public QnaIdResponse addReply(Long postId, QnaCreateReplyRequest request) {
        validateCreateReplyRequest(request);

        QnaPost post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.markAnswered();
        postRepo.save(post);

        QnaReply reply = replyRepo.save(new QnaReply(post, request.content()));
        return new QnaIdResponse(reply.getId(), "OK");
    }

    private void validateCreatePostRequest(QnaCreatePostRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new BadRequestException("제목을 입력하세요.");
        }
        if (!request.isPublic() && (request.password() == null || request.password().isBlank())) {
            throw new BadRequestException("비공개 글에는 비밀번호가 필요합니다.");
        }
    }

    private void validateUpdateRequest(QnaUpdatePostRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new BadRequestException("제목을 입력하세요.");
        }
    }

    private void validateCreateReplyRequest(QnaCreateReplyRequest request) {
        if (request.content() == null || request.content().isBlank()) {
            throw new BadRequestException("답변 내용을 입력하세요.");
        }
    }

    private void validateReadAccess(QnaPost post, String password, boolean isAdmin) {
        if (post.isPublicPost() || isAdmin) {
            return;
        }
        if (password == null || !hash(password).equals(post.getPasswordHash())) {
            throw new ForbiddenException("비밀번호가 틀렸습니다.");
        }
    }

    private void validateEditableAccess(QnaPost post, String password, boolean isAdmin) {
        if (post.isPublicPost()) {
            throw new BadRequestException("비공개 문의만 수정 또는 삭제할 수 있습니다.");
        }
        if (post.isDeleted()) {
            throw new BadRequestException("삭제된 문의는 수정 또는 삭제할 수 없습니다.");
        }
        validateReadAccess(post, password, isAdmin);
    }

    private String normalizeContent(String content) {
        return content == null ? "" : content;
    }

    private void saveHistory(QnaPost post, String action) {
        historyRepo.save(new QnaPostHistory(post, action, post.getTitle(), post.getContent()));
    }
}
