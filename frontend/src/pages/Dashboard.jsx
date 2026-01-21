import { useState, useEffect } from 'react'
import axios from 'axios'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, LineChart, Line, Legend
} from 'recharts'

const API = 'http://localhost:8080/api'

const COLORS = ['#4a90d9', '#27ae60', '#e74c3c', '#f39c12', '#9b59b6']

function Dashboard() {
  const [stats, setStats] = useState({
    tenants: 0,
    subscriptions: 0,
    invoices: 0,
    revenue: 0
  })
  const [planStats, setPlanStats] = useState([])
  const [monthlyRevenue, setMonthlyRevenue] = useState([])
  const [recentTenants, setRecentTenants] = useState([])
  const [subscriptionStatus, setSubscriptionStatus] = useState([])

  useEffect(() => {
    fetchAllData()
  }, [])

  const fetchAllData = async () => {
    try {
      const [tenantsRes, subscriptionsRes, invoicesRes, plansRes] = await Promise.all([
        axios.get(`${API}/tenants`),
        axios.get(`${API}/subscriptions`),
        axios.get(`${API}/invoices`),
        axios.get(`${API}/plans`)
      ])

      const tenants = tenantsRes.data
      const subscriptions = subscriptionsRes.data
      const invoices = invoicesRes.data
      const plans = plansRes.data

      // 기본 통계
      const paidInvoices = invoices.filter(inv => inv.status === 'PAID')
      const revenue = paidInvoices.reduce((sum, inv) => sum + parseFloat(inv.amount), 0)

      setStats({
        tenants: tenants.length,
        subscriptions: subscriptions.filter(s => s.status === 'ACTIVE').length,
        invoices: invoices.length,
        revenue: revenue
      })

      // 플랜별 구독자 수 (파이차트)
      const planCounts = plans.map(plan => {
        const count = subscriptions.filter(s => s.plan?.id === plan.id && s.status === 'ACTIVE').length
        return { name: plan.name, value: count }
      }).filter(p => p.value > 0)
      setPlanStats(planCounts)

      // 구독 상태별 (파이차트)
      const statusCounts = ['ACTIVE', 'CANCELLED', 'EXPIRED', 'PENDING'].map(status => {
        const count = subscriptions.filter(s => s.status === status).length
        return { name: status, value: count }
      }).filter(s => s.value > 0)
      setSubscriptionStatus(statusCounts)

      // 월별 매출 (바차트)
      const monthlyData = calculateMonthlyRevenue(invoices)
      setMonthlyRevenue(monthlyData)

      // 최근 가입 고객
      const sorted = [...tenants].sort((a, b) => 
        new Date(b.createdAt) - new Date(a.createdAt)
      ).slice(0, 5)
      setRecentTenants(sorted)

    } catch (error) {
      console.error('Failed to fetch data:', error)
    }
  }

  const calculateMonthlyRevenue = (invoices) => {
    const months = {}
    const now = new Date()
    
    // 최근 6개월 초기화
    for (let i = 5; i >= 0; i--) {
      const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
      const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
      months[key] = { month: `${date.getMonth() + 1}월`, revenue: 0, count: 0 }
    }

    // 결제된 청구서 집계
    invoices.filter(inv => inv.status === 'PAID' && inv.paidAt).forEach(inv => {
      const date = new Date(inv.paidAt)
      const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
      if (months[key]) {
        months[key].revenue += parseFloat(inv.amount)
        months[key].count += 1
      }
    })

    return Object.values(months)
  }

  return (
    <div className="dashboard">
      <h1>대시보드</h1>
      
      {/* 상단 통계 카드 */}
      <div className="stats-grid">
        <div className="stat-card">
          <h3>총 고객사</h3>
          <p className="stat-number">{stats.tenants}</p>
        </div>
        <div className="stat-card">
          <h3>활성 구독</h3>
          <p className="stat-number">{stats.subscriptions}</p>
        </div>
        <div className="stat-card">
          <h3>총 청구서</h3>
          <p className="stat-number">{stats.invoices}</p>
        </div>
        <div className="stat-card highlight">
          <h3>총 매출</h3>
          <p className="stat-number">₩{stats.revenue.toLocaleString()}</p>
        </div>
      </div>

      {/* 차트 영역 */}
      <div className="charts-grid">
        {/* 월별 매출 바차트 */}
        <div className="chart-card">
          <h3>월별 매출 현황</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={monthlyRevenue}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip formatter={(value) => `₩${value.toLocaleString()}`} />
              <Bar dataKey="revenue" fill="#4a90d9" name="매출" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* 플랜별 구독자 파이차트 */}
        <div className="chart-card">
          <h3>플랜별 구독 현황</h3>
          {planStats.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={planStats}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={100}
                  dataKey="value"
                  label={({ name, value }) => `${name}: ${value}`}
                >
                  {planStats.map((entry, index) => (
                    <Cell key={index} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <p className="no-data">구독 데이터가 없습니다</p>
          )}
        </div>

        {/* 구독 상태 파이차트 */}
        <div className="chart-card">
          <h3>구독 상태 현황</h3>
          {subscriptionStatus.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={subscriptionStatus}
                  cx="50%"
                  cy="50%"
                  outerRadius={100}
                  dataKey="value"
                  label={({ name, value }) => `${name}: ${value}`}
                >
                  {subscriptionStatus.map((entry, index) => (
                    <Cell key={index} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <p className="no-data">구독 데이터가 없습니다</p>
          )}
        </div>

        {/* 최근 가입 고객 */}
        <div className="chart-card">
          <h3>최근 가입 고객</h3>
          <table className="mini-table">
            <thead>
              <tr>
                <th>회사명</th>
                <th>이메일</th>
                <th>가입일</th>
              </tr>
            </thead>
            <tbody>
              {recentTenants.map(tenant => (
                <tr key={tenant.id}>
                  <td>{tenant.companyName}</td>
                  <td>{tenant.email}</td>
                  <td>{new Date(tenant.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
              {recentTenants.length === 0 && (
                <tr><td colSpan="3" className="no-data">고객 데이터가 없습니다</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

export default Dashboard