package com.jjp.jsu.portal;

import com.jjp.jsu.common.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /** 전체 공지 목록 (sort_order 내림차순) */
    public List<Notice> getAll() {
        return noticeRepository.findAllByOrderBySortOrderDesc();
    }

    /** 상단 배너에 표시할 최신 공지 */
    public Optional<Notice> getPinned() {
        return noticeRepository.findFirstByOrderBySortOrderDesc();
    }

    /* ── 관리자 IDU ─────────────────────────────────────────────── */

    @Transactional
    public Notice create(NoticeRequest request) {
        validate(request);
        return noticeRepository.save(new Notice(
                request.type(),
                request.title(),
                request.body(),
                request.displayDate(),
                request.sortOrder() != null ? request.sortOrder() : nextSortOrder(),
                request.pinned()
        ));
    }

    @Transactional
    public Notice update(Long id, NoticeRequest request) {
        validate(request);
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지를 찾을 수 없습니다."));
        notice.update(
                request.type(),
                request.title(),
                request.body(),
                request.displayDate(),
                request.sortOrder() != null ? request.sortOrder() : notice.getSortOrder(),
                request.pinned()
        );
        return noticeRepository.save(notice);
    }

    @Transactional
    public void delete(Long id) {
        if (!noticeRepository.existsById(id)) {
            throw new IllegalArgumentException("공지를 찾을 수 없습니다.");
        }
        noticeRepository.deleteById(id);
    }

    private void validate(NoticeRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new BadRequestException("제목을 입력하세요.");
        }
        if (request.type() == null || request.type().isBlank()) {
            throw new BadRequestException("타입을 선택하세요.");
        }
    }

    private int nextSortOrder() {
        return noticeRepository.findAllByOrderBySortOrderDesc()
                .stream()
                .mapToInt(Notice::getSortOrder)
                .max()
                .orElse(0) + 1;
    }
}
