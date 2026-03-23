package com.jjp.jsu.compare;

import com.jjp.jsu.common.FileExtractService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CompareController {

    private final DiffService diffService;
    private final FileExtractService fileExtractService;

    public CompareController(DiffService diffService,
                             FileExtractService fileExtractService) {
        this.diffService = diffService;
        this.fileExtractService = fileExtractService;
    }

    /** 요구사항 비교 도구 */
    @GetMapping("/compare")
    public String compare() {
        return "compare";
    }

    /**
     * 요구사항 비교 API.
     * POST /api/compare  →  { "oldText": "...", "newText": "..." }
     */
    @PostMapping("/api/compare")
    @ResponseBody
    public CompareResponse compare(@RequestBody CompareRequest request) {
        List<ChangeItem> changes = diffService.diff(request.oldText(), request.newText());

        long high    = changes.stream().filter(c -> "HIGH".equals(c.priority())).count();
        long medium  = changes.stream().filter(c -> "MEDIUM".equals(c.priority())).count();
        long low     = changes.stream().filter(c -> "LOW".equals(c.priority())).count();
        long general = changes.stream().filter(c -> "GENERAL".equals(c.priority())).count();

        return new CompareResponse(changes, changes.size(),
                (int) high, (int) medium, (int) low, (int) general);
    }

    /**
     * 파일 업로드 → 텍스트 추출 API.
     * POST /api/upload  (multipart/form-data, file 파라미터)
     *
     * 성공 응답: { "text": "...", "scanDetected": false, "filename": "..." }
     * 오류 응답: { "error": "...", "errorType": "UNSUPPORTED_FORMAT" | "PROCESSING_ERROR" | "SCAN_PDF" }
     */
    @PostMapping("/api/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> result = new HashMap<>();

        try {
            FileExtractService.ExtractResult extracted = fileExtractService.extract(file);

            if (extracted.scanDetected()) {
                result.put("errorType", "SCAN_PDF");
                result.put("error", "스캔된 PDF로 보입니다. 텍스트 레이어가 없어 내용을 추출할 수 없습니다.");
                result.put("filename", file.getOriginalFilename());
                return ResponseEntity.ok(result);
            }

            result.put("text", extracted.text());
            result.put("scanDetected", false);
            result.put("filename", file.getOriginalFilename());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            result.put("errorType", "UNSUPPORTED_FORMAT");
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);

        } catch (Exception e) {
            result.put("errorType", "PROCESSING_ERROR");
            result.put("error", "파일 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
