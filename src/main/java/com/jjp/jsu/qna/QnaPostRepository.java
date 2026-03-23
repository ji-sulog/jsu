package com.jjp.jsu.qna;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaPostRepository extends JpaRepository<QnaPost, Long> {

    /** 공개/비공개/삭제 포함 전체 최신순 */
    List<QnaPost> findAllByOrderByCreatedAtDesc();
}
