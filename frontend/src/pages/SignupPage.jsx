import { useState } from 'react'
import Card from '../components/common/Card'
import Field from '../components/common/Field'
import Button from '../components/common/Button'
import { api } from '../lib/api'
import { useToast } from '../context/ToastContext'

const initialForm = {
  username: '',
  email: '',
  password: '',
  phone: '',
}

export default function SignupPage({ navigate }) {
  const toast = useToast()
  const [form, setForm] = useState(initialForm)
  const [loading, setLoading] = useState(false)

  const updateField = (key) => (event) => setForm((current) => ({ ...current, [key]: event.target.value }))

  const handleSubmit = async (event) => {
    event.preventDefault()

    try {
      setLoading(true)
      const response = await api.post('/auth/signup', form)
      toast.success('Signup successful', response?.message ?? 'You can log in now.')
      navigate('/login')
    } catch (error) {
      toast.error('Signup failed', error.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto max-w-2xl">
      <Card title="Create account" subtitle="Pick a user role and connect to the backend immediately.">
        <form className="grid gap-4 md:grid-cols-2" onSubmit={handleSubmit}>
          <Field label="Username" value={form.username} onChange={updateField('username')} placeholder="johndoe" required />
          <Field label="Email" type="email" value={form.email} onChange={updateField('email')} placeholder="john@example.com" required />
          <Field label="Password" type="password" value={form.password} onChange={updateField('password')} placeholder="At least 6 characters" required />
          <Field label="Phone" value={form.phone} onChange={updateField('phone')} placeholder="9876543210" />
          <Button type="submit" loading={loading} className="md:col-span-2">
            Create account
          </Button>
        </form>
      </Card>
    </div>
  )
}
