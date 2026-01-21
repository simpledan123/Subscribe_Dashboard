import { useState, useEffect } from 'react'
import axios from 'axios'

const API = 'http://localhost:8080/api'

function Tenants() {
  const [tenants, setTenants] = useState([])
  const [form, setForm] = useState({ companyName: '', email: '', phone: '' })
  const [editing, setEditing] = useState(null)

  useEffect(() => {
    fetchTenants()
  }, [])

  const fetchTenants = async () => {
    const res = await axios.get(`${API}/tenants`)
    setTenants(res.data)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (editing) {
      await axios.put(`${API}/tenants/${editing}`, form)
    } else {
      await axios.post(`${API}/tenants`, form)
    }
    setForm({ companyName: '', email: '', phone: '' })
    setEditing(null)
    fetchTenants()
  }

  const handleEdit = (tenant) => {
    setForm({ companyName: tenant.companyName, email: tenant.email, phone: tenant.phone })
    setEditing(tenant.id)
  }

  const handleDelete = async (id) => {
    if (window.confirm('정말 삭제하시겠습니까?')) {
      await axios.delete(`${API}/tenants/${id}`)
      fetchTenants()
    }
  }

  return (
    <div>
      <h1>고객사 관리</h1>
      
      <form onSubmit={handleSubmit} className="form-card">
        <input
          type="text"
          placeholder="회사명"
          value={form.companyName}
          onChange={(e) => setForm({...form, companyName: e.target.value})}
          required
        />
        <input
          type="email"
          placeholder="이메일"
          value={form.email}
          onChange={(e) => setForm({...form, email: e.target.value})}
          required
        />
        <input
          type="text"
          placeholder="전화번호"
          value={form.phone}
          onChange={(e) => setForm({...form, phone: e.target.value})}
        />
        <button type="submit">{editing ? '수정' : '추가'}</button>
        {editing && <button type="button" onClick={() => { setEditing(null); setForm({ companyName: '', email: '', phone: '' }) }}>취소</button>}
      </form>

      <table className="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>회사명</th>
            <th>이메일</th>
            <th>전화번호</th>
            <th>상태</th>
            <th>액션</th>
          </tr>
        </thead>
        <tbody>
          {tenants.map(tenant => (
            <tr key={tenant.id}>
              <td>{tenant.id}</td>
              <td>{tenant.companyName}</td>
              <td>{tenant.email}</td>
              <td>{tenant.phone}</td>
              <td><span className={`status ${tenant.status.toLowerCase()}`}>{tenant.status}</span></td>
              <td>
                <button onClick={() => handleEdit(tenant)}>수정</button>
                <button onClick={() => handleDelete(tenant.id)} className="delete">삭제</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default Tenants