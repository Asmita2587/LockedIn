import { useEffect, useState } from 'react'
import api from '../api/axios'
import VideoCall from '../components/VideoCall'

export default function Matches() {
  const [matches, setMatches] = useState([])
  const [mySkills, setMySkills] = useState([])
  const [tutors, setTutors] = useState([])
  const [accepted, setAccepted] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('lockedin_accepted_matches') || '[]')
    } catch {
      return []
    }
  })
  const [learnSkillInput, setLearnSkillInput] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [activeCall, setActiveCall] = useState(null)

  const currentUserName = localStorage.getItem('lockedin_name') || 'Student'

  function loadAll() {
    api.get('/api/matches/suggestions')
      .then((res) => setMatches(Array.isArray(res.data) ? res.data : []))
      .catch(() => {})

    api.get('/api/skills/me')
      .then((res) => setMySkills(Array.isArray(res.data) ? res.data : []))
      .catch(() => {})

    api.get('/api/matches/tutors')
      .then((res) => setTutors(Array.isArray(res.data) ? res.data : []))
      .catch(() => {})

    api.get('/api/matches/accepted')
      .then((res) => {
        if (Array.isArray(res.data) && res.data.length > 0) {
          const mapped = res.data.map((m) => ({
            id: m.id,
            candidateUserId: m.userA?.id,
            candidateName: m.userA?.name || 'Peer',
            learnerName: m.userB?.name || 'Peer',
            skillId: m.skill?.id,
            skillName: m.skill?.name || 'Skill',
            matchType: m.matchType
          }))
          setAccepted(mapped)
          localStorage.setItem('lockedin_accepted_matches', JSON.stringify(mapped))
        }
      })
      .catch(() => {})
  }

  useEffect(() => {
    loadAll()
  }, [])

  async function handleQuickLearn(e) {
    e.preventDefault()
    const trimmed = learnSkillInput.trim()
    if (!trimmed) return
    setError('')
    setMessage('')
    try {
      await api.post('/api/skills/me', {
        skillName: trimmed,
        type: 'LEARN',
        proficiency: 'BEGINNER'
      })
      setLearnSkillInput('')
      setMessage(`Added "${trimmed}" to skills you want to learn — checking for tutors...`)
      loadAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not add skill.')
    }
  }

  async function handleLearnFromTutor(skillName) {
    setError('')
    setMessage('')
    try {
      await api.post('/api/skills/me', {
        skillName,
        type: 'LEARN',
        proficiency: 'BEGINNER'
      })
      setMessage(`Added "${skillName}" to your learning list and matched with available tutors!`)
      loadAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not add skill.')
    }
  }

  async function handleAccept(match) {
    setMessage('')
    setError('')
    try {
      await api.post('/api/matches/accept', null, {
        params: {
          candidateUserId: match.candidateUserId,
          skillId: match.skillId,
          matchType: match.matchType || 'ONE_DIRECTIONAL'
        }
      })
      const next = [
        ...accepted.filter(
          (a) => !(a.candidateUserId === match.candidateUserId && a.skillId === match.skillId)
        ),
        match
      ]
      setAccepted(next)
      localStorage.setItem('lockedin_accepted_matches', JSON.stringify(next))
      setMessage(`Match with ${match.candidateName} for "${match.skillName}" accepted! You can now start a video session.`)
      loadAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not accept that match.')
    }
  }

  function isMatchAccepted(m) {
    return accepted.some(
      (a) => a.candidateUserId === m.candidateUserId && (a.skillId === m.skillId || a.skillName === m.skillName)
    )
  }

  const learnMatches = matches.filter((m) => !m.direction || m.direction === 'LEARN')
  const teachMatches = matches.filter((m) => m.direction === 'TEACH')
  const myLearnSkills = mySkills.filter((s) => s.type === 'LEARN')

  return (
    <div className="page">
      <h1>Matches to Learn</h1>
      <p className="page-subtitle">
        When you add a skill as &ldquo;I want to learn this&rdquo;, any student teaching that skill is automatically matched and displayed below.
      </p>

      <form className="inline-form" onSubmit={handleQuickLearn}>
        <input
          placeholder="Add a skill you want to learn (e.g. Java, Python, DBMS, React)..."
          value={learnSkillInput}
          onChange={(e) => setLearnSkillInput(e.target.value)}
        />
        <button type="submit">Find Tutors to Learn From</button>
        <button type="button" className="secondary" onClick={loadAll}>
          Refresh Matches
        </button>
      </form>

      {myLearnSkills.length > 0 && (
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '16px', alignItems: 'center' }}>
          <span className="muted">Skills you want to learn:</span>
          {myLearnSkills.map((s) => (
            <span key={s.id} className="badge badge-amber">
              {s.skill?.name || s.skillName}
            </span>
          ))}
        </div>
      )}

      {message && <p className="info">{message}</p>}
      {error && <p className="error">{error}</p>}

      {activeCall && (
        <VideoCall
          room={activeCall.room}
          label={activeCall.label}
          name={currentUserName}
          onLeave={() => setActiveCall(null)}
        />
      )}

      <section className="section">
        <h2>Matches to Learn ({learnMatches.length})</h2>
        <ul className="list">
          {learnMatches.map((m, i) => {
            const alreadyAccepted = isMatchAccepted(m)
            const roomKey = `match-skill-${m.skillId}`
            return (
              <li key={i} className="match-row">
                <div>
                  <strong>{m.candidateName}</strong> is teaching <strong>{m.skillName}</strong>
                  {m.proficiency && (
                    <span className="badge badge-gray" style={{ marginLeft: '8px' }}>
                      {String(m.proficiency).toLowerCase()}
                    </span>
                  )}
                  <span className={`badge ${m.matchType === 'MUTUAL' ? 'badge-green' : 'badge-amber'}`}>
                    {m.matchType === 'MUTUAL' ? 'Mutual match' : 'Available Tutor'}
                  </span>
                </div>
                <div className="row-actions">
                  {alreadyAccepted ? (
                    <span className="badge badge-green">✓ Matched</span>
                  ) : (
                    <button onClick={() => handleAccept(m)}>Accept Match</button>
                  )}
                  <button
                    type="button"
                    className="secondary"
                    onClick={() =>
                      setActiveCall({
                        room: roomKey,
                        label: `1-on-1 Session: ${m.skillName} with ${m.candidateName}`
                      })
                    }
                  >
                    🎥 Start Video Session
                  </button>
                </div>
              </li>
            )
          })}
        </ul>
        {learnMatches.length === 0 && (
          <p className="empty-state">
            No tutors matched yet for your learning list. Make sure you added the skill as &ldquo;I want to learn this&rdquo; and another student added the same skill as &ldquo;I can teach this&rdquo;.
          </p>
        )}
      </section>

      {accepted.length > 0 && (
        <section className="section">
          <h2>Your Accepted Matches ({accepted.length})</h2>
          <ul className="list">
            {accepted.map((a, idx) => (
              <li key={idx} className="match-row">
                <div>
                  <strong>{a.candidateName}</strong> · Skill: <strong>{a.skillName}</strong>
                  <span className="badge badge-green">Accepted</span>
                </div>
                <div className="row-actions">
                  <button
                    type="button"
                    onClick={() =>
                      setActiveCall({
                        room: `match-skill-${a.skillId}`,
                        label: `1-on-1 Session: ${a.skillName} with ${a.candidateName}`
                      })
                    }
                  >
                    🎥 Join Live Video Call
                  </button>
                </div>
              </li>
            ))}
          </ul>
        </section>
      )}

      {teachMatches.length > 0 && (
        <section className="section">
          <h2>Students Who Want to Learn From You ({teachMatches.length})</h2>
          <ul className="list">
            {teachMatches.map((m, i) => (
              <li key={i} className="match-row">
                <div>
                  <strong>{m.candidateName}</strong> wants to learn <strong>{m.skillName}</strong> from you
                  <span className="badge badge-green">You teach this</span>
                </div>
                <div className="row-actions">
                  <button
                    type="button"
                    onClick={() =>
                      setActiveCall({
                        room: `match-skill-${m.skillId}`,
                        label: `Teaching ${m.skillName} to ${m.candidateName}`
                      })
                    }
                  >
                    🎥 Teach Live on Video
                  </button>
                </div>
              </li>
            ))}
          </ul>
        </section>
      )}

      {tutors.length > 0 && (
        <section className="section">
          <h2>All Students Currently Teaching on LockedIn ({tutors.length})</h2>
          <ul className="list">
            {tutors.map((t) => (
              <li key={t.id} className="match-row">
                <div>
                  <strong>{t.user?.name || 'Student'}</strong> teaches <strong>{t.skill?.name}</strong>
                  <span className="badge badge-green">
                    Teaching · {String(t.proficiency || 'INTERMEDIATE').toLowerCase()}
                  </span>
                </div>
                <div className="row-actions">
                  <button
                    type="button"
                    className="secondary"
                    onClick={() => handleLearnFromTutor(t.skill?.name)}
                  >
                    + I want to learn {t.skill?.name}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  )
}
