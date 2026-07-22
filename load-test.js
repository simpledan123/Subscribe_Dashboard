/**
 * k6 혜택 사용량 동시 증가 테스트
 *
 * TOKEN과 SUBSCRIPTION_ID를 지정한 뒤 실행한다.
 * TOKEN=... SUBSCRIPTION_ID=1 k6 run load-test.js
 */
import http from 'k6/http'
import { check, sleep } from 'k6'

export const options = {
  scenarios: {
    concurrent_usage: {
      executor: 'constant-vus',
      vus: 30,
      duration: '10s',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
}

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080'
const SUBSCRIPTION_ID = __ENV.SUBSCRIPTION_ID || '1'
const TOKEN = __ENV.TOKEN || ''

export default function () {
  const response = http.post(
    `${BASE_URL}/api/benefits/${SUBSCRIPTION_ID}/add`,
    JSON.stringify({ amount: 1 }),
    { headers: { Authorization: `Bearer ${TOKEN}`, 'Content-Type': 'application/json' } },
  )
  check(response, { 'usage increment succeeds': (r) => r.status === 200 })
  sleep(0.05)
}
