import { useState, useEffect } from 'react'
import axios from 'axios'

const API = 'http://localhost:8080/api'

function Subscriptions() {
  const [subscriptions, setSubscriptions] = useState([])
  const [tenants, setTenants] = useState([])
  const [plans, setPlans] = useState([])
  const [form, setForm] = useState({ tenantId: '', planId: '', billingCycle: 'MONTHLY' })

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    const [subs, tenants, plans] = await Promise.all([
      axios.get(`${API}/subscriptions`),
      axios.get(`${API}/tenants`),
      axios.get(`${API}/plans`)
    ])
    setSubscriptions(subs.data)
    setTenants(tenants.data)
    setPlans(plans.data)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    await axios.post(`${API}/subscriptions`, form)
    setForm({ tenantId: '', planId: '', billingCycle: 'MONTHLY' })
    fetchData()
  }

  const handleCancel = async (id) => {
    if (window.confirm('구독을 취소하시겠습니까?')) {
      await axios.put(`${API}/subscriptions/${id}/cancel`)
      fetchData()
    }
  }

  const getTenantName = (id) => {
    const tenant = tenants.find(t => t.id === id)
    return tenant ? tenant.companyName : '-'
  }

  const getPlanName = (id) => {
    const plan = plans.find(p => p.id === id)
    return plan ? plan.name : '-'
  }

  return (
    <div>
      <h1>구독 관리</h1>

      <form onSubmit={handleSubmit} className="form-card">
        <select
          value={form.tenantId}
          onChange={(e) => setForm({...form, tenantId: e.target.value})}
          required
        >
          <option value="">고객사 선택</option>
          {tenants.map(t => (
            <option key={t.id} value={t.id}>{t.companyName}</option>
          ))}
        </select>
        <select
          value={form.planId}
          onChange={(e) => setForm({...form, planId: e.target.value})}
          required
        >
          <option value="">플랜 선택</option>
          {plans.map(p => (
            <option key={p.id} value={p.id}>{p.name}</option>
          ))}
        </select>
        <select
          value={form.billingCycle}
          onChange={(e) => setForm({...form, billingCycle: e.target.value})}
        >
          <option value="MONTHLY">월간</option>
          <option value="YEARLY">연간</option>
        </select>
        <button type="submit">구독 생성</button>
      </form>

      <table className="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>고객사</th>
            <th>플랜</th>
            <th>결제주기</th>
            <th>상태</th>
            <th>시작일</th>
            <th>다음결제일</th>
            <th>액션</th>
          </tr>
        </thead>
        <tbody>
          {subscriptions.map(sub => (
            <tr key={sub.id}>
              <td>{sub.id}</td>
              <td>{getTenantName(sub.tenant?.id)}</td>
              <td>{getPlanName(sub.plan?.id)}</td>
              <td>{sub.billingCycle === 'MONTHLY' ? '월간' : '연간'}</td>
              <td><span className={`status ${sub.status.toLowerCase()}`}>{sub.status}</span></td>
              <td>{new Date(sub.startDate).toLocaleDateString()}</td>
              <td>{sub.nextBillingDate ? new Date(sub.nextBillingDate).toLocaleDateString() : '-'}</td>
              <td>
                {sub.status === 'ACTIVE' && (
                  <button onClick={() => handleCancel(sub.id)} className="delete">취소</button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default Subscriptions