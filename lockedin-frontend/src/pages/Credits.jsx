import { useEffect, useState } from 'react'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'

export default function Credits() {
  const { user } = useAuth()
  const [balance, setBalance] = useState(null)
  const [teachers, setTeachers] = useState([])
  const [learners, setLearners] = useState([])
  const [portfolio, setPortfolio] = useState(null)

  useEffect(() => {
    api.get('/api/credits/balance').then((res) => setBalance(res.data)).catch(() => {})
    api.get('/api/leaderboard/teachers').then((res) => setTeachers(res.data)).catch(() => {})
    api.get('/api/leaderboard/learners').then((res) => setLearners(res.data)).catch(() => {})
    if (user?.userId) {
      api.get(`/api/users/${user.userId}/portfolio`).then((res) => setPortfolio(res.data)).catch(() => {})
    }
  }, [user])

  return (
    <div className="page">
      <h1>Credits &amp; standing</h1>
      <p className="page-subtitle">Your balance, your teaching record, and where you rank.</p>

      <div className="credit-hero">
        <div className="label">Your balance</div>
        {balance ? (
          <>
            <div className="amount">{balance.balance}</div>
            {balance.lowBalance && <div className="note warning">Low balance — answer a doubt to earn more.</div>}
          </>
        ) : <div className="note">Loading…</div>}
      </div>

      <div className="card-grid">
        <div className="card">
          <h3>Your teaching portfolio</h3>
          {portfolio ? (
            <>
              <p>Trust score: <strong>{portfolio.trustScore.toFixed(1)}</strong> / 5</p>
              <p>Sessions taught: <strong>{portfolio.sessionsTaught}</strong></p>
              <p>Subjects: {portfolio.subjectsCovered.join(', ') || '—'}</p>
            </>
          ) : <p>Loading…</p>}
        </div>

        <div className="card">
          <h3>Top teachers</h3>
          {teachers.length > 0 ? (
            <ol>{teachers.map((t) => <li key={t.userId}>{t.name} — {t.score} sessions</li>)}</ol>
          ) : <p>No data yet.</p>}
        </div>

        <div className="card">
          <h3>Top learners</h3>
          {learners.length > 0 ? (
            <ol>{learners.map((l) => <li key={l.userId}>{l.name} — {l.score} topics done</li>)}</ol>
          ) : <p>No data yet.</p>}
        </div>
      </div>
    </div>
  )
}
