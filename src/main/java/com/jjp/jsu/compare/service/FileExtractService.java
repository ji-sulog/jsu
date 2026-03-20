package com.jjp.jsu.compare.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 파일 형식별 텍스트 추출 서비스.
 * <p>
 * 지원 형식: .txt, .md, .docx, .pptx, .pdf(텍스트형)
 * PDF 스캔 감지: 추출된 텍스트의 의미있는 글자 수가 50자 미만이면 스캔 PDF로 판단
 */
@Service
public class FileExtractService {

    /** 지원하는 파일 확장자 목록 */
    public static final java.util.Set<String> SUPPORTED_EXTENSIONS =
            java.util.Set.of(".txt", ".md", ".docx", ".pptx", ".pdf");

    /**
     * 텍스트 추출 결과.
     *
     * @param text         추출된 텍스트 (스캔 감지 시 빈 문자열)
     * @param scanDetected true = PDF이지만 텍스트 레이어 없음(스캔 문서로 추정)
     */
    public record ExtractResult(String text, boolean scanDetected) {}

    /**
     * MultipartFile로부터 텍스트를 추출합니다.
     *
     * @throws IllegalArgumentException 지원하지 않는 파일 형식
     * @throws IOException              파일 읽기/파싱 실패
     */
    public ExtractResult extract(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase()
                : "";

        if (filename.endsWith(".txt") || filename.endsWith(".md")) {
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);
            return new ExtractResult(text, false);
        } else if (filename.endsWith(".docx")) {
            return extractDocx(file.getInputStream());
        } else if (filename.endsWith(".pptx")) {
            return extractPptx(file.getInputStream());
        } else if (filename.endsWith(".pdf")) {
            return extractPdf(file.getInputStream());
        } else {
            String ext = filename.contains(".")
                    ? filename.substring(filename.lastIndexOf('.'))
                    : filename;
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다: " + ext);
        }
    }

    // ── .docx ────────────────────────────────────────────────────────────────

    private ExtractResult extractDocx(InputStream is) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(is)) {
            String text = doc.getParagraphs().stream()
                    .map(XWPFParagraph::getText)
                    .filter(t -> t != null && !t.isBlank())
                    .collect(Collectors.joining("\n"));
            return new ExtractResult(text, false);
        }
    }

    // ── .pptx ────────────────────────────────────────────────────────────────

    private ExtractResult extractPptx(InputStream is) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow(is)) {
            StringBuilder sb = new StringBuilder();
            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        String text = textShape.getText();
                        if (text != null && !text.isBlank()) {
                            sb.append(text.trim()).append("\n");
                        }
                    }
                }
            }
            return new ExtractResult(sb.toString().trim(), false);
        }
    }

    // ── .pdf ─────────────────────────────────────────────────────────────────

    private ExtractResult extractPdf(InputStream is) throws IOException {
        try (PDDocument doc = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);

            // 의미있는 글자(공백 제외) 50자 미만이면 스캔 PDF로 판단
            int meaningfulChars = text.replaceAll("\\s", "").length();
            if (meaningfulChars < 50) {
                return new ExtractResult("", true);
            }
            return new ExtractResult(text.trim(), false);
        }
    }
}
