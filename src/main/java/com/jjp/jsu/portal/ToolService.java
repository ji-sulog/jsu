package com.jjp.jsu.portal;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolService {

    private final ToolRepository toolRepository;

    public ToolService(ToolRepository toolRepository) {
        this.toolRepository = toolRepository;
    }

    public List<Tool> getAll() {
        return toolRepository.findAllByOrderBySortOrderAsc();
    }
}
