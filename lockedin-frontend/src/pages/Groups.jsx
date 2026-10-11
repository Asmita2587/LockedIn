import { useEffect, useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api from '../api/axios'

export function copyText(text) {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(String(text)).catch(() => {})
  }
  alert('Copied Group ID: ' + text + ' — share this ID with friends so they can join!')
}

export default function Groups() {
  const navigate = useNavigate()
  const [subjectName, setSubjectName] = useState('')
  const [examDate, setExamDate] = useState('')
  const [joinGroupId, setJoinGroupId] = useState('')
  const [groups, setGroups] = useState([])
  const [createdGroup, setCreatedGroup] = useState(null)
  const [error, setError] = useState('')

  function loadGroups() {
    api.get('/api/groups')
      .then((res) => {
        setGroups(Array.isArray(res.data) ? res.data : [])
        setError('')
      })
      .catch((err) => setError(err.response?.data?.message || 'Could not load your groups.'))
  }

  useEffect(() => {
    loadGroups()
  }, [])

  async function handleCreate(e) {
    e.preventDefault()
    setError('')
    try {
      const { data } = await api.post('/api/groups', null, {
        params: {
          subjectName: subjectName.trim(),
          examDate: examDate || undefined
        }
      })
      setCreatedGroup(data)
      setGroups((prev) => [data, ...prev.filter((g) => g.id !== data.id)])
      setSubjectName('')
      setExamDate('')
      loadGroups()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create that group.')
    }
  }

  async function handleJoin(e) {
    e.preventDefault()
    setError('')
    const id = joinGroupId.trim().replace(/[^0-9]/g, '')
    if (!id) {
      setError('Please enter a valid numeric Group ID (e.g. 1).')
      return
    }
    try {
      await api.post(`/api/groups/${id}/join`)
      setJoinGroupId('')
      navigate(`/groups/${id}`)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not join that group. Check the Group ID.')
    }
  }

  return (
    <div className="page">
      <h1>Study groups</h1>
      <p className="page-subtitle">
        Create a group for a subject to get a shareable Group ID, or enter a friend&apos;s Group ID to join.
      </p>
      {error && <p className="error">{error}</p>}

      {createdGroup && (
        <div className="info" style={{ marginBottom: '20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '12px' }}>
          <div>
            <strong>Group created for &ldquo;{createdGroup.subject?.name}&rdquo;!</strong> Share this Group ID so others can join:{' '}
            <span className="group-id" style={{ marginLeft: '8px' }}>ID: {createdGroup.id}</span>
          </div>
          <div className="row-actions">
            <button type="button" className="secondary" onClick={() => copyText(createdGroup.id)}>
              Copy Group ID ({createdGroup.id})
            </button>
            <Link className="btn-link" to={`/groups/${createdGroup.id}`}>
              Open Group →
            </Link>
          </div>
        </div>
      )}

      <div className="card-grid">
        <form className="card" onSubmit={handleCreate}>
          <h3>Create a group</h3>
          <label>Subject name</label>
          <input
            placeholder="e.g. DBMS, Operating Systems"
            value={subjectName}
            onChange={(e) => setSubjectName(e.target.value)}
            required
          />
          <label>Exam date (optional)</label>
          <input
            type="date"
            value={examDate}
            onChange={(e) => setExamDate(e.target.value)}
          />
          <button type="submit">Create group &amp; generate ID</button>
        </form>

        <form className="card" onSubmit={handleJoin}>
          <h3>Join a group by ID</h3>
          <label>Enter shared Group ID</label>
          <input
            placeholder="e.g. 1"
            value={joinGroupId}
            onChange={(e) => setJoinGroupId(e.target.value)}
            required
          />
          <button type="submit">Join group</button>
        </form>
      </div>

      <section className="section">
        <h2>My groups ({groups.length})</h2>
        <ul className="list">
          {groups.map((g) => (
            <li key={g.id} className="match-row">
              <span>
                <strong>{g.subject?.name || `Group #${g.id}`}</strong>
                {g.examDate && <span className="badge badge-gray">Exam: {g.examDate}</span>}
              </span>
              <span className="row-actions">
                <span className="group-id">Group ID: {g.id}</span>
                <button type="button" className="secondary" onClick={() => copyText(g.id)}>
                  Copy ID
                </button>
                <Link className="btn-link" to={`/groups/${g.id}`}>
                  Open →
                </Link>
              </span>
            </li>
          ))}
        </ul>
        {groups.length === 0 && (
          <p className="empty-state">No groups yet — create one above to get a shareable Group ID.</p>
        )}
      </section>
    </div>
  )
}
