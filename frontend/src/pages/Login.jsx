import { useState } from 'react'
import axios from 'axios'

const API = 'http://localhost:8080/api'

function Login({ onLogin }) {
  const [isRegister, setIsRegister] = useState(false)
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    try {
      if (isRegister) {
        await axios.post(`${API}/auth/register`, form)
        alert('회원가입 성공! 로그인해주세요.')
        setIsRegister(false)
      } else {
        const res = await axios.post(`${API}/auth/login`, form)
        localStorage.setItem('token', res.data.token)
        onLogin()
      }
    } catch (err) {
      setError(err.response?.data?.error || '오류가 발생했습니다.')
    }
  }

  return (
    <div className="login-container">
      <div className="login-box">
        <h2>SaaS Admin</h2>
        <p className="login-subtitle">{isRegister ? '회원가입' : '로그인'}</p>
        
        {error && <div className="error-msg">{error}</div>}
        
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder="아이디"
            value={form.username}
            onChange={(e) => setForm({...form, username: e.target.value})}
            required
          />
          <input
            type="password"
            placeholder="비밀번호"
            value={form.password}
            onChange={(e) => setForm({...form, password: e.target.value})}
            required
          />
          <button type="submit">{isRegister ? '회원가입' : '로그인'}</button>
        </form>
        
        <p className="toggle-text">
          {isRegister ? '이미 계정이 있나요?' : '계정이 없나요?'}
          <span onClick={() => setIsRegister(!isRegister)}>
            {isRegister ? ' 로그인' : ' 회원가입'}
          </span>
        </p>
      </div>
    </div>
  )
}

export default Login