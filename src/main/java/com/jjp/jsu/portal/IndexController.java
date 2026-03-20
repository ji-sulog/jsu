package com.jjp.jsu.portal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 메인 대시보드(/)를 담당하는 컨트롤러.
 * Tool, Notice 데이터를 모델에 담아 index.html에 전달합니다.
 */
@Controller
public class IndexController {

    private final ToolService toolService;
    private final NoticeService noticeService;

    public IndexController(ToolService toolService, NoticeService noticeService) {
        this.toolService = toolService;
        this.noticeService = noticeService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Tool> tools = toolService.getAll();

        long activeCount     = tools.stream().filter(t -> "ACTIVE".equals(t.getStatus())).count();
        long comingSoonCount = tools.stream().filter(t -> "COMING_SOON".equals(t.getStatus())).count();

        model.addAttribute("tools",          tools);
        model.addAttribute("activeCount",    activeCount);
        model.addAttribute("comingSoonCount", comingSoonCount);

        List<Notice> notices = noticeService.getAll();
        model.addAttribute("notices", notices);
        noticeService.getPinned().ifPresent(n -> model.addAttribute("pinnedNotice", n));

        return "index";
    }
}
