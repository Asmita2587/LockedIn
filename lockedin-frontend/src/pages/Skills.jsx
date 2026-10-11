import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'

export default function Skills() {
  const [skills, setSkills] = useState([])
  const [matchesToLearn, setMatchesToLearn] = useState([])
  const [skillName, setSkillName] = useState('')
  const [type, setType] = useState('TEACH')
  const [proficiency, setProficiency] = useState('INTERMEDIATE')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  function loadSkillsAndMatches() {
    api.get('/api/skills/me')
      .then((res) => {
        setSkills(Array.isArray(res.data) ? res.data : [])
        setError('')
      })
      .catch((err) => setError(err.response?.data?.message || 'Could not load skills.'))

    api.get('/api/matches/suggestions')
      .then((res) => {
        const all = Array.isArray(res.data) ? res.data : []
        setMatchesToLearn(all.filter((m) => !m.direction || m.direction === 'LEARN'))
      })
      .catch(() => {})
  }

  useEffect(() => {
    loadSkillsAndMatches()
  }, [])

  async function handleAdd(e) {
    e.preventDefault()
    const trimmed = skillName.trim()
    if (!trimmed) return
    setError('')
    setSuccess('')
    try {
      const { data } = await api.post('/api/skills/me', {
        skillName: trimmed,
        type,
        proficiency
      })
      setSkills((prev) => {
        const filtered = prev.filter((item) => item.id !== data.id)
        return [...filtered, data]
      })
      setSkillName('')
      setSuccess(
        `Added "${data.skill?.name || trimmed}" (${
          type === 'TEACH' ? 'I can teach this' : 'I want to learn this'
        }) to your skills list!`
      )
      loadSkillsAndMatches()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not add that skill.')
    }
  }

  async function handleRemove(id) {
    try {
      await api.delete(`/api/skills/me/${id}`)
      setSkills((prev) => prev.filter((s) => s.id !== id))
      loadSkillsAndMatches()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not remove skill.')
    }
  }

  return (
    <div className="page">
      <h1>Your skills</h1>
      <p className="page-subtitle">
        Add skills you can teach and skills you want to learn — they will appear in your skills list and automatically match you with tutors.
      </p>

      <form className="inline-form" onSubmit={handleAdd}>
        <input
          placeholder="Skill name, e.g. Data Structures, Java, DBMS"
          value={skillName}
          onChange={(e) => setSkillName(e.target.value)}
          required
        />
        <select value={type} onChange={(e) => setType(e.target.value)}>
          <option value="TEACH">I can teach this</option>
          <option value="LEARN">I want to learn this</option>
        </select>
        <select value={proficiency} onChange={(e) => setProficiency(e.target.value)}>
          <option value="BEGINNER">Beginner</option>
          <option value="INTERMEDIATE">Intermediate</option>
          <option value="ADVANCED">Advanced</option>
        </select>
        <button type="submit">Add skill</button>
      </form>

      {error && <p className="error">{error}</p>}
      {success && <p className="info">{success}</p>}

      <section className="section">
        <h2>My skills list ({skills.length})</h2>
        <ul className="list">
          {skills.map((s) => (
            <li key={s.id} className="match-row">
              <span>
                <strong>{s.skill?.name || s.skillName}</strong>
              </span>
              <span className="row-actions">
                <span className={`badge ${s.type === 'TEACH' ? 'badge-green' : 'badge-amber'}`}>
                  {s.type === 'TEACH' ? 'Teaching' : 'Want to Learn'} · {String(s.proficiency || '').toLowerCase()}
                </span>
                <button type="button" className="secondary" onClick={() => handleRemove(s.id)}>
                  Remove
                </button>
              </span>
            </li>
          ))}
        </ul>
        {skills.length === 0 && (
          <p className="empty-state">No skills added yet — add one above and it will be printed here.</p>
        )}
      </section>

      <section className="section">
        <div className="row-between">
          <h2>Matches to Learn ({matchesToLearn.length})</h2>
          <Link to="/matches">
            <button type="button" className="secondary">Open Matches Page →</button>
          </Link>
        </div>
        <ul className="list">
          {matchesToLearn.map((m, i) => (
            <li key={i} className="match-row">
              <div>
                <strong>{m.candidateName}</strong> is teaching <strong>{m.skillName}</strong>
                <span className={`badge ${m.matchType === 'MUTUAL' ? 'badge-green' : 'badge-amber'}`}>
                  {m.matchType === 'MUTUAL' ? 'Mutual match' : 'Available Tutor'}
                </span>
              </div>
              <Link to="/matches">
                <button type="button">View & Accept Match</button>
              </Link>
            </li>
          ))}
        </ul>
        {matchesToLearn.length === 0 && (
          <p className="empty-state">
            When you add a skill with &ldquo;I want to learn this&rdquo; and another student adds that skill with &ldquo;I can teach this&rdquo;, they will appear here and in the Matches tab.
          </p>
        )}
      </section>
    </div>
  )
}
