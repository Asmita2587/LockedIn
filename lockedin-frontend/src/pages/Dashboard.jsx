import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'

export default function Dashboard() {
  const { user } = useAuth()
  const [balance, setBalance] = useState(null)
  const [matches, setMatches] = useState([])

  useEffect(() => {
    api.get('/api/credits/balance').then((res) => setBalance(res.data)).catch(() => {})
    api.get('/api/matches/suggestions').then((res) => setMatches(res.data)).catch(() => {})
  }, [])

  return (
    <div className="page">
      <h1>Welcome back, {user?.name}</h1>
      <p className="page-subtitle">Here's where things stand today.</p>

      <div className="credit-hero">
        <div className="label">Credit balance</div>
        {balance ? (
          <>
            <div className="amount">{balance.balance}</div>
            <div className={`note ${balance.lowBalance ? 'warning' : ''}`}>
              {balance.lowBalance
                ? 'Running low — answer a doubt to earn more.'
                : 'Teaching and helping others earns bonus credits.'}
            </div>
          </>
        ) : <div className="note">Loading…</div>}
        <Link to="/credits">View details →</Link>
      </div>

      <div className="card-grid">
        <div className="card">
          <h3>Suggested matches</h3>
          <p className="big-number">{matches.length}</p>
          <Link to="/matches">See matches →</Link>
        </div>

        <div className="card">
          <h3>Your groups</h3>
          <p>Join or create a study group to get started.</p>
          <Link to="/groups">Go to groups →</Link>
        </div>
      </div>
    </div>
  )
}
