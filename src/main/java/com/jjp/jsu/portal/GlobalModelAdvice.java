package com.jjp.jsu.portal;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 모든 Thymeleaf 뷰에 공통 모델 데이터를 주입합니다.
 * sidebarTools: 사이드바 도구 목록 (Tool 엔티티 리스트, sort_order 오름차순)
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final ToolService toolService;

    public GlobalModelAdvice(ToolService toolService) {
        this.toolService = toolService;
    }

    @ModelAttribute
    public void addSidebarTools(Model model) {
        model.addAttribute("sidebarTools", toolService.getAll());
    }
}
