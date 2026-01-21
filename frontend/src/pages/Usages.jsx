import { useState, useEffect } from 'react'
import axios from 'axios'

const API = 'http://localhost:8080/api'

function Usages() {
  const [usages, setUsages] = useState([])
  const [tenants, setTenants] = useState([])
  const [subscriptions, setSubscriptions] = useState([])
  const [plans, setPlans] = useState([])

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    const [usagesRes, tenantsRes, subsRes, plansRes] = await Promise.all([
      axios.get(`${API}/usages`),
      axios.get(`${API}/tenants`),
      axios.get(`${API}/subscriptions`),
      axios.get(`${API}/plans`)
    ])
    setUsages(usagesRes.data)
    setTenants(tenantsRes.data)
    setSubscriptions(subsRes.data)
    setPlans(plansRes.data)
  }

  const getTenantName = (id) => {
    const tenant = tenants.find(t => t.id === id)
    return tenant ? tenant.companyName : '-'
  }

  const getPlanLimit = (tenantId) => {
    const sub = subscriptions.find(s => s.tenant?.id === tenantId && s.status === 'ACTIVE')
    if (!sub) return null
    return plans.find(p => p.id === sub.plan?.id)
  }

  const simulateApiCall = async (tenantId) => {
    try {
      await axios.post(`${API}/usages/tenant/${tenantId}/api-call`)
      fetchData()
    } catch (error) {
      if (error.response?.status === 429) {
        alert('API 호출 한도를 초과했습니다!')
      }
    }
  }

  const getUsagePercent = (current, max) => {
    if (!max) return 0
    return Math.min((current / max) * 100, 100)
  }

  return (
    <div>
      <h1>사용량 관리</h1>

      <div className="usage-cards">
        {tenants.map(tenant => {
          const usage = usages.find(u => u.tenant?.id === tenant.id)
          const plan = getPlanLimit(tenant.id)
          const apiCalls = usage?.apiCalls || 0
          const maxCalls = plan?.maxApiCalls || 0
          const percent = getUsagePercent(apiCalls, maxCalls)

          return (
            <div key={tenant.id} className="usage-card">
              <h3>{tenant.companyName}</h3>
              <p className="plan-name">{plan ? plan.name : '구독 없음'}</p>
              
              <div className="usage-item">
                <div className="usage-label">
                  <span>API 호출</span>
                  <span>{apiCalls.toLocaleString()} / {maxCalls.toLocaleString()}</span>
                </div>
                <div className="usage-bar">
                  <div 
                    className={`usage-fill ${percent >= 90 ? 'danger' : percent >= 70 ? 'warning' : ''}`}
                    style={{ width: `${percent}%` }}
                  ></div>
                </div>
              </div>

              <div className="usage-item">
                <div className="usage-label">
                  <span>저장공간</span>
                  <span>{usage?.storageUsed || 0} MB / {plan?.maxStorage || 0} MB</span>
                </div>
                <div className="usage-bar">
                  <div 
                    className="usage-fill"
                    style={{ width: `${getUsagePercent(usage?.storageUsed || 0, plan?.maxStorage)}%` }}
                  ></div>
                </div>
              </div>

              <div className="usage-item">
                <div className="usage-label">
                  <span>사용자</span>
                  <span>{usage?.activeUsers || 0} / {plan?.maxUsers || 0}명</span>
                </div>
                <div className="usage-bar">
                  <div 
                    className="usage-fill"
                    style={{ width: `${getUsagePercent(usage?.activeUsers || 0, plan?.maxUsers)}%` }}
                  ></div>
                </div>
              </div>

              <button 
                className="simulate-btn"
                onClick={() => simulateApiCall(tenant.id)}
                disabled={!plan}
              >
                API 호출 시뮬레이션
              </button>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default Usages