package com.jjp.jsu.devlog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DevLogRepository extends JpaRepository<DevLog, Long> {

    List<DevLog> findAllByOrderByCreatedAtDesc();
}
