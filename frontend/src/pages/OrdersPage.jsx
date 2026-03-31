import { useCallback, useEffect, useState } from 'react'
import Card from '../components/common/Card'
import Field from '../components/common/Field'
import Button from '../components/common/Button'
import Spinner from '../components/common/Spinner'
import EmptyState from '../components/common/EmptyState'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { api } from '../lib/api'
import { formatCurrency } from '../lib/format'

export default function OrdersPage() {
  const { token } = useAuth()
  const toast = useToast()
  const [orders, setOrders] = useState([])
  const [deliveryAddress, setDeliveryAddress] = useState('')
  const [loading, setLoading] = useState(true)
  const [placingOrder, setPlacingOrder] = useState(false)

  const loadOrders = useCallback(async () => {
    try {
      setLoading(true)
      const response = await api.get('/me/orders', token)
      setOrders(response ?? [])
    } catch (error) {
      toast.error('Unable to load orders', error.message)
    } finally {
      setLoading(false)
    }
  }, [toast, token])

  useEffect(() => {
    loadOrders()
  }, [loadOrders])

  const handlePlaceOrder = async (event) => {
    event.preventDefault()

    try {
      setPlacingOrder(true)
      const response = await api.post('/me/orders', { deliveryAddress }, token)
      setOrders((current) => [response, ...current])
      setDeliveryAddress('')
      toast.success('Order placed', `Order #${response.orderId} was created successfully.`)
    } catch (error) {
      toast.error('Order failed', error.message)
    } finally {
      setPlacingOrder(false)
    }
  }

  return (
    <div className="grid gap-8 xl:grid-cols-[0.9fr_1.1fr]">
      <Card title="Place order" subtitle="The backend will create an order from the current authenticated cart.">
        <form className="space-y-4" onSubmit={handlePlaceOrder}>
          <Field
            label="Delivery address"
            as="textarea"
            value={deliveryAddress}
            onChange={(event) => setDeliveryAddress(event.target.value)}
            placeholder="Flat, street, city, state, country, pincode"
            required
          />
          <Button type="submit" loading={placingOrder} className="w-full">
            Place order from cart
          </Button>
        </form>
      </Card>

      <Card title="Order history" subtitle="All orders for the current authenticated user.">
        {loading ? (
          <Spinner label="Loading orders" />
        ) : orders.length ? (
          <div className="space-y-4">
            {orders.map((order) => (
              <article key={order.orderId} className="rounded-[1.75rem] border border-slate-200 bg-slate-50 p-5">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <h3 className="text-lg font-semibold text-slate-950">Order #{order.orderId}</h3>
                    <p className="text-sm text-slate-500">{order.restaurantName || 'Restaurant assigned by backend cart state'}</p>
                  </div>
                  <span className="rounded-full bg-white px-3 py-1 text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">
                    {order.orderStatus}
                  </span>
                </div>
                <p className="mt-3 text-sm text-slate-600">{order.deliveryAddress}</p>
                <div className="mt-4 space-y-2">
                  {order.orderItems.map((item) => (
                    <div key={item.orderItemId} className="flex items-center justify-between gap-3 text-sm text-slate-700">
                      <span>{item.menuItemName} x {item.quantity}</span>
                      <span>{formatCurrency(item.price)}</span>
                    </div>
                  ))}
                </div>
                <p className="mt-4 text-right text-base font-semibold text-slate-950">Total: {formatCurrency(order.totalAmount)}</p>
              </article>
            ))}
          </div>
        ) : (
          <EmptyState title="No orders yet" description="Place your first order once your cart has items." />
        )}
      </Card>
    </div>
  )
}
