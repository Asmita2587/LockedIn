import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  if (!user) return null

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <nav className="navbar">
      <Link to="/dashboard" className="navbar-brand">
        {/* Drop your logo file at src/assets/logo.png and swap the line below for:
            <img src="/src/assets/logo.png" alt="LockedIn" /> */}
        <span>LockedIn</span>
      </Link>
      <div className="navbar-links">
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/skills">Skills</Link>
        <Link to="/matches">Matches</Link>
        <Link to="/groups">Groups</Link>
        <Link to="/credits">Credits</Link>
      </div>
      <div className="navbar-user">
        <span>{user.name}</span>
        <button onClick={handleLogout}>Log out</button>
      </div>
    </nav>
  )
}
