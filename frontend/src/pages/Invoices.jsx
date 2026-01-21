import { useState, useEffect } from 'react'
import axios from 'axios'

const API = 'http://localhost:8080/api'

function Invoices() {
  const [invoices, setInvoices] = useState([])
  const [subscriptions, setSubscriptions] = useState([])
  const [tenants, setTenants] = useState([])
  const [selectedSubscription, setSelectedSubscription] = useState('')

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    const [inv, subs, tenants] = await Promise.all([
      axios.get(`${API}/invoices`),
      axios.get(`${API}/subscriptions`),
      axios.get(`${API}/tenants`)
    ])
    setInvoices(inv.data)
    setSubscriptions(subs.data)
    setTenants(tenants.data)
  }

  const handleGenerate = async (e) => {
    e.preventDefault()
    if (!selectedSubscription) return
    await axios.post(`${API}/invoices`, { subscriptionId: Number(selectedSubscription) })
    setSelectedSubscription('')
    fetchData()
  }

  const handlePay = async (id) => {
    await axios.put(`${API}/invoices/${id}/pay`)
    fetchData()
  }

  const getTenantName = (id) => {
    const tenant = tenants.find(t => t.id === id)
    return tenant ? tenant.companyName : '-'
  }

  return (
    <div>
      <h1>청구서 관리</h1>

      <form onSubmit={handleGenerate} className="form-card">
        <select
          value={selectedSubscription}
          onChange={(e) => setSelectedSubscription(e.target.value)}
          required
        >
          <option value="">구독 선택</option>
          {subscriptions.filter(s => s.status === 'ACTIVE').map(sub => (
            <option key={sub.id} value={sub.id}>
              {getTenantName(sub.tenant?.id)} - {sub.plan?.name}
            </option>
          ))}
        </select>
        <button type="submit">청구서 생성</button>
      </form>

      <table className="data-table">
        <thead>
          <tr>
            <th>청구서 번호</th>
            <th>고객사</th>
            <th>금액</th>
            <th>상태</th>
            <th>발행일</th>
            <th>만기일</th>
            <th>결제일</th>
            <th>액션</th>
          </tr>
        </thead>
        <tbody>
          {invoices.map(inv => (
            <tr key={inv.id}>
              <td>{inv.invoiceNumber}</td>
              <td>{getTenantName(inv.tenant?.id)}</td>
              <td>₩{parseFloat(inv.amount).toLocaleString()}</td>
              <td><span className={`status ${inv.status.toLowerCase()}`}>{inv.status}</span></td>
              <td>{new Date(inv.issueDate).toLocaleDateString()}</td>
              <td>{new Date(inv.dueDate).toLocaleDateString()}</td>
              <td>{inv.paidAt ? new Date(inv.paidAt).toLocaleDateString() : '-'}</td>
              <td>
                {inv.status === 'PENDING' && (
                  <button onClick={() => handlePay(inv.id)} className="pay">결제완료</button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default Invoices