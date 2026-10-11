import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'
import VideoCall from '../components/VideoCall'
import { copyText } from './Groups'

export default function GroupDetail() {
  const { groupId } = useParams()
  const { user } = useAuth()

  const [group, setGroup] = useState(null)
  const [members, setMembers] = useState([])
  const [topics, setTopics] = useState([])
  const [progressMap, setProgressMap] = useState({})
  const [doubts, setDoubts] = useState([])
  const [sessions, setSessions] = useState([])

  const [newTopic, setNewTopic] = useState('')
  const [doubtTitle, setDoubtTitle] = useState('')
  const [doubtDesc, setDoubtDesc] = useState('')
  const [answerDrafts, setAnswerDrafts] = useState({})
  const [sessType, setSessType] = useState('CO_STUDY')
  const [sessTime, setSessTime] = useState('')
  const [activeCall, setActiveCall] = useState(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  function loadAll() {
    api.get(`/api/groups/${groupId}`)
      .then((r) => {
        setGroup(r.data)
        setError('')
      })
      .catch((err) => setError(err.response?.data?.message || 'Could not load this group.'))

    api.get(`/api/groups/${groupId}/members`)
      .then((r) => setMembers(Array.isArray(r.data) ? r.data : []))
      .catch(() => {})

    api.get(`/api/groups/${groupId}/syllabus/topics`)
      .then((r) => setTopics(Array.isArray(r.data) ? r.data : []))
      .catch(() => {})

    api.get(`/api/groups/${groupId}/syllabus/progress`)
      .then((r) => setProgressMap(r.data || {}))
      .catch(() => {})

    api.get(`/api/groups/${groupId}/doubts`)
      .then((r) => setDoubts(Array.isArray(r.data) ? r.data : []))
      .catch(() => {})

    api.get(`/api/groups/${groupId}/sessions`)
      .then((r) => setSessions(Array.isArray(r.data) ? r.data : []))
      .catch(() => {})
  }

  useEffect(() => {
    // Auto-ensure current user is joined in the group so their name shows in members
    api.post(`/api/groups/${groupId}/join`).finally(() => {
      loadAll()
    })
    const t = setInterval(loadAll, 2500)
    return () => clearInterval(t)
  }, [groupId])

  async function handleAddTopic(e) {
    e.preventDefault()
    if (!newTopic.trim()) return
    setError('')
    try {
      const { data } = await api.post(`/api/groups/${groupId}/syllabus/topics`, null, {
        params: { title: newTopic.trim() }
      })
      setTopics((prev) => [...prev, data])
      setNewTopic('')
      setNotice(`Topic "${data.title}" added to shared syllabus!`)
      loadAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not add that topic.')
    }
  }

  async function handleProgress(topicId, status) {
    setError('')
    try {
      await api.put(`/api/groups/${groupId}/syllabus/topics/${topicId}/progress`, null, {
        params: { status }
      })
      setProgressMap((prev) => ({ ...prev, [topicId]: status }))
      loadAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not update your progress.')
    }
  }

  async function handlePostDoubt(e) {
    e.preventDefault()
    if (!doubtTitle.trim()) return
    setError('')
    try {
      const { data } = await api.post(`/api/groups/${groupId}/doubts`, null, {
        params: {
          title: doubtTitle.trim(),
          description: doubtDesc.trim() || undefined
        }
      })
      setDoubts((prev) => [data, ...prev.filter((d) => d.id !== data.id)])
      setDoubtTitle('')
      setDoubtDesc('')
      setNotice('Your doubt has been posted to the Doubt Board!')
      loadAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not post that doubt.')
    }
  }

  async function handleAnswer(doubtId) {
    const content = (answerDrafts[doubtId] || '').trim()
    if (!content) return
    setError('')
    try {
      await api.post(`/api/groups/${groupId}/doubts/${doubtId}/answers`, { content })
      setAnswerDrafts((prev) => ({ ...prev, [doubtId]: '' }))
      setNotice('Solution posted! You earned +2 bonus credits.')
      loadAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not post your answer.')
    }
  }

  async function handleResolve(doubtId) {
    try {
      await api.post(`/api/groups/${groupId}/doubts/${doubtId}/resolve`)
      loadAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not mark doubt as clarified.')
    }
  }

  async function handleBook(e) {
    e.preventDefault()
    setError('')
    try {
      const when = sessTime ? new Date(sessTime) : new Date()
      const { data } = await api.post(`/api/groups/${groupId}/sessions`, null, {
        params: {
          type: sessType,
          scheduledAt: when.toISOString(),
          tutorId: sessType === 'TUTOR' ? user?.userId : undefined,
          videoEnabled: true
        }
      })
      setSessions((prev) => [...prev, data])
      setSessTime('')
      loadAll()
      // Immediately start camera and audio in the group's live room when booked
      setActiveCall({
        room: `group-${groupId}-live`,
        label: `${sessType === 'TUTOR' ? 'Tutor' : 'Co-study'} Session #${data.id}`
      })
    } catch (err) {
      setError(err.response?.data?.message || 'Could not book that session.')
    }
  }

  function joinSessionCall(s) {
    api.post(`/api/groups/${groupId}/sessions/${s.id}/start`).catch(() => {})
    setActiveCall({
      room: `group-${groupId}-live`,
      label: `${s.type === 'TUTOR' ? 'Tutor' : 'Co-study'} Session #${s.id}`
    })
  }

  return (
    <div className="page">
      <Link to="/groups" className="back">← All groups</Link>
      <h1>{group?.subject?.name || `Study Group #${groupId}`}</h1>

      <div className="group-id-bar">
        <span>Shareable Group ID:</span>
        <span className="group-id big">{groupId}</span>
        <button type="button" className="secondary" onClick={() => copyText(groupId)}>
          Copy Group ID
        </button>
        <span className="hint-inline">Share this Group ID with friends so they can join from the Groups page.</span>
      </div>

      {group?.examDate && <p className="page-subtitle">Upcoming Exam Date: {group.examDate}</p>}
      {error && <p className="error">{error}</p>}
      {notice && <p className="info">{notice}</p>}

      <section className="section">
        <h2>Members ({members.length})</h2>
        <ul className="list">
          {members.map((m) => (
            <li key={m.id} className="match-row">
              <span><strong>{m.user?.name || 'Member'}</strong></span>
              <span className={`badge ${m.role === 'CREATOR' ? 'badge-green' : 'badge-gray'}`}>
                {String(m.role || 'MEMBER').toLowerCase()}
              </span>
            </li>
          ))}
        </ul>
      </section>

      <section className="section">
        <h2>Video &amp; audio sessions ({sessions.length})</h2>
        <p className="meta" style={{ marginBottom: '10px' }}>
          Booking a session immediately starts your camera &amp; microphone and lets group members join the live call.
        </p>
        <form className="inline-form" onSubmit={handleBook}>
          <select value={sessType} onChange={(e) => setSessType(e.target.value)}>
            <option value="CO_STUDY">Co-study session</option>
            <option value="TUTOR">Tutor session (I&apos;ll teach)</option>
          </select>
          <input
            type="datetime-local"
            value={sessTime}
            onChange={(e) => setSessTime(e.target.value)}
          />
          <button type="submit">📹 Book &amp; Start Camera/Audio</button>
          <button
            type="button"
            className="secondary"
            onClick={() => setActiveCall({ room: `group-${groupId}-live`, label: 'Instant Group Call' })}
          >
            📹 Join Live Group Call
          </button>
        </form>

        <ul className="list">
          {sessions.map((s) => (
            <li key={s.id} className="match-row">
              <span>
                <strong>{s.type === 'TUTOR' ? 'Tutor session' : 'Co-study session'} #{s.id}</strong>
                <span className="badge badge-gray">
                  {s.scheduledAt ? new Date(s.scheduledAt).toLocaleString() : 'Live now'}
                </span>
                {s.tutor?.name && <span className="badge badge-green">Tutor: {s.tutor.name}</span>}
              </span>
              <button type="button" onClick={() => joinSessionCall(s)}>
                📹 Start / Join Camera &amp; Audio
              </button>
            </li>
          ))}
        </ul>
        {sessions.length === 0 && (
          <p className="empty-state">No sessions booked yet — click &ldquo;Book &amp; Start Camera/Audio&rdquo; above to launch a live video call.</p>
        )}
      </section>

      <section className="section">
        <h2>Doubt board ({doubts.length})</h2>
        <form className="inline-form-column" onSubmit={handlePostDoubt}>
          <input
            placeholder="Post a doubt or question for the group…"
            value={doubtTitle}
            onChange={(e) => setDoubtTitle(e.target.value)}
            required
          />
          <textarea
            placeholder="Describe what you need help clarifying (optional)"
            value={doubtDesc}
            onChange={(e) => setDoubtDesc(e.target.value)}
          />
          <button type="submit">Post doubt</button>
        </form>

        <ul className="list">
          {doubts.map((d) => {
            const answers = Array.isArray(d.answers) ? d.answers : []
            const author = typeof d.postedBy === 'object' ? d.postedBy?.name : d.postedBy
            return (
              <li key={d.id} className="doubt-card">
                <div className="match-row">
                  <div>
                    <strong>{d.title}</strong>
                    <span className={`badge ${d.resolved ? 'badge-green' : 'badge-gray'}`}>
                      {d.resolved ? 'Clarified ✓' : 'Open doubt'}
                    </span>
                  </div>
                  {!d.resolved && (
                    <button type="button" className="secondary" onClick={() => handleResolve(d.id)}>
                      ✓ Mark Clarified
                    </button>
                  )}
                </div>
                <span className="meta">
                  Asked by <strong>{author || 'Member'}</strong>
                  {d.createdAt ? ` · ${new Date(d.createdAt).toLocaleTimeString()}` : ''}
                </span>
                {d.description && <p>{d.description}</p>}

                {answers.length > 0 ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', marginTop: '6px' }}>
                    {answers.map((a) => (
                      <div key={a.id} className="answer">
                        <b>{a.answeredBy || 'Peer'}:</b> {a.content}
                      </div>
                    ))}
                  </div>
                ) : (
                  <span className="meta">No solutions posted yet — clarify this doubt below!</span>
                )}

                <div className="inline-form">
                  <input
                    placeholder="Write a solution or clarification…"
                    value={answerDrafts[d.id] || ''}
                    onChange={(e) => setAnswerDrafts((prev) => ({ ...prev, [d.id]: e.target.value }))}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') {
                        e.preventDefault()
                        handleAnswer(d.id)
                      }
                    }}
                  />
                  <button type="button" onClick={() => handleAnswer(d.id)}>
                    Solve &amp; Clarify (+2 credits)
                  </button>
                </div>
              </li>
            )
          })}
        </ul>
        {doubts.length === 0 && (
          <p className="empty-state">No doubts posted yet — post a doubt above so group members can solve and clarify it.</p>
        )}
      </section>

      <section className="section">
        <h2>Shared syllabus ({topics.length})</h2>
        <form className="inline-form" onSubmit={handleAddTopic}>
          <input
            placeholder="Add a syllabus topic, e.g. Normalization &amp; B-Trees"
            value={newTopic}
            onChange={(e) => setNewTopic(e.target.value)}
            required
          />
          <button type="submit">Add topic</button>
        </form>
        <ul className="list">
          {topics.map((t) => {
            const currentStatus = progressMap[t.id] || 'NOT_STARTED'
            return (
              <li key={t.id} className="match-row">
                <span>
                  <strong>{t.title}</strong>
                  <span className={`badge ${currentStatus === 'DONE' ? 'badge-green' : 'badge-gray'}`}>
                    {currentStatus === 'DONE'
                      ? 'Done ✓'
                      : currentStatus === 'IN_PROGRESS'
                        ? 'In progress'
                        : 'Not started'}
                  </span>
                </span>
                <select
                  value={currentStatus}
                  onChange={(e) => handleProgress(t.id, e.target.value)}
                >
                  <option value="NOT_STARTED">Not started</option>
                  <option value="IN_PROGRESS">In progress</option>
                  <option value="DONE">Done</option>
                </select>
              </li>
            )
          })}
        </ul>
        {topics.length === 0 && <p className="empty-state">No syllabus topics added yet.</p>}
      </section>

      {activeCall && (
        <VideoCall
          room={activeCall.room}
          label={activeCall.label}
          name={user?.name || 'Student'}
          onLeave={() => setActiveCall(null)}
        />
      )}
    </div>
  )
}
