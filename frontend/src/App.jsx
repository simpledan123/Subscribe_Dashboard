import { useState, useEffect } from 'react'
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import Tenants from './pages/Tenants'
import Plans from './pages/Plans'
import Subscriptions from './pages/Subscriptions'
import Invoices from './pages/Invoices'
import Usages from './pages/Usages'
import Login from './pages/Login'
import './App.css'

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (token) {
      setIsLoggedIn(true)
    }
  }, [])

  const handleLogin = () => {
    setIsLoggedIn(true)
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    setIsLoggedIn(false)
  }

  if (!isLoggedIn) {
    return <Login onLogin={handleLogin} />
  }

  

  return (
    <Router>
      <div className="app">
        <nav className="sidebar" data-testid="sidebar">
          <h2 data-testid="app-title">구독 관리</h2>
          <ul>
            <li>
              <Link to="/" data-testid="nav-dashboard">
                대시보드
              </Link>
            </li>
            <li>
              <Link to="/tenants" data-testid="nav-tenants">
                계정 관리
              </Link>
            </li>
            <li>
              <Link to="/plans" data-testid="nav-plans">
                서비스 관리
              </Link>
            </li>
            <li>
              <Link to="/subscriptions" data-testid="nav-subs">
                구독 관리
              </Link>
            </li>
            <li>
              <Link to="/invoices" data-testid="nav-invoices">
                결제 내역
              </Link>
            </li>
            <li>
              <Link to="/usages" data-testid="nav-usages">
                사용량 관리
              </Link>
            </li>
          </ul>
          <button
            className="logout-btn"
            data-testid="logout"
            onClick={handleLogout}
          >
            로그아웃
          </button>
        </nav>

        <main className="content" data-testid="content">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/tenants" element={<Tenants />} />
            <Route path="/plans" element={<Plans />} />
            <Route path="/subscriptions" element={<Subscriptions />} />
            <Route path="/invoices" element={<Invoices />} />
            <Route path="/usages" element={<Usages />} />
          </Routes>
        </main>
      </div>
    </Router>
  )
}

useEffect(() => {
  const token = localStorage.getItem('token')
  if (token) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
    setIsLoggedIn(true)
  }
}, [])

const handleLogin = () => {
  const token = localStorage.getItem('token')
  axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
  setIsLoggedIn(true)
}

const handleLogout = () => {
  localStorage.removeItem('token')
  delete axios.defaults.headers.common['Authorization']
  setIsLoggedIn(false)
}

export default App