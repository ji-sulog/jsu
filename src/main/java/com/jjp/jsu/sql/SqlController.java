package com.jjp.jsu.sql;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class SqlController {

    private final SqlQualityService sqlQualityService;

    public SqlController(SqlQualityService sqlQualityService) {
        this.sqlQualityService = sqlQualityService;
    }

    @GetMapping("/sql")
    public String sql() {
        return "sql";
    }

    /** SQL 텍스트 직접 검사 */
    @PostMapping("/api/sql/check")
    @ResponseBody
    public ResponseEntity<SqlCheckResponse> check(@RequestBody SqlCheckRequest request) {
        if (request.sql() == null || request.sql().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(sqlQualityService.check(request.sql(), request.customPatterns()));
    }

    /** .sql 파일 업로드 → 텍스트 추출 */
    @PostMapping("/api/sql/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        String filename = file.getOriginalFilename();

        if (filename == null || !filename.toLowerCase().endsWith(".sql")) {
            result.put("error", ".sql 파일만 업로드할 수 있습니다.");
            return ResponseEntity.badRequest().body(result);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String text = reader.lines().collect(Collectors.joining("\n"));
            result.put("text", text);
            result.put("filename", filename);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", "파일 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
