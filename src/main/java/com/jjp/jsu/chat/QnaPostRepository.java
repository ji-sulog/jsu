package com.jjp.jsu.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaPostRepository extends JpaRepository<QnaPost, Long> {

    /** 공개 글만 최신순 */
    List<QnaPost> findByPublicPostTrueOrderByCreatedAtDesc();

    /** 전체 글 최신순 (관리자용) */
    List<QnaPost> findAllByOrderByCreatedAtDesc();
}
