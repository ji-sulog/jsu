/**
 * common.js — JSU 포털 공통 유틸리티
 *
 * 포함 기능:
 *  - escHtml(s)              : HTML 특수문자 이스케이프
 *  - setSpinner(show)        : id="spinner" 스피너 표시/숨김
 *  - showAlert(icon, title, msg) : 공통 알림 모달 표시
 *  - closeAlert()            : 공통 알림 모달 닫기
 *
 * 공통 알림 모달을 사용하려면 페이지에 아래 HTML이 있어야 합니다:
 *   <div id="alertOverlay"> ... </div>  (아래 createAlertModal() 자동 주입)
 */

/* ── HTML 이스케이프 ──────────────────────────────────────────────────── */

function escHtml(s) {
  if (!s) return '';
  return s.replace(/&/g, '&amp;')
          .replace(/</g, '&lt;')
          .replace(/>/g, '&gt;')
          .replace(/"/g, '&quot;');
}

/* ── 스피너 ──────────────────────────────────────────────────────────── */

function setSpinner(show) {
  const el = document.getElementById('spinner');
  if (!el) return;
  el.classList.toggle('show', show);
}

/* ── 공통 알림 모달 ──────────────────────────────────────────────────── */

(function createAlertModal() {
  if (document.getElementById('jsu-alert-overlay')) return;

  const overlay = document.createElement('div');
  overlay.id = 'jsu-alert-overlay';
  overlay.style.cssText =
    'display:none;position:fixed;inset:0;background:rgba(0,0,0,.45);' +
    'z-index:9999;align-items:center;justify-content:center;';

  overlay.innerHTML = `
    <div style="background:#fff;border-radius:16px;padding:28px 24px;width:320px;max-width:90vw;box-shadow:0 8px 32px rgba(0,0,0,.18);">
      <div style="font-size:1.8rem;text-align:center;margin-bottom:12px" id="jsu-alert-icon">ℹ️</div>
      <div style="font-weight:700;font-size:1rem;text-align:center;margin-bottom:8px" id="jsu-alert-title"></div>
      <div style="font-size:0.85rem;color:#64748b;text-align:center;line-height:1.6" id="jsu-alert-msg"></div>
      <button onclick="closeAlert()"
        style="width:100%;margin-top:20px;padding:10px;background:#0284c7;color:#fff;border:none;border-radius:8px;font-size:0.9rem;cursor:pointer;">확인</button>
    </div>`;

  overlay.addEventListener('click', function(e) {
    if (e.target === overlay) closeAlert();
  });

  document.body.appendChild(overlay);
})();

function showAlert(icon, title, msg) {
  document.getElementById('jsu-alert-icon').textContent  = icon  || 'ℹ️';
  document.getElementById('jsu-alert-title').textContent = title || '';
  document.getElementById('jsu-alert-msg').innerHTML     = msg   || '';
  const overlay = document.getElementById('jsu-alert-overlay');
  overlay.style.display = 'flex';
  document.body.style.overflow = 'hidden';
}

function closeAlert() {
  const overlay = document.getElementById('jsu-alert-overlay');
  overlay.style.display = 'none';
  document.body.style.overflow = '';
}
