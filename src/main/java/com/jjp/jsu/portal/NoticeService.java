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

    /** 전체 공지 목록 (최신순) */
    public List<Notice> getAll() {
        return noticeRepository.findAllByOrderBySortOrderDesc();
    }

    /** 상단 배너에 표시할 최신 공지 */
    public Optional<Notice> getPinned() {
        return noticeRepository.findFirstByOrderBySortOrderDesc();
    }
}
