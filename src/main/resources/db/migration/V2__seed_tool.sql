-- ── tool 초기 데이터 (최종 상태 기준) ────────────────────────────────────────
-- sort_order / icon_type 기준으로 중복 방지

INSERT INTO tool (name, href, status, sort_order, icon, icon_type,
                  description, tags, since,
                  status_label, button_label, status_class)
SELECT '요구사항 비교', '/compare', 'ACTIVE', 1, '📋', 'compare',
       '이전/최신 요구사항 문서를 붙여넣거나 파일로 업로드하면, 변경된 항목을 자동으로 감지하고 규칙 엔진(R01~R10)으로 중요도를 추천합니다. 단순 문구 수정부터 권한·정책 반전까지 10가지 규칙을 적용합니다.',
       'LCS Diff,규칙 엔진,파일 업로드,삭제 가중치,부정어 반전',
       '2026.03', '● 운영 중', '도구 열기', 'live'
WHERE NOT EXISTS (SELECT 1 FROM tool WHERE sort_order = 1);

INSERT INTO tool (name, href, status, sort_order, icon, icon_type,
                  description, tags, since,
                  status_label, button_label, status_class)
SELECT '하드코딩 탐지', '/scan', 'ACTIVE', 2, '🔍', 'scan',
       '소스코드를 붙여넣거나 파일로 업로드하면 하드코딩 값, 고객사 특화 분기, 설정화 후보를 자동으로 스캔합니다. HC·CL·WT 3개 카테고리 16+5 규칙을 적용하며, 사용자 정의 키워드로 고객사 코드를 직접 등록할 수 있습니다.',
       'HC 규칙 8개,CL 규칙 8개,WT 가중치 5개,커스텀 키워드,Java / JS / SQL',
       '2026.03', '● 운영 중', '도구 열기', 'live'
WHERE NOT EXISTS (SELECT 1 FROM tool WHERE sort_order = 2);

INSERT INTO tool (name, href, status, sort_order, icon, icon_type,
                  description, tags, since,
                  status_label, button_label, status_class)
SELECT 'SQL 품질 검사', '/sql', 'ACTIVE', 3, '🗄️', 'sql',
       'SQL 쿼리를 붙여넣으면 N+1 유발 패턴, SELECT *, 인덱스 없는 LIKE 검색, 페이지네이션 누락 등 품질 문제를 규칙 기반으로 탐지합니다. 팀 금지 패턴을 커스텀으로 등록할 수 있습니다.',
       '규칙 기반,커스텀 패턴,MySQL / Oracle',
       NULL, '● 운영 중', '도구 열기', 'live'
WHERE NOT EXISTS (SELECT 1 FROM tool WHERE sort_order = 3);

INSERT INTO tool (name, href, status, sort_order, icon, icon_type,
                  description, tags, since,
                  status_label, button_label, status_class)
SELECT '네이밍 컨벤션 검사', '/naming', 'ACTIVE', 4, '✏️', 'naming',
       '소스코드의 클래스·메서드·변수명이 컨벤션을 따르는지 검사합니다. PascalCase, camelCase, UPPER_SNAKE_CASE 등 기본 규칙과 팀 자체 접두사·접미사 규칙을 직접 등록해 적용할 수 있습니다.',
       'Java / JS,팀 규칙 등록,자동 수정 제안',
       '2026.03', '● 운영 중', '도구 열기', 'live'
WHERE NOT EXISTS (SELECT 1 FROM tool WHERE sort_order = 4);

INSERT INTO tool (name, href, status, sort_order, icon, icon_type,
                  description, tags, since,
                  status_label, button_label, status_class)
SELECT 'API 스펙 점검기', NULL, 'COMING_SOON', 5, '🧾', 'api-spec',
       'OpenAPI 문서나 요청/응답 예시를 바탕으로 상태코드, 필수값, 에러 응답 형식, 필드명 일관성을 점검합니다. 백엔드 API가 늘어날수록 스펙 품질을 빠르게 맞추는 데 도움이 되는 도구입니다.',
       'OpenAPI,응답 형식,상태코드,필수값,일관성 점검',
       NULL, '개발 예정', '개발 예정', 'soon'
WHERE NOT EXISTS (SELECT 1 FROM tool WHERE sort_order = 5);

INSERT INTO tool (name, href, status, sort_order, icon, icon_type,
                  description, tags, since,
                  status_label, button_label, status_class)
SELECT '서버 로그 분석기', NULL, 'COMING_SOON', 6, '🖥️', 'log-analyzer',
       '에러 로그를 붙여넣으면 AI가 에러 유형을 분류하고 원인을 한글로 해설합니다. 반복 패턴 그루핑, 시간대별 빈도 분석, 해결 방향 제안까지 제공합니다. Spring AI 연동 후 오픈 예정입니다.',
       'Spring AI,한글 해설,패턴 그루핑',
       NULL, 'AI 연동 예정', 'AI 연동 후 오픈', 'planned'
WHERE NOT EXISTS (SELECT 1 FROM tool WHERE sort_order = 6);
