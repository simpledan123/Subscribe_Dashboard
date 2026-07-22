import { useEffect, useState } from 'react'
import api from '../api'

const empty = { nickname:'', email:'', purpose:'PERSONAL', memo:'', status:'ACTIVE' }
const purpose = { PERSONAL:'개인', STUDENT_BENEFIT:'학생 혜택', AI:'AI 서비스', JOB_SEARCH:'취업 준비' }

export default function Accounts() {
  const [items,setItems]=useState([]), [form,setForm]=useState(empty), [editing,setEditing]=useState(null), [error,setError]=useState('')
  const load=()=>api.get('/accounts').then(r=>setItems(r.data)).catch(()=>setError('계정 목록을 불러오지 못했습니다.'))
  useEffect(load,[])
  const submit=async(e)=>{e.preventDefault();setError('');try{editing?await api.put(`/accounts/${editing}`,form):await api.post('/accounts',form);setForm(empty);setEditing(null);load()}catch(err){setError(err.response?.data?.message||'계정을 저장하지 못했습니다.')}}
  const edit=(x)=>{setEditing(x.id);setForm({nickname:x.nickname,email:x.email,purpose:x.purpose,memo:x.memo||'',status:x.status})}
  const remove=async(id)=>{if(confirm('연결된 구독이 없다면 계정을 삭제합니다. 계속할까요?')){try{await api.delete(`/accounts/${id}`);load()}catch{setError('구독이 연결된 계정은 삭제할 수 없습니다.')}}}
  return <div><Header title="계정 관리" text="서비스마다 사용하는 이메일과 계정 목적을 구분해 관리합니다." />{error&&<div className="error-banner">{error}</div>}
    <form className="form-panel form-grid" onSubmit={submit} data-testid="account-form">
      <input placeholder="계정 별칭" value={form.nickname} onChange={e=>setForm({...form,nickname:e.target.value})} required />
      <input type="email" placeholder="로그인 이메일" value={form.email} onChange={e=>setForm({...form,email:e.target.value})} required />
      <select value={form.purpose} onChange={e=>setForm({...form,purpose:e.target.value})}>{Object.entries(purpose).map(([v,l])=><option key={v} value={v}>{l}</option>)}</select>
      <input placeholder="메모 (선택)" value={form.memo} onChange={e=>setForm({...form,memo:e.target.value})}/>
      <div className="form-actions"><button className="btn">{editing?'계정 수정':'계정 추가'}</button>{editing&&<button type="button" className="btn secondary" onClick={()=>{setEditing(null);setForm(empty)}}>취소</button>}</div>
    </form>
    <div className="table-wrap"><table className="data-table"><thead><tr><th>별칭</th><th>이메일</th><th>목적</th><th>메모</th><th>상태</th><th></th></tr></thead><tbody>{items.map(x=><tr key={x.id}><td><strong>{x.nickname}</strong></td><td>{x.email}</td><td>{purpose[x.purpose]||x.purpose}</td><td>{x.memo||'-'}</td><td><span className={`badge ${x.status.toLowerCase()}`}>{x.status}</span></td><td><button className="btn small secondary" onClick={()=>edit(x)}>수정</button> <button className="btn small danger" onClick={()=>remove(x.id)}>삭제</button></td></tr>)}</tbody></table></div>
  </div>
}
function Header({title,text}){return <header className="page-head"><div><h1>{title}</h1><p>{text}</p></div></header>}
