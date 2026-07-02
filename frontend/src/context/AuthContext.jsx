import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem('token'))
  const [user, setUser] = useState(
    JSON.parse(localStorage.getItem('user') || 'null')
  )

  const login = (authResponse) => {
    localStorage.setItem('token', authResponse.token)
    localStorage.setItem('user', JSON.stringify({
      email: authResponse.email,
      fullName: authResponse.fullName,
      role: authResponse.role
    }))
    setToken(authResponse.token)
    setUser({
      email: authResponse.email,
      fullName: authResponse.fullName,
      role: authResponse.role
    })
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setToken(null)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ token, user, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}