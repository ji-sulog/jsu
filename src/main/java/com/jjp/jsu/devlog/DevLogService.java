package com.jjp.jsu.devlog;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DevLogService {

    private final DevLogRepository repo;

    @Value("${chat.admin-password}")
    private String adminPassword;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public boolean isAdmin(String password) {
        return adminPassword.equals(password);
    }

    /* ── 목록 ──────────────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getList() {
        return repo.findAllByOrderByCreatedAtDesc().stream()
                .map(d -> Map.<String, Object>of(
                        "id",      d.getId(),
                        "title",   d.getTitle(),
                        "tags",    d.getTagList(),
                        "date",    d.getCreatedAt().format(FMT)
                ))
                .collect(Collectors.toList());
    }

    /* ── 상세 ──────────────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public Map<String, Object> getDetail(Long id) {
        DevLog d = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("로그를 찾을 수 없습니다."));
        return Map.of(
                "id",        d.getId(),
                "title",     d.getTitle(),
                "content",   d.getContent(),
                "tags",      d.getTagList(),
                "createdAt", d.getCreatedAt().format(FMT),
                "updatedAt", d.getUpdatedAt().format(FMT)
        );
    }

    /* ── 등록 ──────────────────────────────────────────────────────── */

    @Transactional
    public DevLog create(String title, String content, String tags) {
        return repo.save(new DevLog(title, content, tags));
    }

    /* ── 수정 ──────────────────────────────────────────────────────── */

    @Transactional
    public DevLog update(Long id, String title, String content, String tags) {
        DevLog d = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("로그를 찾을 수 없습니다."));
        d.update(title, content, tags);
        return repo.save(d);
    }

    /* ── 삭제 ──────────────────────────────────────────────────────── */

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
