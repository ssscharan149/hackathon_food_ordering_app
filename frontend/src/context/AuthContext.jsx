/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useEffect, useState } from 'react'
import { api } from '../lib/api'

const STORAGE_KEY = 'hackathon-food-auth'
const AuthContext = createContext(null)

function readStoredAuth() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(readStoredAuth)

  useEffect(() => {
    if (auth) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(auth))
    } else {
      localStorage.removeItem(STORAGE_KEY)
    }
  }, [auth])

  const login = (payload) => {
    setAuth({
      token: payload.token,
      user: payload.user,
    })
  }

  const logout = async () => {
    try {
      if (auth?.token) {
        await api.post('/auth/logout', null, auth.token)
      }
    } catch {
      // The backend is stateless, so local logout still clears auth safely.
    } finally {
      setAuth(null)
    }
  }

  const refreshUser = async () => {
    if (!auth?.token) {
      return null
    }

    const user = await api.get('/auth/me', auth.token)
    setAuth((current) => (current ? { ...current, user } : current))
    return user
  }

  const value = {
    token: auth?.token ?? null,
    user: auth?.user ?? null,
    isAuthenticated: Boolean(auth?.token),
    isAdmin: auth?.user?.role === 'ADMIN',
    login,
    logout,
    refreshUser,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }

  return context
}
