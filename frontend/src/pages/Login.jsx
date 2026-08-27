import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth.jsx'
import Message from '../components/Message.jsx'

export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [greska, setGreska] = useState('')
  const { login } = useAuth()
  const navigate = useNavigate()

  async function posalji(event) {
    event.preventDefault()
    setGreska('')
    try {
      await login(email, password)
      navigate('/kursevi')
    } catch (err) {
      setGreska('Pogresna email adresa ili lozinka')
    }
  }

  return (
    <div className="card login-box">
      <h2>Prijava</h2>
      <Message text={greska} type="error" />
      <form onSubmit={posalji}>
        <div style={{ marginBottom: 12 }}>
          <label>Email</label>
          <input value={email} onChange={(e) => setEmail(e.target.value)} required />
        </div>
        <div style={{ marginBottom: 16 }}>
          <label>Lozinka</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </div>
        <button type="submit">Prijavi se</button>
      </form>
    </div>
  )
}
