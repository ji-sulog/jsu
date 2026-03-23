package com.jjp.jsu.qna;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaPostHistoryRepository extends JpaRepository<QnaPostHistory, Long> {
    java.util.List<QnaPostHistory> findByPostOrderByChangedAtDesc(QnaPost post);
}
