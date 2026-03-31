import { AuthProvider, useAuth } from './context/AuthContext'
import { ToastProvider } from './context/ToastContext'
import { useHashRoute } from './hooks/useHashRoute'
import AppShell from './components/layout/AppShell'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import AdminPage from './pages/AdminPage'
import CartPage from './pages/CartPage'
import OrdersPage from './pages/OrdersPage'
import ProfilePage from './pages/ProfilePage'

function RouterView() {
  const { route, navigate } = useHashRoute()
  const { isAuthenticated, isAdmin } = useAuth()

  if ((route === '/login' || route === '/signup') && isAuthenticated) {
    navigate('/')
    return null
  }

  if (route === '/admin' && !isAdmin) {
    navigate(isAuthenticated ? '/' : '/login')
    return null
  }

  if ((route === '/cart' || route === '/orders' || route === '/profile') && !isAuthenticated) {
    navigate('/login')
    return null
  }

  const page = (() => {
    switch (route) {
      case '/login':
        return <LoginPage navigate={navigate} />
      case '/signup':
        return <SignupPage navigate={navigate} />
      case '/admin':
        return <AdminPage />
      case '/cart':
        return <CartPage />
      case '/orders':
        return <OrdersPage />
      case '/profile':
        return <ProfilePage />
      default:
        return <HomePage navigate={navigate} />
    }
  })()

  return <AppShell route={route}>{page}</AppShell>
}

export default function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <RouterView />
      </AuthProvider>
    </ToastProvider>
  )
}
