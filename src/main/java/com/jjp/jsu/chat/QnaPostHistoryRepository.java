package com.jjp.jsu.chat;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaPostHistoryRepository extends JpaRepository<QnaPostHistory, Long> {
    java.util.List<QnaPostHistory> findByPostOrderByChangedAtDesc(QnaPost post);
}
