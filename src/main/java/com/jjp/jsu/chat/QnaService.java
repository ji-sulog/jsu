package com.jjp.jsu.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaPostRepository postRepo;
    private final QnaReplyRepository replyRepo;

    @Value("${chat.admin-password}")
    private String adminPassword;

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

    public boolean isAdmin(String password) {
        return adminPassword.equals(password);
    }

    /* ── 목록 ──────────────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPublicList() {
        // 비공개 글도 목록에 표시 (제목은 마스킹, 클릭 시 비번 요구)
        return postRepo.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllList() {
        return postRepo.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    private Map<String, Object> toSummary(QnaPost p) {
        return Map.of(
                "id",       p.getId(),
                "title",    p.isPublicPost() ? p.getTitle() : "🔒 비공개 문의",
                "status",   p.getStatus(),
                "isPublic", p.isPublicPost(),
                "date",     p.getCreatedAt().format(FMT)
        );
    }

    /* ── 글쓰기 ────────────────────────────────────────────────────── */

    @Transactional
    public QnaPost createPost(String title, String content, boolean publicPost, String password) {
        String hash = (password != null && !password.isBlank()) ? hash(password) : null;
        return postRepo.save(new QnaPost(title, content, publicPost, hash));
    }

    /* ── 상세 조회 ─────────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public Map<String, Object> getDetail(Long postId, String password, boolean admin) {
        QnaPost post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 비공개 글 접근 제어
        if (!post.isPublicPost() && !admin) {
            if (password == null || !hash(password).equals(post.getPasswordHash())) {
                return Map.of("error", "비밀번호가 틀렸습니다.");
            }
        }

        List<Map<String, Object>> replies = replyRepo.findByPostOrderByCreatedAtAsc(post)
                .stream()
                .map(r -> Map.<String, Object>of(
                        "id",      r.getId(),
                        "content", r.getContent(),
                        "date",    r.getCreatedAt().format(FMT)
                ))
                .collect(Collectors.toList());

        return Map.of(
                "id",       post.getId(),
                "title",    post.getTitle(),
                "content",  post.getContent(),
                "status",   post.getStatus(),
                "isPublic", post.isPublicPost(),
                "date",     post.getCreatedAt().format(FMT),
                "replies",  replies
        );
    }

    /* ── 답변 등록 (관리자 전용) ───────────────────────────────────── */

    @Transactional
    public QnaReply addReply(Long postId, String content) {
        QnaPost post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.markAnswered();
        postRepo.save(post);
        return replyRepo.save(new QnaReply(post, content));
    }
}
