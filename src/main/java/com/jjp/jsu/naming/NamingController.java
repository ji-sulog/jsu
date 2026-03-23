package com.jjp.jsu.naming;

import com.jjp.jsu.common.FileExtractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/**
 * 네이밍 컨벤션 검사 컨트롤러.
 */
@Controller
@RequiredArgsConstructor
public class NamingController {

    /**
     * 네이밍 검사에서 허용하는 파일 형식.
     * 소스 코드(텍스트) 기반 검사이므로 .java, .txt만 허용합니다.
     * docx/pptx/pdf는 의미 있는 코드 텍스트가 보장되지 않아 제외합니다.
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".java", ".txt");

    private final NamingService namingService;
    private final FileExtractService fileExtractService;

    /** 페이지 렌더링 */
    @GetMapping("/naming")
    public String page() {
        return "naming";
    }

    /** POST /api/naming/check — 코드 검사 */
    @PostMapping("/api/naming/check")
    @ResponseBody
    public ResponseEntity<NamingCheckResponse> check(
            @RequestBody NamingCheckRequest request) {
        return ResponseEntity.ok(namingService.check(request));
    }

    /** POST /api/naming/upload — 파일 업로드 (.java, .txt만 허용) */
    @PostMapping("/api/naming/upload")
    @ResponseBody
    public ResponseEntity<NamingUploadResponse> upload(
            @RequestParam("file") MultipartFile file) {
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String ext  = name.contains(".") ? name.substring(name.lastIndexOf('.')).toLowerCase() : "";

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return ResponseEntity.ok(new NamingUploadResponse(
                    null, null, "지원하지 않는 형식입니다. .java 또는 .txt 파일만 업로드할 수 있습니다."));
        }

        try {
            FileExtractService.ExtractResult result = fileExtractService.extract(file);
            return ResponseEntity.ok(new NamingUploadResponse(result.text(), name, null));
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.ok(new NamingUploadResponse(
                    null, null, "파일 읽기에 실패했습니다: " + e.getMessage()));
        }
    }
}
