-- ── 초기 도구 데이터 ──────────────────────────────────────────────────────────
INSERT INTO tool (name, href, status, sort_order, icon,
                  description, tags, since,
                  status_label, button_label, status_class)
SELECT '요구사항 비교', '/compare', 'ACTIVE', 1, '📋',
       '이전/최신 요구사항 문서를 붙여넣거나 파일로 업로드하면, 변경된 항목을 자동으로 감지하고 규칙 엔진(R01~R10)으로 중요도를 추천합니다. 단순 문구 수정부터 권한·정책 반전까지 10가지 규칙을 적용합니다.',
       'LCS Diff,규칙 엔진,파일 업로드,삭제 가중치,부정어 반전',
       '2026.03', '● 운영 중', '도구 열기', 'live'
WHERE NOT EXISTS (SELECT 1 FROM tool WHERE sort_order = 1);

INSERT INTO tool (name, href, status, sort_order, icon,
                  description, tags, since,
                  status_label, button_label, status_class)
SELECT '하드코딩 탐지', '/scan', 'ACTIVE', 2, '🔍',
       '소스코드를 붙여넣거나 파일로 업로드하면 하드코딩 값, 고객사 특화 분기, 설정화 후보를 자동으로 스캔합니다. HC·CL·WT 3개 카테고리 16+5 규칙을 적용하며, 사용자 정의 키워드로 고객사 코드를 직접 등록할 수 있습니다.',
       'HC 규칙 8개,CL 규칙 8개,WT 가중치 5개,커스텀 키워드,Java / JS / SQL',
       '2026.03', '● 운영 중', '도구 열기', 'live'
WHERE NOT EXISTS (SELECT 1 FROM tool WHERE sort_order = 2);

INSERT INTO tool (name, href, status, sort_order, icon,
                  description, tags, since,
                  status_label, button_label, status_class)
SELECT 'SQL 품질 검사', '/sql', 'ACTIVE', 3, '🗄️',
       'SQL 쿼리를 붙여넣으면 N+1 유발 패턴, SELECT *, 인덱스 없는 LIKE 검색, 페이지네이션 누락 등 품질 문제를 규칙 기반으로 탐지합니다. 팀 금지 패턴을 커스텀으로 등록할 수 있습니다.',
       '규칙 기반,커스텀 패턴,MySQL / Oracle',
       NULL, '● 운영 중', '도구 열기', 'live'
WHERE NOT EXISTS (SELECT 1 FROM tool WHERE sort_order = 3);

INSERT INTO tool (name, href, status, sort_order, icon,
                  description, tags, since,
                  status_label, button_label, status_class)
SELECT '네이밍 컨벤션 검사', NULL, 'COMING_SOON', 4, '✏️',
       '소스코드의 클래스·메서드·변수명이 컨벤션을 따르는지 검사합니다. PascalCase, camelCase, UPPER_SNAKE_CASE 등 기본 규칙과 팀 자체 접두사·접미사 규칙을 직접 등록해 적용할 수 있습니다.',
       'Java / JS,팀 규칙 등록,자동 수정 제안',
       NULL, '준비 중', '개발 예정', 'soon'
WHERE NOT EXISTS (SELECT 1 FROM tool WHERE sort_order = 4);

INSERT INTO tool (name, href, status, sort_order, icon,
                  description, tags, since,
                  status_label, button_label, status_class)
SELECT '서버 로그 분석기', NULL, 'COMING_SOON', 5, '🖥️',
       '에러 로그를 붙여넣으면 AI가 에러 유형을 분류하고 원인을 한글로 해설합니다. 반복 패턴 그루핑, 시간대별 빈도 분석, 해결 방향 제안까지 제공합니다. Spring AI 연동 후 오픈 예정입니다.',
       'Spring AI,한글 해설,패턴 그루핑',
       NULL, 'AI 연동 예정', 'AI 연동 후 오픈', 'planned'
WHERE NOT EXISTS (SELECT 1 FROM tool WHERE sort_order = 5);

-- ── 초기 공지 데이터 ──────────────────────────────────────────────────────────
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
