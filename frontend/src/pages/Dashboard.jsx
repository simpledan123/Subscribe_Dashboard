import { useEffect, useMemo, useState } from 'react'
import { Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import api from '../api'

const won = (n) => `₩${Number(n || 0).toLocaleString('ko-KR')}`
const colors = ['#6674e8', '#78b9e5', '#72c5a2', '#edb46d', '#a88ae8']

export default function Dashboard() {
  const [data, setData] = useState({ accounts: [], services: [], subscriptions: [], payments: [] })
  const [error, setError] = useState('')
  useEffect(() => {
    Promise.all(['/accounts','/services','/subscriptions','/payments'].map((url) => api.get(url)))
      .then(([a,s,sub,p]) => setData({accounts:a.data,services:s.data,subscriptions:sub.data,payments:p.data}))
      .catch(() => setError('데이터를 불러오지 못했습니다. 백엔드와 로그인 상태를 확인해 주세요.'))
  }, [])

  const stats = useMemo(() => {
    const active = data.subscriptions.filter((s) => s.status === 'ACTIVE')
    const monthly = active.reduce((sum, s) => sum + (s.billingCycle === 'YEARLY' ? Number(s.price)/12 : Number(s.price)), 0)
    const upcoming = data.payments.filter((p) => p.status === 'SCHEDULED')
    const benefits = active.filter((s) => s.endDate)
    return { active, monthly, upcoming, benefits }
  }, [data])

  const monthlyData = useMemo(() => {
    const now = new Date()
    return Array.from({length:6}, (_,i) => {
      const d = new Date(now.getFullYear(), now.getMonth()-5+i, 1)
      const key = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}`
      const amount = data.payments.filter((p) => p.status === 'PAID' && p.paidAt?.startsWith(key)).reduce((n,p)=>n+Number(p.amount),0)
      return { month:`${d.getMonth()+1}월`, amount }
    })
  }, [data.payments])

  const categories = useMemo(() => Object.values(stats.active.reduce((acc,s) => {
    const name=s.servicePlan?.category || '기타'; acc[name] ||= {name,value:0}; acc[name].value += Number(s.price); return acc
  }, {})), [stats.active])

  const today = new Date().toLocaleDateString('ko-KR',{year:'numeric',month:'long',day:'numeric',weekday:'short'})
  return <div>
    <header className="page-head"><div><h1>안녕하세요, 윤단님</h1><p>흩어진 구독과 혜택 만료일을 한곳에서 확인하세요.</p></div><span className="date-chip">{today}</span></header>
    {error && <div className="error-banner">{error}</div>}
    <section className="stats-grid">
      <Stat title="등록 계정" value={`${data.accounts.length}개`} note="서비스 로그인 계정" icon="◎" />
      <Stat title="활성 구독" value={`${stats.active.length}개`} note={`무료 혜택 ${stats.benefits.length}개 포함`} icon="↻" />
      <Stat title="월 예상 지출" value={won(stats.monthly)} note="연간 구독은 월 환산" icon="₩" />
      <Stat title="결제 예정" value={`${stats.upcoming.length}건`} note="30일 이내 일정을 확인하세요" icon="⌁" warn />
    </section>
    <section className="content-grid">
      <div className="panel"><div className="panel-title"><h2>다가오는 결제와 만료</h2><span>예정일순</span></div>
        <table className="data-table"><thead><tr><th>서비스</th><th>계정</th><th>예정일</th><th>금액</th></tr></thead><tbody>
          {stats.upcoming.slice().sort((a,b)=>a.scheduledDate.localeCompare(b.scheduledDate)).slice(0,5).map((p)=><tr key={p.id}><td><Service sub={p.subscription}/></td><td>{p.subscription?.account?.nickname}</td><td>{p.scheduledDate}</td><td><strong>{won(p.amount)}</strong></td></tr>)}
          {!stats.upcoming.length && <tr><td colSpan="4" className="empty">예정된 결제가 없습니다.</td></tr>}
        </tbody></table>
      </div>
      <div className="panel"><div className="panel-title"><h2>카테고리별 지출</h2><span>현재 활성 구독</span></div>
        {categories.length ? <ResponsiveContainer width="100%" height={230}><PieChart><Pie data={categories} dataKey="value" innerRadius={55} outerRadius={88} paddingAngle={3}>{categories.map((_,i)=><Cell key={i} fill={colors[i%colors.length]}/>)}</Pie><Tooltip formatter={(v)=>won(v)}/></PieChart></ResponsiveContainer> : <div className="empty">구독을 등록해 주세요.</div>}
      </div>
    </section>
    <section className="panel"><div className="panel-title"><h2>최근 6개월 지출</h2><span>결제 완료 기준</span></div>
      <ResponsiveContainer width="100%" height={250}><BarChart data={monthlyData}><CartesianGrid vertical={false} stroke="#edf0f5"/><XAxis dataKey="month" axisLine={false} tickLine={false} fontSize={11}/><YAxis axisLine={false} tickLine={false} fontSize={10}/><Tooltip formatter={(v)=>won(v)}/><Bar dataKey="amount" fill="#6674e8" radius={[5,5,0,0]} maxBarSize={42}/></BarChart></ResponsiveContainer>
    </section>
  </div>
}

function Stat({title,value,note,icon,warn}) { return <div className="stat-card"><div className="stat-top"><span>{title}</span><span className="stat-icon">{icon}</span></div><div className="stat-value">{value}</div><div className={`stat-note ${warn?'warn':''}`}>{note}</div></div> }
function Service({sub}) { return <div className="service-cell"><span className="service-logo">{sub?.servicePlan?.serviceName?.[0]}</span><div><strong>{sub?.servicePlan?.serviceName}</strong><small>{sub?.servicePlan?.planName}</small></div></div> }
