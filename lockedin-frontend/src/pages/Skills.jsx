import { useEffect, useState } from 'react'
import api from '../api/axios'

export default function Skills() {
  const [skills, setSkills] = useState([])
  const [skillName, setSkillName] = useState('')
  const [type, setType] = useState('TEACH')
  const [proficiency, setProficiency] = useState('INTERMEDIATE')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  function loadSkills() {
    api.get('/api/skills/me')
      .then((res) => {
        setSkills(Array.isArray(res.data) ? res.data : [])
        setError('')
      })
      .catch((err) => setError(err.response?.data?.message || 'Could not load skills (make sure backend is running on port 8080).'))
  }

  useEffect(() => {
    loadSkills()
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
      setSuccess(`Added "${data.skill?.name || trimmed}" (${type === 'TEACH' ? 'Teaching' : 'Learning'}) to your skills list!`)
      loadSkills()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not add that skill.')
    }
  }

  async function handleRemove(id) {
    try {
      await api.delete(`/api/skills/me/${id}`)
      setSkills((prev) => prev.filter((s) => s.id !== id))
    } catch (err) {
      setError(err.response?.data?.message || 'Could not remove skill.')
    }
  }

  return (
    <div className="page">
      <h1>Your skills</h1>
      <p className="page-subtitle">
        Add skills you can teach and skills you want to learn — they will appear in your skills list below.
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
                <span className={`badge ${s.type === 'TEACH' ? 'badge-green' : 'badge-gray'}`}>
                  {s.type === 'TEACH' ? 'Teaching' : 'Learning'} · {String(s.proficiency || '').toLowerCase()}
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
    </div>
  )
}
