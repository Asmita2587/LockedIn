import { useEffect, useState } from 'react'
import api from '../api/axios'

export default function Matches() {
  const [matches, setMatches] = useState([])
  const [message, setMessage] = useState('')

  function loadMatches() {
    api.get('/api/matches/suggestions').then((res) => setMatches(res.data)).catch(() => {})
  }

  useEffect(() => { loadMatches() }, [])

  async function handleAccept(match) {
    setMessage('')
    try {
      await api.post('/api/matches/accept', null, {
        params: {
          candidateUserId: match.candidateUserId,
          skillId: match.skillId,
          matchType: match.matchType
        }
      })
      setMessage(`Match with ${match.candidateName} accepted.`)
      loadMatches()
    } catch (err) {
      setMessage(err.response?.data?.message || 'Could not accept that match.')
    }
  }

  return (
    <div className="page">
      <h1>Suggested matches</h1>
      <p className="page-subtitle">Mutual matches — where you can also teach them something — are shown first.</p>
      {message && <p className="info">{message}</p>}

      <ul className="list">
        {matches.map((m, i) => (
          <li key={i} className="match-row">
            <div>
              <strong>{m.candidateName}</strong> can teach <strong>{m.skillName}</strong>
              <span className={`badge ${m.matchType === 'MUTUAL' ? 'badge-green' : 'badge-gray'}`}>
                {m.matchType === 'MUTUAL' ? 'Mutual match' : 'One-directional'}
              </span>
            </div>
            <button onClick={() => handleAccept(m)}>Accept</button>
          </li>
        ))}
      </ul>
      {matches.length === 0 && <p className="empty-state">No matches yet — add a skill you want to learn on the Skills page to see who can teach it.</p>}
    </div>
  )
}
