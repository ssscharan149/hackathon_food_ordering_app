import { useCallback, useEffect, useState } from 'react'
import Card from '../components/common/Card'
import Field from '../components/common/Field'
import Button from '../components/common/Button'
import Spinner from '../components/common/Spinner'
import EmptyState from '../components/common/EmptyState'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { api } from '../lib/api'

const emptyAddress = {
  street: '',
  buildingName: '',
  city: '',
  state: '',
  country: '',
  pincode: '',
}

export default function ProfilePage() {
  const { token, user } = useAuth()
  const toast = useToast()
  const [addresses, setAddresses] = useState([])
  const [form, setForm] = useState(emptyAddress)
  const [editingId, setEditingId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  const loadAddresses = useCallback(async () => {
    try {
      setLoading(true)
      const response = await api.get('/me/addresses', token)
      setAddresses(response ?? [])
    } catch (error) {
      toast.error('Unable to load addresses', error.message)
    } finally {
      setLoading(false)
    }
  }, [toast, token])

  useEffect(() => {
    loadAddresses()
  }, [loadAddresses])

  const updateField = (key) => (event) => setForm((current) => ({ ...current, [key]: event.target.value }))

  const resetForm = () => {
    setForm(emptyAddress)
    setEditingId(null)
  }

  const handleSubmit = async (event) => {
    event.preventDefault()

    try {
      setSaving(true)
      if (editingId) {
        await api.put(`/addresses/${editingId}`, form, token)
        toast.success('Address updated', 'Your address changes were saved.')
      } else {
        await api.post('/me/addresses', form, token)
        toast.success('Address added', 'A new delivery address is ready to use.')
      }
      resetForm()
      await loadAddresses()
    } catch (error) {
      toast.error('Address save failed', error.message)
    } finally {
      setSaving(false)
    }
  }

  const startEditing = (address) => {
    setEditingId(address.addressId)
    setForm(address)
  }

  const deleteAddress = async (addressId) => {
    try {
      await api.delete(`/addresses/${addressId}`, token)
      toast.success('Address removed', 'The address was deleted successfully.')
      await loadAddresses()
    } catch (error) {
      toast.error('Delete failed', error.message)
    }
  }

  return (
    <div className="grid gap-8 xl:grid-cols-[0.9fr_1.1fr]">
      <Card title="Profile" subtitle="Your authenticated account and address book.">
        <div className="space-y-2 rounded-[1.75rem] bg-slate-50 p-5">
          <p className="text-lg font-semibold text-slate-950">{user.username}</p>
          <p className="text-sm text-slate-600">{user.email}</p>
          <p className="text-sm text-slate-600">{user.phone || 'Phone not added yet.'}</p>
          <p className="text-xs uppercase tracking-[0.2em] text-emerald-600">{user.role}</p>
        </div>

        <form className="mt-6 grid gap-4" onSubmit={handleSubmit}>
          <Field label="Street" value={form.street} onChange={updateField('street')} placeholder="Street name" required />
          <Field label="Building name" value={form.buildingName} onChange={updateField('buildingName')} placeholder="Building / landmark" required />
          <div className="grid gap-4 md:grid-cols-2">
            <Field label="City" value={form.city} onChange={updateField('city')} placeholder="City" required />
            <Field label="State" value={form.state} onChange={updateField('state')} placeholder="State" required />
          </div>
          <div className="grid gap-4 md:grid-cols-2">
            <Field label="Country" value={form.country} onChange={updateField('country')} placeholder="Country" required />
            <Field label="Pincode" value={form.pincode} onChange={updateField('pincode')} placeholder="Pincode" required />
          </div>
          <div className="flex flex-wrap gap-3">
            <Button type="submit" loading={saving}>
              {editingId ? 'Update address' : 'Save address'}
            </Button>
            {editingId ? (
              <Button type="button" variant="secondary" onClick={resetForm}>
                Cancel edit
              </Button>
            ) : null}
          </div>
        </form>
      </Card>

      <Card title="Saved addresses" subtitle="Use these details for checkout or order delivery.">
        {loading ? (
          <Spinner label="Loading addresses" />
        ) : addresses.length ? (
          <div className="space-y-4">
            {addresses.map((address) => (
              <article key={address.addressId} className="rounded-[1.75rem] border border-slate-200 bg-slate-50 p-5">
                <p className="text-sm font-medium text-slate-900">
                  {address.buildingName}, {address.street}
                </p>
                <p className="mt-1 text-sm text-slate-500">
                  {address.city}, {address.state}, {address.country} - {address.pincode}
                </p>
                <div className="mt-4 flex gap-3">
                  <Button variant="secondary" onClick={() => startEditing(address)}>
                    Edit
                  </Button>
                  <Button variant="danger" onClick={() => deleteAddress(address.addressId)}>
                    Delete
                  </Button>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <EmptyState title="No saved addresses" description="Add one on the left to speed up ordering." />
        )}
      </Card>
    </div>
  )
}
