package com.jjp.jsu.devlog;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DevLogService {

    private final DevLogRepository repo;

    @Value("${chat.admin-password}")
    private String adminPassword;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public boolean isAdmin(String password) {
        return adminPassword.equals(password);
    }

    public void validateAdmin(String password) {
        if (!isAdmin(password)) {
            throw new DevLogAccessDeniedException("관리자 권한이 없습니다.");
        }
    }

    /* ── 목록 ──────────────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<DevLogSummaryResponse> getList() {
        return repo.findAllByOrderByCreatedAtDesc().stream()
                .map(d -> new DevLogSummaryResponse(
                        d.getId(),
                        d.getTitle(),
                        d.getTagList(),
                        d.getCreatedAt().format(FMT)
                ))
                .toList();
    }

    /* ── 상세 ──────────────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public DevLogDetailResponse getDetail(Long id) {
        DevLog d = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("로그를 찾을 수 없습니다."));

        return new DevLogDetailResponse(
                d.getId(),
                d.getTitle(),
                d.getContent(),
                d.getTagList(),
                d.getCreatedAt().format(FMT),
                d.getUpdatedAt().format(FMT)
        );
    }

    /* ── 등록 ──────────────────────────────────────────────────────── */

    @Transactional
    public DevLogIdResponse create(String adminPassword, DevLogUpsertRequest request) {
        validateAdmin(adminPassword);
        validateRequest(request);

        DevLog log = repo.save(new DevLog(
                request.title(),
                request.content(),
                normalizeTags(request.tags())
        ));

        return new DevLogIdResponse(log.getId(), "OK");
    }

    /* ── 수정 ──────────────────────────────────────────────────────── */

    @Transactional
    public DevLogStatusResponse update(Long id, String adminPassword, DevLogUpsertRequest request) {
        validateAdmin(adminPassword);
        validateRequest(request);

        DevLog d = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("로그를 찾을 수 없습니다."));

        d.update(request.title(), request.content(), normalizeTags(request.tags()));
        repo.save(d);
        return new DevLogStatusResponse("OK");
    }

    /* ── 삭제 ──────────────────────────────────────────────────────── */

    @Transactional
    public DevLogStatusResponse delete(Long id, String adminPassword) {
        validateAdmin(adminPassword);

        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("로그를 찾을 수 없습니다.");
        }

        repo.deleteById(id);
        return new DevLogStatusResponse("OK");
    }

    private void validateRequest(DevLogUpsertRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new DevLogBadRequestException("제목을 입력하세요.");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new DevLogBadRequestException("내용을 입력하세요.");
        }
    }

    private String normalizeTags(String tags) {
        return tags == null ? "" : tags;
    }
}
