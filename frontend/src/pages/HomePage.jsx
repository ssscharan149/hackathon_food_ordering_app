import { useEffect, useMemo, useState } from 'react'
import Card from '../components/common/Card'
import Button from '../components/common/Button'
import Spinner from '../components/common/Spinner'
import EmptyState from '../components/common/EmptyState'
import { api } from '../lib/api'
import { formatCurrency } from '../lib/format'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'

export default function HomePage({ navigate }) {
  const { token, isAuthenticated } = useAuth()
  const toast = useToast()
  const [loading, setLoading] = useState(true)
  const [busyId, setBusyId] = useState(null)
  const [restaurants, setRestaurants] = useState([])
  const [categories, setCategories] = useState([])
  const [menuItems, setMenuItems] = useState([])
  const [filters, setFilters] = useState({
    restaurantId: 'all',
    categoryId: 'all',
    keyword: '',
  })

  useEffect(() => {
    if (!isAuthenticated) {
      setLoading(false)
      return
    }

    const load = async () => {
      try {
        setLoading(true)
        const [restaurantsResponse, categoriesResponse, menuResponse] = await Promise.all([
          api.get('/restaurants', token),
          api.get('/public/categories?pageNumber=0&pageSize=100', token),
          api.get('/public/menuItems?pageNumber=0&pageSize=100', token),
        ])
        setRestaurants(restaurantsResponse ?? [])
        setCategories(categoriesResponse?.content ?? [])
        setMenuItems(menuResponse?.content ?? [])
      } catch (error) {
        toast.error('Unable to load catalog', error.message)
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [isAuthenticated, toast, token])

  const filteredItems = useMemo(() => {
    return menuItems.filter((item) => {
      const matchesRestaurant =
        filters.restaurantId === 'all' || String(item.restaurantId) === String(filters.restaurantId)
      const matchesCategory =
        filters.categoryId === 'all' || String(item.categoryId) === String(filters.categoryId)
      const matchesKeyword =
        !filters.keyword ||
        item.name.toLowerCase().includes(filters.keyword.toLowerCase()) ||
        item.description.toLowerCase().includes(filters.keyword.toLowerCase())

      return matchesRestaurant && matchesCategory && matchesKeyword
    })
  }, [filters, menuItems])

  const handleAddToCart = async (menuItemId) => {
    if (!isAuthenticated) {
      toast.info('Login required', 'Please sign in before adding items to your cart.')
      navigate('/login')
      return
    }

    try {
      setBusyId(menuItemId)
      await api.post(`/carts/menuItems/${menuItemId}/quantity/1`, null, token)
      toast.success('Added to cart', 'The item was added successfully.')
    } catch (error) {
      toast.error('Cart update failed', error.message)
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div className="space-y-8">
      <section className="grid gap-6 lg:grid-cols-[1.15fr_0.85fr]">
        <div className="rounded-[2.5rem] bg-slate-950 px-8 py-10 text-white shadow-[0_40px_120px_-50px_rgba(15,23,42,0.9)]">
          <p className="text-sm uppercase tracking-[0.35em] text-amber-300">Restaurant Marketplace</p>
          <h1 className="mt-4 max-w-3xl text-4xl font-semibold leading-tight">A focused Tailwind MVP for browsing menus and managing operations.</h1>
          <p className="mt-4 max-w-2xl text-sm text-slate-300">
            Browse restaurants, filter categories, add menu items to cart, and switch into admin workflows from the same app shell.
          </p>
          {!isAuthenticated ? (
            <div className="mt-6 flex flex-wrap gap-3">
              <Button onClick={() => navigate('/login')}>Login to continue</Button>
              <a
                href="#/signup"
                className="inline-flex min-h-11 items-center justify-center rounded-2xl border border-white/20 px-4 py-2 text-sm font-semibold text-white transition duration-200 hover:bg-white/10 hover:border-white/40"
              >
                Create account
              </a>
            </div>
          ) : null}
        </div>
        <Card title="Quick filters" subtitle="Search by restaurant, category, or keyword.">
          {isAuthenticated ? (
            <div className="space-y-4">
              <input
                value={filters.keyword}
                onChange={(event) => setFilters((current) => ({ ...current, keyword: event.target.value }))}
                placeholder="Search menu items"
                className="min-h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-emerald-400 focus:bg-white"
              />
              <select
                value={filters.restaurantId}
                onChange={(event) => setFilters((current) => ({ ...current, restaurantId: event.target.value }))}
                className="min-h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-emerald-400 focus:bg-white"
              >
                <option value="all">All restaurants</option>
                {restaurants.map((restaurant) => (
                  <option key={restaurant.restaurantId} value={restaurant.restaurantId}>
                    {restaurant.name}
                  </option>
                ))}
              </select>
              <select
                value={filters.categoryId}
                onChange={(event) => setFilters((current) => ({ ...current, categoryId: event.target.value }))}
                className="min-h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-emerald-400 focus:bg-white"
              >
                <option value="all">All categories</option>
                {categories.map((category) => (
                  <option key={category.categoryId} value={category.categoryId}>
                    {category.categoryName}
                  </option>
                ))}
              </select>
            </div>
          ) : (
            <EmptyState
              title="Authentication required"
              description="The current backend security config protects even the public catalog endpoints, so sign in first to load restaurants and menu items."
            />
          )}
        </Card>
      </section>

      <Card title="Restaurants" subtitle="All restaurant records from the backend.">
        {loading ? (
          <Spinner label="Loading restaurants" />
        ) : !isAuthenticated ? (
          <EmptyState title="Catalog locked" description="Login or sign up to fetch restaurant data from the backend." />
        ) : restaurants.length ? (
          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
            {restaurants.map((restaurant) => (
              <article key={restaurant.restaurantId} className="overflow-hidden rounded-[1.75rem] border border-slate-200 bg-slate-50">
                {restaurant.posterUrl ? (
                  <img src={restaurant.posterUrl} alt={restaurant.name} className="h-44 w-full object-cover" />
                ) : (
                  <div className="grid h-44 place-items-center bg-gradient-to-br from-amber-100 via-white to-emerald-100 text-sm font-semibold text-slate-500">
                    Poster pending
                  </div>
                )}
                <div className="space-y-2 p-5">
                  <div className="flex items-start justify-between gap-3">
                    <h3 className="text-lg font-semibold text-slate-950">{restaurant.name}</h3>
                    <span className="rounded-full bg-white px-3 py-1 text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">
                      {restaurant.createdByUsername}
                    </span>
                  </div>
                  <p className="text-sm text-slate-500">{restaurant.description || 'No description added yet.'}</p>
                  <p className="text-sm font-medium text-slate-700">{restaurant.location}</p>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <EmptyState title="No restaurants found" description="Create one from the admin area once you log in as an admin." />
        )}
      </Card>

      <Card title="Menu items" subtitle="Public listing with add-to-cart actions and backend-driven availability.">
        {loading ? (
          <Spinner label="Loading menu items" />
        ) : !isAuthenticated ? (
          <EmptyState title="Login to browse" description="Once authenticated, this page will fetch categories, restaurants, and menu items from the backend." />
        ) : filteredItems.length ? (
          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
            {filteredItems.map((item) => (
              <article key={item.menuItemId} className="overflow-hidden rounded-[1.75rem] border border-slate-200 bg-white">
                <img
                  src={item.imageUrl || 'https://placehold.co/600x400/f8fafc/0f172a?text=Menu+Item'}
                  alt={item.name}
                  className="h-48 w-full object-cover"
                />
                <div className="space-y-3 p-5">
                  <div className="flex items-start justify-between gap-3">
                    <h3 className="text-lg font-semibold text-slate-950">{item.name}</h3>
                    <span className={`rounded-full px-3 py-1 text-xs font-semibold ${item.isAvailable ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700'}`}>
                      {item.isAvailable ? 'Available' : 'Unavailable'}
                    </span>
                  </div>
                  <p className="text-sm text-slate-500">{item.description}</p>
                  <p className="text-base font-semibold text-slate-950">{formatCurrency(item.price)}</p>
                  <Button loading={busyId === item.menuItemId} onClick={() => handleAddToCart(item.menuItemId)} className="w-full" disabled={!item.isAvailable}>
                    Add to cart
                  </Button>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <EmptyState title="No matching menu items" description="Try changing the filters or add menu items from the admin area." />
        )}
      </Card>
    </div>
  )
}
