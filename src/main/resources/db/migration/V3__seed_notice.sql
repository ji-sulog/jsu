-- ── notice 초기 데이터 ────────────────────────────────────────────────────────

INSERT INTO notice (type, title, body, display_date, sort_order, pinned)
SELECT 'UPDATE', '요구사항 비교 도구 신규 오픈',
       '이전/최신 요구사항 문서를 붙여넣거나 파일로 업로드하면 변경 항목을 자동 감지하고 규칙 엔진(R01~R10)으로 중요도를 추천합니다. .txt · .md · .docx · .pptx · .pdf 파일 업로드를 지원합니다.',
       '2026.03', 1, false
WHERE NOT EXISTS (SELECT 1 FROM notice WHERE sort_order = 1);

INSERT INTO notice (type, title, body, display_date, sort_order, pinned)
SELECT 'UPDATE', '하드코딩 탐지 도구 신규 오픈',
       '소스코드의 하드코딩 값·고객사 특화 분기·설정화 후보를 자동으로 스캔하는 도구가 추가되었습니다. Java · JS/TS · SQL · Config · 텍스트 형식을 지원하며, 커스텀 키워드 등록이 가능합니다.',
       '2026.03', 2, true
WHERE NOT EXISTS (SELECT 1 FROM notice WHERE sort_order = 2);

INSERT INTO notice (type, title, body, display_date, sort_order, pinned)
SELECT 'UPDATE', '[요구사항 비교] 부정어 반전 감지 및 삭제 가중치 규칙 강화 — R07·R08 개선 적용',
       '요구사항 비교 도구의 R07 규칙은 "없음", "불가", "금지" 등의 부정어 포함 여부를 감지해 방향 반전 시 중요도를 HIGH로 올립니다. R08은 삭제된 항목에 추가 가중치를 부여해 누락 리스크를 강조합니다.',
       '2026.03', 3, false
WHERE NOT EXISTS (SELECT 1 FROM notice WHERE sort_order = 3);

INSERT INTO notice (type, title, body, display_date, sort_order, pinned)
SELECT 'UPDATE', '[요구사항 비교 · 하드코딩 탐지] 파일 업로드 지원 (.txt .md .docx .pptx .pdf 텍스트형) — 스캔 PDF 자동 감지',
       '두 도구 모두 파일 업로드를 지원합니다. .txt · .md 는 직접 텍스트 추출, .docx · .pptx 는 Apache POI로 본문을 추출합니다. PDF는 PDFBox로 텍스트 레이어를 읽으며, 스캔된 PDF(텍스트 레이어 없음)는 자동으로 감지하여 안내 메시지를 표시합니다.',
       '2026.03', 4, false
WHERE NOT EXISTS (SELECT 1 FROM notice WHERE sort_order = 4);

INSERT INTO notice (type, title, body, display_date, sort_order, pinned)
SELECT 'GUIDE', '규칙 엔진 R01~R10 전체 설명이 도구 내 ''사용 가이드'' 팝업에서 확인 가능합니다',
       '요구사항 비교 도구 화면 우측 상단의 [사용 가이드] 버튼을 클릭하면 R01(단순 문구 수정)부터 R10(항목 이동·재배치)까지 각 규칙의 판정 기준과 예시를 확인할 수 있습니다.',
       '2026.03', 5, false
WHERE NOT EXISTS (SELECT 1 FROM notice WHERE sort_order = 5);

INSERT INTO notice (type, title, body, display_date, sort_order, pinned)
SELECT 'GUIDE', '하드코딩 탐지 도구 사용 가이드 — HC·CL·WT 규칙 및 커스텀 키워드 등록 방법',
       '하드코딩 탐지 도구 화면 우측 상단의 [사용 가이드] 버튼에서 HC(하드코딩) 8개, CL(고객사 특화) 8개, WT(가중치) 5개 규칙의 판정 기준과 커스텀 키워드 등록 방법을 확인할 수 있습니다.',
       '2026.03', 6, false
WHERE NOT EXISTS (SELECT 1 FROM notice WHERE sort_order = 6);

INSERT INTO notice (type, title, body, display_date, sort_order, pinned)
SELECT 'UPDATE', '[Q&A] 비공개 문의 수정/삭제 기능 추가',
       '비공개 문의는 작성자 비밀번호로 수정·삭제할 수 있도록 개선되었습니다. 수정 시 이전 제목과 내용은 이력으로 보관되며, 삭제는 실제 제거 대신 논리 삭제 방식으로 처리됩니다. 또한 문의 등록 시 내용은 비어 있어도 저장할 수 있도록 변경했습니다.',
       '2026.03', 7, false
WHERE NOT EXISTS (SELECT 1 FROM notice WHERE sort_order = 7);

INSERT INTO notice (type, title, body, display_date, sort_order, pinned)
SELECT 'UPDATE', '[신규] 네이밍 컨벤션 검사 도구 오픈',
       '타입·메서드·변수명이 PascalCase/camelCase/UPPER_SNAKE_CASE 규칙을 따르는지 자동으로 검사합니다. '
       || '의미 품질 규칙(boolean 접두사, 컬렉션 이름, 축약어 과다)까지 함께 점검하여 리뷰 전 빠르게 코드 품질을 확인할 수 있습니다.',
       '2026.03', 8, false
WHERE NOT EXISTS (SELECT 1 FROM notice WHERE sort_order = 8);
