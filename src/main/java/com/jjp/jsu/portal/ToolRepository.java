package com.jjp.jsu.portal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToolRepository extends JpaRepository<Tool, Long> {

    List<Tool> findAllByOrderBySortOrderAsc();
}
