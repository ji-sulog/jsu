package com.jjp.jsu.portal;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    /** 전체 공지 목록 (sort_order 오름차순) */
    public List<Notice> getAll() {
        return noticeRepository.findAllByOrderBySortOrderAsc();
    }

    /** 상단 배너에 표시할 pinned 공지 */
    public Optional<Notice> getPinned() {
        return noticeRepository.findFirstByPinnedTrueOrderBySortOrderAsc();
    }
}
