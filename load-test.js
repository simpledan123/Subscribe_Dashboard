/**
 * k6 부하테스트 스크립트 - API 호출 한도 동시성 검증
 *
 * 목적:
 *   - 개선 전(DB only): 동시 요청 시 한도 초과 케이스가 중복 통과되는지 확인
 *   - 개선 후(Redis INCR): 동일 조건에서 정확히 한도만큼만 통과되는지 검증
 *
 * 실행 방법:
 *   k6 run load-test.js
 *
 * 설치:
 *   brew install k6 (macOS)
 *   또는 https://k6.io/docs/getting-started/installation/
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate } from 'k6/metrics';

// 커스텀 메트릭
const successCount = new Counter('success_count');
const blockedCount = new Counter('blocked_count');
const overLimitRate = new Rate('over_limit_rate');

export const options = {
  scenarios: {
    // 시나리오 1: 점진적 부하 (응답시간 측정용)
    ramp_up: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 10 },   // 10초 동안 10명으로
        { duration: '20s', target: 50 },   // 20초 동안 50명으로
        { duration: '10s', target: 0 },    // 10초 동안 0명으로
      ],
      gracefulRampDown: '5s',
      tags: { scenario: 'ramp_up' },
    },

    // 시나리오 2: 동시성 폭발 (race condition 검증 핵심)
    spike: {
      executor: 'constant-vus',
      vus: 100,           // 100명이 동시에
      duration: '5s',     // 5초 동안 집중 요청
      startTime: '45s',   // ramp_up 끝난 후 실행
      tags: { scenario: 'spike' },
    },
  },

  thresholds: {
    http_req_duration: ['p(95)<500'],   // 95%ile 응답시간 500ms 이하
    http_req_failed: ['rate<0.05'],     // 에러율 5% 미만 (429 제외)
  },
};

// 테스트 전 설정 (테넌트 ID와 JWT 토큰은 환경에 맞게 수정)
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TENANT_ID = __ENV.TENANT_ID || '1';
const TOKEN = __ENV.TOKEN || 'your-jwt-token-here';

export default function () {
  const url = `${BASE_URL}/api/usages/tenant/${TENANT_ID}/api-call`;
  const params = {
    headers: {
      'Authorization': `Bearer ${TOKEN}`,
      'Content-Type': 'application/json',
    },
    timeout: '5s',
  };

  const res = http.post(url, null, params);

  // 200: 정상 카운트, 429: 한도 초과 차단
  const isSuccess = check(res, {
    'status is 200 or 429': (r) => r.status === 200 || r.status === 429,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });

  if (res.status === 200) {
    successCount.add(1);
  } else if (res.status === 429) {
    blockedCount.add(1);
    overLimitRate.add(1);
  } else {
    overLimitRate.add(0);
  }

  sleep(0.01); // 10ms 간격
}

export function handleSummary(data) {
  const total = data.metrics.success_count
    ? data.metrics.success_count.values.count
    : 0;
  const blocked = data.metrics.blocked_count
    ? data.metrics.blocked_count.values.count
    : 0;

  console.log('\n========== 테스트 결과 요약 ==========');
  console.log(`총 성공 요청: ${total}`);
  console.log(`한도 초과 차단: ${blocked}`);
  console.log(`p95 응답시간: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
  console.log(`p99 응답시간: ${data.metrics.http_req_duration.values['p(99)'].toFixed(2)}ms`);
  console.log('======================================\n');

  return {
    'test-results/load-test-summary.json': JSON.stringify(data, null, 2),
  };
}
