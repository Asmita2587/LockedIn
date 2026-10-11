import { createContext, useContext, useEffect, useState } from 'react'
import api from '../api/axios'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('lockedin_token')
    const userId = localStorage.getItem('lockedin_userId')
    const name = localStorage.getItem('lockedin_name')
    if (token && userId) {
      setUser({ token, userId: Number(userId), name })
    }
    setLoading(false)
  }, [])

  async function login(email, password) {
    const { data } = await api.post('/api/auth/login', { email, password })
    persist(data)
  }

  async function register(name, email, password) {
    const { data } = await api.post('/api/auth/register', { name, email, password })
    persist(data)
  }

  function persist(data) {
    localStorage.setItem('lockedin_token', data.token)
    localStorage.setItem('lockedin_userId', data.userId)
    localStorage.setItem('lockedin_name', data.name)
    setUser({ token: data.token, userId: data.userId, name: data.name })
  }

  function logout() {
    localStorage.removeItem('lockedin_token')
    localStorage.removeItem('lockedin_userId')
    localStorage.removeItem('lockedin_name')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
