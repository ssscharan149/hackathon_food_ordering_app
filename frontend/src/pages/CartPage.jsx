import { useCallback, useEffect, useState } from 'react'
import Card from '../components/common/Card'
import Button from '../components/common/Button'
import Spinner from '../components/common/Spinner'
import EmptyState from '../components/common/EmptyState'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { api } from '../lib/api'
import { formatCurrency } from '../lib/format'

export default function CartPage() {
  const { token } = useAuth()
  const toast = useToast()
  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(true)
  const [updatingId, setUpdatingId] = useState(null)

  const loadCart = useCallback(async () => {
    try {
      setLoading(true)
      const response = await api.get('/carts/users/cart', token)
      setCart(response)
    } catch (error) {
      toast.error('Unable to load cart', error.message)
    } finally {
      setLoading(false)
    }
  }, [toast, token])

  useEffect(() => {
    loadCart()
  }, [loadCart])

  const updateQuantity = async (menuItemId, operation) => {
    try {
      setUpdatingId(menuItemId)
      const response = await api.put(`/cart/menuItems/${menuItemId}/quantity/${operation}`, null, token)
      setCart(response)
      toast.success('Cart updated', 'The quantity was refreshed successfully.')
    } catch (error) {
      toast.error('Cart update failed', error.message)
    } finally {
      setUpdatingId(null)
    }
  }

  const removeItem = async (menuItemId) => {
    try {
      setUpdatingId(menuItemId)
      await api.delete(`/carts/${cart.cartId}/menuItems/${menuItemId}`, token)
      toast.success('Item removed', 'The menu item was removed from the cart.')
      await loadCart()
    } catch (error) {
      toast.error('Remove failed', error.message)
    } finally {
      setUpdatingId(null)
    }
  }

  return (
    <Card title="Cart" subtitle="Review cart items and adjust quantities before placing an order.">
      {loading ? (
        <Spinner label="Loading cart" />
      ) : cart?.cartItems?.length ? (
        <div className="space-y-4">
          {cart.cartItems.map((item) => (
            <article key={item.cartItemId} className="grid gap-4 rounded-[1.75rem] border border-slate-200 bg-slate-50 p-4 md:grid-cols-[120px_1fr_auto] md:items-center">
              <img
                src={item.imageUrl || 'https://placehold.co/300x200/f8fafc/0f172a?text=Cart'}
                alt={item.menuItemName}
                className="h-28 w-full rounded-[1.25rem] object-cover"
              />
              <div>
                <h3 className="text-lg font-semibold text-slate-950">{item.menuItemName}</h3>
                <p className="mt-1 text-sm text-slate-500">Unit price: {formatCurrency(item.productPrice)}</p>
                <p className="mt-1 text-sm text-slate-500">Quantity: {item.quantity}</p>
              </div>
              <div className="flex flex-wrap gap-2">
                <Button variant="secondary" loading={updatingId === item.menuItemId} onClick={() => updateQuantity(item.menuItemId, 'add')}>
                  +1
                </Button>
                <Button variant="secondary" loading={updatingId === item.menuItemId} onClick={() => updateQuantity(item.menuItemId, 'delete')}>
                  -1
                </Button>
                <Button variant="danger" loading={updatingId === item.menuItemId} onClick={() => removeItem(item.menuItemId)}>
                  Remove
                </Button>
              </div>
            </article>
          ))}
          <div className="flex justify-end border-t border-slate-200 pt-4">
            <p className="text-lg font-semibold text-slate-950">Total: {formatCurrency(cart.totalPrice)}</p>
          </div>
        </div>
      ) : (
        <EmptyState title="Your cart is empty" description="Browse menu items from the Discover page and add something delicious." />
      )}
    </Card>
  )
}
