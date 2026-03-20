UPDATE tool
SET sort_order = 6
WHERE name = '서버 로그 분석기'
  AND sort_order = 5;

INSERT INTO tool (name, href, status, sort_order, icon,
                  description, tags, since,
                  status_label, button_label, status_class)
SELECT 'API 스펙 점검기', NULL, 'COMING_SOON', 5, '🧾',
       'OpenAPI 문서나 요청/응답 예시를 바탕으로 상태코드, 필수값, 에러 응답 형식, 필드명 일관성을 점검합니다. 백엔드 API가 늘어날수록 스펙 품질을 빠르게 맞추는 데 도움이 되는 도구입니다.',
       'OpenAPI,응답 형식,상태코드,필수값,일관성 점검',
       NULL, '개발 예정', '개발 예정', 'soon'
WHERE NOT EXISTS (SELECT 1 FROM tool WHERE sort_order = 5)
  AND NOT EXISTS (SELECT 1 FROM tool WHERE name = 'API 스펙 점검기');
