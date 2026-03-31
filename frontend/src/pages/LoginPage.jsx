import { useState } from 'react'
import Card from '../components/common/Card'
import Field from '../components/common/Field'
import Button from '../components/common/Button'
import { api } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'

export default function LoginPage({ navigate }) {
  const { login } = useAuth()
  const toast = useToast()
  const [form, setForm] = useState({ username: '', password: '' })
  const [loading, setLoading] = useState(false)

  const updateField = (key) => (event) => setForm((current) => ({ ...current, [key]: event.target.value }))

  const handleSubmit = async (event) => {
    event.preventDefault()

    try {
      setLoading(true)
      const response = await api.post('/auth/login', form)
      const resolvedUser = response?.user ?? (response?.token ? await api.get('/auth/me', response.token) : null)

      if (!response?.token || !resolvedUser) {
        throw new Error('Login response is missing account details. Please verify the backend auth response.')
      }

      login({ token: response.token, user: resolvedUser })
      toast.success('Login successful', `Welcome back, ${resolvedUser.username}.`)
      navigate(resolvedUser.role === 'ADMIN' ? '/admin' : '/')
    } catch (error) {
      const message =
        error.message?.toLowerCase().includes('invalid') ||
        error.message?.toLowerCase().includes('bad credentials') ||
        error.message?.toLowerCase().includes('unauthorized')
          ? 'Invalid username or password.'
          : error.message

      toast.error('Login failed', message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto grid max-w-5xl gap-8 lg:grid-cols-[1.1fr_0.9fr]">
      <section className="rounded-[2.5rem] bg-slate-950 px-8 py-10 text-white shadow-2xl">
        <p className="text-sm uppercase tracking-[0.35em] text-emerald-300">MVP Frontend</p>
        <h1 className="mt-4 text-4xl font-semibold leading-tight">Run restaurants, menus, uploads, and customer flows from one dashboard.</h1>
        <p className="mt-4 max-w-xl text-sm text-slate-300">
          This frontend is aligned to your Spring backend and emphasizes quick operations, clear navigation, and visible
          feedback with toasts and spinners.
        </p>
      </section>

      <Card title="Login" subtitle="Use your backend username and password to continue.">
        <form className="space-y-4" onSubmit={handleSubmit}>
          <Field label="Username" value={form.username} onChange={updateField('username')} placeholder="Enter username" required />
          <Field label="Password" type="password" value={form.password} onChange={updateField('password')} placeholder="Enter password" required />
          <Button type="submit" loading={loading} className="w-full">
            Login
          </Button>
        </form>
        <p className="mt-4 text-sm text-slate-500">
          New here? <a href="#/signup" className="font-semibold text-emerald-600 transition hover:text-emerald-500 hover:underline">Create an account</a>
        </p>
      </Card>
    </div>
  )
}
