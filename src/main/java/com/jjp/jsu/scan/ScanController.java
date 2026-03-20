package com.jjp.jsu.scan;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 하드코딩 / 고객사 특화 로직 탐지 컨트롤러.
 */
@Controller
public class ScanController {

    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    /** 스캔 도구 페이지 */
    @GetMapping("/scan")
    public String scan() {
        return "scan";
    }

    /** 스캔 API */
    @PostMapping("/api/scan")
    @ResponseBody
    public ResponseEntity<ScanResponse> doScan(@RequestBody ScanRequest request) {
        List<ScanItem> items = scanService.scan(request);

        int high   = (int) items.stream().filter(i -> "HIGH".equals(i.severity())).count();
        int medium = (int) items.stream().filter(i -> "MEDIUM".equals(i.severity())).count();
        int low    = (int) items.stream().filter(i -> "LOW".equals(i.severity())).count();
        int info   = (int) items.stream().filter(i -> "INFO".equals(i.severity())).count();

        ScanResponse response = new ScanResponse(items, items.size(), high, medium, low, info);
        return ResponseEntity.ok(response);
    }
}
