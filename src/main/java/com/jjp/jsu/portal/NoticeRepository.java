package com.jjp.jsu.portal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findAllByOrderBySortOrderAsc();
    Optional<Notice> findFirstByPinnedTrueOrderBySortOrderAsc();
}
