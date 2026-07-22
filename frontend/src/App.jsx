import { BrowserRouter, NavLink, Route, Routes } from 'react-router-dom'
import { useState } from 'react'
import Dashboard from './pages/Dashboard'
import Accounts from './pages/Accounts'
import Services from './pages/Services'
import Subscriptions from './pages/Subscriptions'
import Payments from './pages/Payments'
import Benefits from './pages/Benefits'
import Login from './pages/Login'
import './App.css'

const menus = [
  ['/', '⌂', '대시보드', 'nav-dashboard'],
  ['/accounts', '◎', '계정 관리', 'nav-accounts'],
  ['/services', '▦', '서비스 관리', 'nav-services'],
  ['/subscriptions', '↻', '구독 관리', 'nav-subs'],
  ['/payments', '₩', '결제 내역', 'nav-payments'],
  ['/benefits', '◔', '혜택·사용량', 'nav-benefits'],
]

export default function App() {
  const [loggedIn, setLoggedIn] = useState(Boolean(localStorage.getItem('token')))
  if (!loggedIn) return <Login onLogin={() => setLoggedIn(true)} />

  const logout = () => {
    localStorage.removeItem('token')
    setLoggedIn(false)
  }

  return (
    <BrowserRouter>
      <div className="shell">
        <aside className="sidebar" data-testid="sidebar">
          <div className="brand"><span className="brand-mark">S</span><div><strong>Subtrack</strong><small>나의 구독 한눈에</small></div></div>
          <nav>
            <p className="nav-label">MENU</p>
            {menus.map(([to, icon, label, testid]) => (
              <NavLink key={to} to={to} end={to === '/'} data-testid={testid}>
                <span className="nav-icon">{icon}</span>{label}
              </NavLink>
            ))}
          </nav>
          <div className="sidebar-foot">
            <div className="profile"><span>윤</span><div><strong>윤단</strong><small>개인 워크스페이스</small></div></div>
            <button onClick={logout} data-testid="logout">로그아웃</button>
          </div>
        </aside>
        <main className="main-content" data-testid="content">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/accounts" element={<Accounts />} />
            <Route path="/services" element={<Services />} />
            <Route path="/subscriptions" element={<Subscriptions />} />
            <Route path="/payments" element={<Payments />} />
            <Route path="/benefits" element={<Benefits />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}
