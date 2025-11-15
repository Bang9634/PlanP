window.APP_CONFIG = {
  // API 서버 주소 (포트별 환경 처리)
  API_BASE_URL: (() => {
    const hostname = window.location.hostname;
    const protocol = window.location.protocol;
    const port = window.location.port;
    
    // 로컬 개발환경 (3000번 포트)
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
      if (port === '3000') {
        return 'http://localhost:8080/api';
      }
    }
    
    // 프로덕션: 80번 포트 → 8080번 포트 API 호출
    return `${protocol}//${hostname}:8080/api`;
  })(),
  
  // 환경 정보
  FRONTEND_PORT: window.location.port || (window.location.protocol === 'https:' ? '443' : '80'),
  BACKEND_PORT: '8080',
  
  // 앱 메타정보
  APP_NAME: 'PlanP',
  APP_VERSION: '1.0.0',
  
  // 환경 감지
  IS_DEVELOPMENT: window.location.hostname === 'localhost' && window.location.port === '3000',
  IS_PRODUCTION: window.location.hostname !== 'localhost',
  
  // 디버그 모드
  DEBUG: window.location.hostname === 'localhost' || window.location.search.includes('debug=true'),
};

if (window.APP_CONFIG.DEBUG) {
  console.group('📋 PlanP 런타임 설정');
  console.log('환경:', window.APP_CONFIG.IS_DEVELOPMENT ? 'Development' : 'Production');
  console.log('프론트엔드 포트:', window.APP_CONFIG.FRONTEND_PORT);
  console.log('백엔드 포트:', window.APP_CONFIG.BACKEND_PORT);
  console.log('API URL:', window.APP_CONFIG.API_BASE_URL);
  console.groupEnd();
}