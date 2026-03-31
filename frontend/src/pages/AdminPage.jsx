import { useCallback, useEffect, useState } from 'react'
import Card from '../components/common/Card'
import Field from '../components/common/Field'
import Button from '../components/common/Button'
import Spinner from '../components/common/Spinner'
import EmptyState from '../components/common/EmptyState'
import ImageUploader from '../components/shared/ImageUploader'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { api } from '../lib/api'
import { formatCurrency } from '../lib/format'

const emptyRestaurant = {
  name: '',
  description: '',
  posterUrl: '',
  location: '',
}

const emptyCategory = {
  categoryName: '',
}

const emptyMenuItem = {
  name: '',
  imageUrl: '',
  description: '',
  price: '',
  categoryId: '',
  restaurantId: '',
  isAvailable: true,
}

export default function AdminPage() {
  const { token } = useAuth()
  const toast = useToast()
  const [loading, setLoading] = useState(true)
  const [uploadingRestaurant, setUploadingRestaurant] = useState(false)
  const [uploadingMenuItem, setUploadingMenuItem] = useState(false)
  const [restaurantForm, setRestaurantForm] = useState(emptyRestaurant)
  const [categoryForm, setCategoryForm] = useState(emptyCategory)
  const [menuItemForm, setMenuItemForm] = useState(emptyMenuItem)
  const [editingRestaurantId, setEditingRestaurantId] = useState(null)
  const [editingCategoryId, setEditingCategoryId] = useState(null)
  const [editingMenuItemId, setEditingMenuItemId] = useState(null)
  const [savingSection, setSavingSection] = useState('')
  const [restaurants, setRestaurants] = useState([])
  const [categories, setCategories] = useState([])
  const [menuItems, setMenuItems] = useState([])

  const loadDashboard = useCallback(async () => {
    try {
      setLoading(true)
      const [restaurantResponse, categoryResponse, menuResponse] = await Promise.all([
        api.get('/me/restaurants', token),
        api.get('/public/categories?pageNumber=0&pageSize=100', token),
        api.get('/admin/menuItems?pageNumber=0&pageSize=100', token),
      ])
      setRestaurants(restaurantResponse ?? [])
      setCategories(categoryResponse?.content ?? [])
      setMenuItems(menuResponse?.content ?? [])
    } catch (error) {
      toast.error('Unable to load admin data', error.message)
    } finally {
      setLoading(false)
    }
  }, [toast, token])

  useEffect(() => {
    loadDashboard()
  }, [loadDashboard])

  const updateRestaurantField = (key) => (event) =>
    setRestaurantForm((current) => ({ ...current, [key]: event.target.value }))
  const updateCategoryField = (key) => (event) =>
    setCategoryForm((current) => ({ ...current, [key]: event.target.value }))
  const updateMenuItemField = (key) => (event) =>
    setMenuItemForm((current) => ({
      ...current,
      [key]: key === 'isAvailable' ? event.target.value === 'true' : event.target.value,
    }))

  const resetRestaurant = () => {
    setRestaurantForm(emptyRestaurant)
    setEditingRestaurantId(null)
  }

  const resetCategory = () => {
    setCategoryForm(emptyCategory)
    setEditingCategoryId(null)
  }

  const resetMenuItem = () => {
    setMenuItemForm(emptyMenuItem)
    setEditingMenuItemId(null)
  }

  const saveRestaurant = async (event) => {
    event.preventDefault()

    try {
      setSavingSection('restaurant')
      if (editingRestaurantId) {
        await api.put(`/restaurants/${editingRestaurantId}`, restaurantForm, token)
        toast.success('Restaurant updated', 'Restaurant details were saved successfully.')
      } else {
        await api.post('/me/restaurants', restaurantForm, token)
        toast.success('Restaurant created', 'A new restaurant was added for this admin.')
      }
      resetRestaurant()
      await loadDashboard()
    } catch (error) {
      toast.error('Restaurant save failed', error.message)
    } finally {
      setSavingSection('')
    }
  }

  const saveCategory = async (event) => {
    event.preventDefault()

    try {
      setSavingSection('category')
      if (editingCategoryId) {
        await api.put(`/admin/categories/${editingCategoryId}`, categoryForm, token)
        toast.success('Category updated', 'Category details were saved successfully.')
      } else {
        await api.post('/admin/categories', categoryForm, token)
        toast.success('Category created', 'A new category is ready for menu items.')
      }
      resetCategory()
      await loadDashboard()
    } catch (error) {
      toast.error('Category save failed', error.message)
    } finally {
      setSavingSection('')
    }
  }

  const saveMenuItem = async (event) => {
    event.preventDefault()

    const payload = {
      ...menuItemForm,
      price: Number(menuItemForm.price),
      categoryId: Number(menuItemForm.categoryId),
      restaurantId: Number(menuItemForm.restaurantId),
    }

    try {
      setSavingSection('menu')
      if (editingMenuItemId) {
        await api.put(`/admin/menuItems/${editingMenuItemId}`, payload, token)
        toast.success('Menu item updated', 'Menu item details were saved successfully.')
      } else {
        await api.post(
          `/admin/categories/${payload.categoryId}/restaurants/${payload.restaurantId}/menuItems`,
          payload,
          token,
        )
        toast.success('Menu item created', 'A new menu item is now listed.')
      }
      resetMenuItem()
      await loadDashboard()
    } catch (error) {
      toast.error('Menu item save failed', error.message)
    } finally {
      setSavingSection('')
    }
  }

  const deleteRestaurant = async (restaurantId) => {
    try {
      await api.delete(`/restaurants/${restaurantId}`, token)
      toast.success('Restaurant deleted', 'The restaurant was removed successfully.')
      await loadDashboard()
    } catch (error) {
      toast.error('Delete failed', error.message)
    }
  }

  const deleteCategory = async (categoryId) => {
    try {
      await api.delete(`/admin/categories/${categoryId}`, token)
      toast.success('Category deleted', 'The category was removed successfully.')
      await loadDashboard()
    } catch (error) {
      toast.error('Delete failed', error.message)
    }
  }

  const deleteMenuItem = async (menuItemId) => {
    try {
      await api.delete(`/admin/menuItems/${menuItemId}`, token)
      toast.success('Menu item deleted', 'The menu item was removed successfully.')
      await loadDashboard()
    } catch (error) {
      toast.error('Delete failed', error.message)
    }
  }

  return (
    <div className="space-y-8">
      <section className="rounded-[2.5rem] bg-slate-950 px-8 py-10 text-white">
        <p className="text-sm uppercase tracking-[0.35em] text-emerald-300">Admin Workspace</p>
        <h1 className="mt-4 text-4xl font-semibold leading-tight">Manage restaurants, categories, menu items, and hosted poster images.</h1>
        <p className="mt-4 max-w-3xl text-sm text-slate-300">
          Every create/update action surfaces backend success or error messages in toasts, and image uploads go through Cloudinary before saving the hosted URL.
        </p>
      </section>

      {loading ? (
        <Card title="Admin data" subtitle="Loading restaurant and menu controls.">
          <Spinner label="Loading dashboard" />
        </Card>
      ) : (
        <>
          <div className="grid gap-8 xl:grid-cols-3">
            <Card title="Restaurant form" subtitle="Create or update restaurants for the current admin.">
              <form className="space-y-4" onSubmit={saveRestaurant}>
                <Field label="Name" value={restaurantForm.name} onChange={updateRestaurantField('name')} placeholder="Restaurant name" required />
                <Field label="Description" as="textarea" value={restaurantForm.description} onChange={updateRestaurantField('description')} placeholder="Short description" />
                <Field label="Location" value={restaurantForm.location} onChange={updateRestaurantField('location')} placeholder="City or full address" required />
                <ImageUploader
                  label="Restaurant poster"
                  value={restaurantForm.posterUrl}
                  onChange={(posterUrl) => setRestaurantForm((current) => ({ ...current, posterUrl }))}
                  uploading={uploadingRestaurant}
                  setUploading={setUploadingRestaurant}
                />
                <div className="flex flex-wrap gap-3">
                  <Button type="submit" loading={savingSection === 'restaurant'}>
                    {editingRestaurantId ? 'Update restaurant' : 'Create restaurant'}
                  </Button>
                  {editingRestaurantId ? (
                    <Button type="button" variant="secondary" onClick={resetRestaurant}>
                      Cancel
                    </Button>
                  ) : null}
                </div>
              </form>
            </Card>

            <Card title="Category form" subtitle="Keep categories lightweight for faster menu setup.">
              <form className="space-y-4" onSubmit={saveCategory}>
                <Field label="Category name" value={categoryForm.categoryName} onChange={updateCategoryField('categoryName')} placeholder="Pizza, Drinks, Dessert" required />
                <div className="flex flex-wrap gap-3">
                  <Button type="submit" loading={savingSection === 'category'}>
                    {editingCategoryId ? 'Update category' : 'Create category'}
                  </Button>
                  {editingCategoryId ? (
                    <Button type="button" variant="secondary" onClick={resetCategory}>
                      Cancel
                    </Button>
                  ) : null}
                </div>
              </form>
            </Card>

            <Card title="Menu form" subtitle="Cloudinary-hosted images are stored in the backend imageUrl field.">
              <form className="space-y-4" onSubmit={saveMenuItem}>
                <Field label="Name" value={menuItemForm.name} onChange={updateMenuItemField('name')} placeholder="Menu item name" required />
                <Field label="Description" as="textarea" value={menuItemForm.description} onChange={updateMenuItemField('description')} placeholder="Describe the dish" required />
                <Field label="Price" type="number" min="0.01" step="0.01" value={menuItemForm.price} onChange={updateMenuItemField('price')} placeholder="199" required />
                <label className="block space-y-2">
                  <span className="text-sm font-medium text-slate-700">Restaurant</span>
                  <select
                    value={menuItemForm.restaurantId}
                    onChange={updateMenuItemField('restaurantId')}
                    className="min-h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-emerald-400 focus:bg-white"
                    required
                  >
                    <option value="">Select restaurant</option>
                    {restaurants.map((restaurant) => (
                      <option key={restaurant.restaurantId} value={restaurant.restaurantId}>
                        {restaurant.name}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="block space-y-2">
                  <span className="text-sm font-medium text-slate-700">Category</span>
                  <select
                    value={menuItemForm.categoryId}
                    onChange={updateMenuItemField('categoryId')}
                    className="min-h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-emerald-400 focus:bg-white"
                    required
                  >
                    <option value="">Select category</option>
                    {categories.map((category) => (
                      <option key={category.categoryId} value={category.categoryId}>
                        {category.categoryName}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="block space-y-2">
                  <span className="text-sm font-medium text-slate-700">Availability</span>
                  <select
                    value={String(menuItemForm.isAvailable)}
                    onChange={updateMenuItemField('isAvailable')}
                    className="min-h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-emerald-400 focus:bg-white"
                  >
                    <option value="true">Available</option>
                    <option value="false">Unavailable</option>
                  </select>
                </label>
                <ImageUploader
                  label="Menu image"
                  value={menuItemForm.imageUrl}
                  onChange={(imageUrl) => setMenuItemForm((current) => ({ ...current, imageUrl }))}
                  uploading={uploadingMenuItem}
                  setUploading={setUploadingMenuItem}
                />
                <div className="flex flex-wrap gap-3">
                  <Button type="submit" loading={savingSection === 'menu'}>
                    {editingMenuItemId ? 'Update menu item' : 'Create menu item'}
                  </Button>
                  {editingMenuItemId ? (
                    <Button type="button" variant="secondary" onClick={resetMenuItem}>
                      Cancel
                    </Button>
                  ) : null}
                </div>
              </form>
            </Card>
          </div>

          <div className="grid gap-8 xl:grid-cols-3">
            <Card title="Restaurants" subtitle="Edit poster URLs, descriptions, and locations.">
              {restaurants.length ? (
                <div className="space-y-4">
                  {restaurants.map((restaurant) => (
                    <article key={restaurant.restaurantId} className="rounded-[1.5rem] border border-slate-200 bg-slate-50 p-4">
                      <div className="flex items-start justify-between gap-3">
                        <div>
                          <h3 className="font-semibold text-slate-950">{restaurant.name}</h3>
                          <p className="text-sm text-slate-500">{restaurant.location}</p>
                        </div>
                        <p className="text-xs uppercase tracking-[0.2em] text-slate-400">#{restaurant.restaurantId}</p>
                      </div>
                      {restaurant.posterUrl ? (
                        <img src={restaurant.posterUrl} alt={restaurant.name} className="mt-3 h-28 w-full rounded-2xl object-cover" />
                      ) : null}
                      <div className="mt-4 flex gap-3">
                        <Button
                          variant="secondary"
                          onClick={() => {
                            setEditingRestaurantId(restaurant.restaurantId)
                            setRestaurantForm({
                              name: restaurant.name,
                              description: restaurant.description || '',
                              posterUrl: restaurant.posterUrl || '',
                              location: restaurant.location,
                            })
                          }}
                        >
                          Edit
                        </Button>
                        <Button variant="danger" onClick={() => deleteRestaurant(restaurant.restaurantId)}>
                          Delete
                        </Button>
                      </div>
                    </article>
                  ))}
                </div>
              ) : (
                <EmptyState title="No restaurants yet" description="Use the form above to add your first restaurant." />
              )}
            </Card>

            <Card title="Categories" subtitle="Manage the list used by menu items.">
              {categories.length ? (
                <div className="space-y-4">
                  {categories.map((category) => (
                    <article key={category.categoryId} className="flex items-center justify-between gap-3 rounded-[1.5rem] border border-slate-200 bg-slate-50 p-4">
                      <div>
                        <p className="font-semibold text-slate-950">{category.categoryName}</p>
                        <p className="text-xs uppercase tracking-[0.2em] text-slate-400">#{category.categoryId}</p>
                      </div>
                      <div className="flex gap-2">
                        <Button
                          variant="secondary"
                          onClick={() => {
                            setEditingCategoryId(category.categoryId)
                            setCategoryForm({ categoryName: category.categoryName })
                          }}
                        >
                          Edit
                        </Button>
                        <Button variant="danger" onClick={() => deleteCategory(category.categoryId)}>
                          Delete
                        </Button>
                      </div>
                    </article>
                  ))}
                </div>
              ) : (
                <EmptyState title="No categories yet" description="Create categories like Burgers, Desserts, or Drinks." />
              )}
            </Card>

            <Card title="Menu items" subtitle="Review price, restaurant, category, and availability at a glance.">
              {menuItems.length ? (
                <div className="space-y-4">
                  {menuItems.map((item) => (
                    <article key={item.menuItemId} className="rounded-[1.5rem] border border-slate-200 bg-slate-50 p-4">
                      <div className="flex items-start justify-between gap-3">
                        <div>
                          <h3 className="font-semibold text-slate-950">{item.name}</h3>
                          <p className="text-sm text-slate-500">{formatCurrency(item.price)}</p>
                        </div>
                        <span className={`rounded-full px-3 py-1 text-xs font-semibold ${item.isAvailable ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700'}`}>
                          {item.isAvailable ? 'Available' : 'Unavailable'}
                        </span>
                      </div>
                      {item.imageUrl ? (
                        <img src={item.imageUrl} alt={item.name} className="mt-3 h-28 w-full rounded-2xl object-cover" />
                      ) : null}
                      <p className="mt-3 text-sm text-slate-500">{item.description}</p>
                      <div className="mt-4 flex gap-3">
                        <Button
                          variant="secondary"
                          onClick={() => {
                            setEditingMenuItemId(item.menuItemId)
                            setMenuItemForm({
                              name: item.name,
                              imageUrl: item.imageUrl || '',
                              description: item.description,
                              price: item.price,
                              categoryId: String(item.categoryId),
                              restaurantId: String(item.restaurantId),
                              isAvailable: item.isAvailable,
                            })
                          }}
                        >
                          Edit
                        </Button>
                        <Button variant="danger" onClick={() => deleteMenuItem(item.menuItemId)}>
                          Delete
                        </Button>
                      </div>
                    </article>
                  ))}
                </div>
              ) : (
                <EmptyState title="No menu items yet" description="Create a menu item once you have at least one restaurant and category." />
              )}
            </Card>
          </div>
        </>
      )}
    </div>
  )
}
