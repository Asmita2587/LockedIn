import { useEffect, useRef, useState } from 'react'
import api from '../api/axios'

const ICE = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' }
  ]
}

function Video({ stream, muted }) {
  const ref = useRef(null)
  useEffect(() => {
    const el = ref.current
    if (!el) return
    el.srcObject = stream || null
    if (stream) {
      el.play().catch(() => {})
    }
  }, [stream])
  return <video ref={ref} autoPlay playsInline muted={muted} />
}

export default function VideoCall({ room, label, name, onLeave }) {
  const [localStream, setLocalStream] = useState(null)
  const [tiles, setTiles] = useState({})
  const [status, setStatus] = useState('Starting camera and microphone…')
  const [micOn, setMicOn] = useState(true)
  const [camOn, setCamOn] = useState(true)
  const streamRef = useRef(null)

  useEffect(() => {
    let cancelled = false
    const clientId = Math.random().toString(36).slice(2, 10)
    const path = `/api/signal/${encodeURIComponent(room)}`
    const pcs = {}
    const pending = {}
    const names = {}
    const peerStreams = {}
    let stream = null
    let since = 0
    let timer = null
    let busy = false
    let chain = Promise.resolve()

    function send(msg) {
      chain = chain
        .then(() => api.post(path, { from: clientId, name, ...msg }))
        .catch(() => {})
      return chain
    }

    function updatePeerTile(peerId) {
      const tracks = peerStreams[peerId] ? peerStreams[peerId].getTracks() : []
      const freshStream = tracks.length > 0 ? new MediaStream(tracks) : null
      setTiles((prev) => ({
        ...prev,
        [peerId]: {
          name: names[peerId] || 'Peer',
          stream: freshStream
        }
      }))
    }

    function removePeer(peerId) {
      if (pcs[peerId]) {
        pcs[peerId].close()
        delete pcs[peerId]
      }
      delete pending[peerId]
      delete peerStreams[peerId]
      setTiles((prev) => {
        const next = { ...prev }
        delete next[peerId]
        return next
      })
    }

    function makePc(peerId) {
      if (pcs[peerId]) pcs[peerId].close()
      const pc = new RTCPeerConnection(ICE)
      pcs[peerId] = pc
      if (!pending[peerId]) pending[peerId] = []
      peerStreams[peerId] = new MediaStream()

      const tracks = stream ? stream.getTracks() : []
      tracks.forEach((t) => pc.addTrack(t, stream))
      if (!tracks.some((t) => t.kind === 'video')) {
        pc.addTransceiver('video', { direction: 'recvonly' })
      }
      if (!tracks.some((t) => t.kind === 'audio')) {
        pc.addTransceiver('audio', { direction: 'recvonly' })
      }

      pc.onicecandidate = (e) => {
        if (e.candidate) {
          send({
            to: peerId,
            type: 'ice',
            data: e.candidate.toJSON ? e.candidate.toJSON() : e.candidate
          })
        }
      }

      pc.ontrack = (e) => {
        if (!peerStreams[peerId]) {
          peerStreams[peerId] = new MediaStream()
        }
        if (e.streams && e.streams[0]) {
          e.streams[0].getTracks().forEach((tr) => {
            if (!peerStreams[peerId].getTracks().some((existing) => existing.id === tr.id)) {
              peerStreams[peerId].addTrack(tr)
            }
          })
        } else if (e.track) {
          if (!peerStreams[peerId].getTracks().some((existing) => existing.id === e.track.id)) {
            peerStreams[peerId].addTrack(e.track)
          }
        }
        updatePeerTile(peerId)
        setStatus(`Connected with ${names[peerId] || 'peer'} — camera & audio live`)
      }

      pc.onconnectionstatechange = () => {
        if (pc.connectionState === 'connected') {
          setStatus(`Connected with ${names[peerId] || 'peer'} — camera & audio live`)
        } else if (pc.connectionState === 'disconnected' || pc.connectionState === 'failed') {
          removePeer(peerId)
        }
      }

      return pc
    }

    async function flush(peerId) {
      const pc = pcs[peerId]
      const q = pending[peerId] || []
      while (pc && pc.remoteDescription && q.length > 0) {
        const cand = q.shift()
        await pc.addIceCandidate(new RTCIceCandidate(cand)).catch(() => {})
      }
    }

    async function handle(m) {
      const p = m.from
      if (!p || p === clientId) return
      if (m.name) names[p] = m.name

      if (m.type === 'hello') {
        updatePeerTile(p)
        const pc = makePc(p)
        const offer = await pc.createOffer()
        await pc.setLocalDescription(offer)
        await send({
          to: p,
          type: 'offer',
          data: { type: pc.localDescription.type, sdp: pc.localDescription.sdp }
        })
      } else if (m.type === 'offer') {
        const existing = pcs[p]
        if (existing && existing.signalingState === 'have-local-offer' && clientId > p) {
          return
        }
        updatePeerTile(p)
        const pc = makePc(p)
        await pc.setRemoteDescription(new RTCSessionDescription(m.data))
        await flush(p)
        const answer = await pc.createAnswer()
        await pc.setLocalDescription(answer)
        await send({
          to: p,
          type: 'answer',
          data: { type: pc.localDescription.type, sdp: pc.localDescription.sdp }
        })
      } else if (m.type === 'answer') {
        const pc = pcs[p]
        if (pc && pc.signalingState === 'have-local-offer') {
          await pc.setRemoteDescription(new RTCSessionDescription(m.data))
          await flush(p)
        }
      } else if (m.type === 'ice') {
        const pc = pcs[p]
        if (!pending[p]) pending[p] = []
        if (pc && pc.remoteDescription) {
          await pc.addIceCandidate(new RTCIceCandidate(m.data)).catch(() => {})
        } else {
          pending[p].push(m.data)
        }
      } else if (m.type === 'bye') {
        removePeer(p)
      }
    }

    async function tick() {
      if (busy || cancelled) return
      busy = true
      try {
        const { data } = await api.get(path, { params: { client: clientId, since } })
        since = data.next
        for (const m of data.msgs || []) {
          try {
            await handle(m)
          } catch (e) {
            console.warn('WebRTC signaling error:', e)
          }
        }
      } catch (e) {
        // retry on next tick
      }
      busy = false
    }

    async function start() {
      try {
        stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true })
        setStatus('Camera & microphone active — waiting for peers to join…')
      } catch (e1) {
        try {
          stream = await navigator.mediaDevices.getUserMedia({ audio: true })
          setCamOn(false)
          setStatus('Microphone active (camera busy/unavailable) — waiting for peers…')
        } catch (e2) {
          stream = null
          setCamOn(false)
          setMicOn(false)
          setStatus(
            window.isSecureContext
              ? 'Joined call (no local camera/mic detected) — you can still see and hear peers.'
              : 'Open http://localhost:5173 to allow camera & microphone access.'
          )
        }
      }

      if (cancelled) {
        if (stream) stream.getTracks().forEach((t) => t.stop())
        return
      }

      streamRef.current = stream
      setLocalStream(stream)

      const { data } = await api.get(path, { params: { client: clientId, since: -1 } })
      since = data.next
      await send({ type: 'hello' })
      timer = setInterval(tick, 700)
    }

    start().catch((e) => setStatus('Could not start call: ' + (e.message || e)))

    return () => {
      cancelled = true
      clearInterval(timer)
      api.post(path, { from: clientId, name, type: 'bye' }).catch(() => {})
      Object.values(pcs).forEach((pc) => pc.close())
      if (stream) stream.getTracks().forEach((t) => t.stop())
    }
  }, [room, name])

  function toggleMic() {
    const s = streamRef.current
    if (!s) return
    s.getAudioTracks().forEach((t) => {
      t.enabled = !t.enabled
    })
    setMicOn((v) => !v)
  }

  function toggleCam() {
    const s = streamRef.current
    if (!s) return
    s.getVideoTracks().forEach((t) => {
      t.enabled = !t.enabled
    })
    setCamOn((v) => !v)
  }

  const peers = Object.entries(tiles)

  return (
    <div className="call-overlay">
      <div className="call-status">
        <strong>{label || room}</strong> · {status} · {peers.length} peer(s) connected
      </div>
      <div className="call-grid">
        <div className="tile">
          <Video stream={localStream} muted={true} />
          <span>You ({name}) {micOn ? '🎤' : '🔇'} {camOn ? '📷' : '🚫'}</span>
        </div>
        {peers.map(([id, t]) => (
          <div className="tile" key={id}>
            <Video stream={t.stream} muted={false} />
            <span>{t.name} 🔊</span>
          </div>
        ))}
      </div>
      <div className="call-controls">
        <button type="button" onClick={toggleMic}>
          {micOn ? '🎤 Mute Audio' : '🔇 Unmute Audio'}
        </button>
        <button type="button" onClick={toggleCam}>
          {camOn ? '📷 Turn Camera Off' : '📷 Turn Camera On'}
        </button>
        <button type="button" className="danger" onClick={onLeave}>
          Leave Call
        </button>
      </div>
    </div>
  )
}
