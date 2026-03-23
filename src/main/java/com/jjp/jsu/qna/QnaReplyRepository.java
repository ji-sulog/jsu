package com.jjp.jsu.qna;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaReplyRepository extends JpaRepository<QnaReply, Long> {

    List<QnaReply> findByPostOrderByCreatedAtAsc(QnaPost post);
}
