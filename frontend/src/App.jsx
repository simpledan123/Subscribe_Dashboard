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
        <nav className="sidebar">
          <h2>SaaS Admin</h2>
          <ul>
            <li><Link to="/">대시보드</Link></li>
            <li><Link to="/tenants">고객사 관리</Link></li>
            <li><Link to="/plans">플랜 관리</Link></li>
            <li><Link to="/subscriptions">구독 관리</Link></li>
            <li><Link to="/invoices">청구서 관리</Link></li>
            <li><Link to="/usages">사용량 관리</Link></li>
          </ul>
          <button className="logout-btn" onClick={handleLogout}>로그아웃</button>
        </nav>
        <main className="content">
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

export default App