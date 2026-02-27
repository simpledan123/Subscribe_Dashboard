import { useState, useEffect } from 'react'
import axios from 'axios'

const API = 'http://localhost:8080/api'

const DEFAULT_FORM = {
  name: '',
  description: '',
  monthlyPrice: 0,
  yearlyPrice: 0,
  maxApiCalls: 1000,
  maxStorage: 100,
  maxUsers: 5
}

function Plans() {
  const [plans, setPlans] = useState([])
  const [form, setForm] = useState({ ...DEFAULT_FORM })
  const [editing, setEditing] = useState(null)

  useEffect(() => {
    fetchPlans()
  }, [])

  const fetchPlans = async () => {
    const res = await axios.get(`${API}/plans`)
    setPlans(res.data)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (editing) {
      await axios.put(`${API}/plans/${editing}`, form)
    } else {
      await axios.post(`${API}/plans`, form)
    }
    setForm({ ...DEFAULT_FORM })
    setEditing(null)
    fetchPlans()
  }

  const handleEdit = (plan) => {
    setForm({
      name: plan.name,
      description: plan.description,
      monthlyPrice: plan.monthlyPrice,
      yearlyPrice: plan.yearlyPrice,
      maxApiCalls: plan.maxApiCalls,
      maxStorage: plan.maxStorage,
      maxUsers: plan.maxUsers
    })
    setEditing(plan.id)
  }

  const handleCancel = () => {
    setEditing(null)
    setForm({ ...DEFAULT_FORM })
  }

  const handleDelete = async (id) => {
    if (window.confirm('정말 삭제하시겠습니까?')) {
      await axios.delete(`${API}/plans/${id}`)
      fetchPlans()
    }
  }

  return (
    <div data-testid="plan-page">
      <h1 data-testid="plan-title">플랜 관리</h1>

      <form onSubmit={handleSubmit} className="form-card" data-testid="plan-form">
        <input
          data-testid="plan-name"
          type="text"
          placeholder="플랜명"
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
          required
        />
        <input
          data-testid="plan-description"
          type="text"
          placeholder="설명"
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
        />
        <input
          data-testid="plan-monthlyPrice"
          type="number"
          placeholder="월간 가격"
          value={form.monthlyPrice}
          onChange={(e) => setForm({ ...form, monthlyPrice: Number(e.target.value) })}
        />
        <input
          data-testid="plan-yearlyPrice"
          type="number"
          placeholder="연간 가격"
          value={form.yearlyPrice}
          onChange={(e) => setForm({ ...form, yearlyPrice: Number(e.target.value) })}
        />
        <input
          data-testid="plan-maxApiCalls"
          type="number"
          placeholder="API 호출 제한"
          value={form.maxApiCalls}
          onChange={(e) => setForm({ ...form, maxApiCalls: Number(e.target.value) })}
        />
        <input
          data-testid="plan-maxStorage"
          type="number"
          placeholder="저장공간(MB)"
          value={form.maxStorage}
          onChange={(e) => setForm({ ...form, maxStorage: Number(e.target.value) })}
        />
        <input
          data-testid="plan-maxUsers"
          type="number"
          placeholder="최대 사용자"
          value={form.maxUsers}
          onChange={(e) => setForm({ ...form, maxUsers: Number(e.target.value) })}
        />

        <button data-testid="plan-submit" type="submit">
          {editing ? '수정' : '추가'}
        </button>

        {editing && (
          <button data-testid="plan-cancel" type="button" onClick={handleCancel}>
            취소
          </button>
        )}
      </form>

      <div className="plans-grid" data-testid="plan-grid">
        {plans.map((plan) => (
          <div
            key={plan.id}
            className="plan-card"
            data-testid={`plan-card-${plan.id}`}
          >
            <h3>{plan.name}</h3>
            <p className="plan-price">₩{plan.monthlyPrice.toLocaleString()}/월</p>
            <p className="plan-price-yearly">₩{plan.yearlyPrice.toLocaleString()}/년</p>
            <ul>
              <li>API 호출: {plan.maxApiCalls.toLocaleString()}회</li>
              <li>저장공간: {plan.maxStorage}MB</li>
              <li>사용자: {plan.maxUsers}명</li>
            </ul>
            <p className="plan-desc">{plan.description}</p>

            <div className="plan-actions">
              <button
                data-testid={`plan-edit-${plan.id}`}
                onClick={() => handleEdit(plan)}
              >
                수정
              </button>
              <button
                data-testid={`plan-delete-${plan.id}`}
                onClick={() => handleDelete(plan.id)}
                className="delete"
              >
                삭제
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

export default Plans