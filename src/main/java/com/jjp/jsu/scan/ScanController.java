package com.jjp.jsu.scan;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(ScanResponse.of(scanService.scan(request)));
    }
}
