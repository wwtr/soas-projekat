import { createContext, useContext, useState } from 'react'
import axios from 'axios'
import { GATEWAY_URL } from './api.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = sessionStorage.getItem('soas-user')
    return stored ? JSON.parse(stored) : null
  })

  async function login(email, password) {
    const response = await axios.get(`${GATEWAY_URL}/login`, {
      headers: { Authorization: 'Basic ' + btoa(`${email}:${password}`) }
    })
    const logged = { email, password, role: response.data.role }
    sessionStorage.setItem('soas-user', JSON.stringify(logged))
    setUser(logged)
    return logged
  }

  function logout() {
    sessionStorage.removeItem('soas-user')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
