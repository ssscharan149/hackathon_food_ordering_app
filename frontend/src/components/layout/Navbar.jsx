import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import Button from '../common/Button'
import { classNames } from '../../lib/format'

const links = [
  { path: '/', label: 'Discover', auth: 'all' },
  { path: '/cart', label: 'Cart', auth: 'user' },
  { path: '/orders', label: 'Orders', auth: 'user' },
  { path: '/profile', label: 'Profile', auth: 'user' },
  { path: '/admin', label: 'Admin', auth: 'admin' },
]

export default function Navbar({ route }) {
  const { isAuthenticated, isAdmin, user, logout } = useAuth()
  const toast = useToast()

  const visibleLinks = links.filter((link) => {
    if (link.auth === 'all') return true
    if (link.auth === 'admin') return isAdmin
    return isAuthenticated
  })

  const handleLogout = async () => {
    await logout()
    toast.info('Signed out', 'Your session has been cleared from this browser.')
    window.location.hash = '#/login'
  }

  return (
    <header className="sticky top-0 z-40 border-b border-white/60 bg-white/80 backdrop-blur-xl">
      <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-8">
        <a href="#/" className="flex items-center gap-3">
          <span className="grid h-11 w-11 place-items-center rounded-2xl bg-slate-950 text-lg font-black text-white">
            HF
          </span>
          <div>
            <p className="text-sm uppercase tracking-[0.35em] text-emerald-500">FoodFlow</p>
            <p className="text-lg font-semibold text-slate-950">Restaurant Control Center</p>
          </div>
        </a>

        <nav className="flex flex-wrap items-center gap-2">
          {visibleLinks.map((link) => (
            <a
              key={link.path}
              href={`#${link.path}`}
              className={classNames(
                'rounded-full px-4 py-2 text-sm font-medium transition duration-200',
                route === link.path
                  ? 'bg-emerald-100 text-emerald-900 ring-1 ring-emerald-200'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-950',
              )}
            >
              {link.label}
            </a>
          ))}
        </nav>

        <div className="flex items-center gap-3">
          {isAuthenticated ? (
            <>
              <div className="hidden text-right sm:block">
                <p className="text-sm font-semibold text-slate-900">{user?.username}</p>
                <p className="text-xs uppercase tracking-[0.2em] text-slate-500">{user?.role}</p>
              </div>
              <Button variant="secondary" onClick={handleLogout}>
                Logout
              </Button>
            </>
          ) : (
            <>
              <a
                href="#/login"
                className="rounded-full px-4 py-2 text-sm font-medium text-slate-700 transition duration-200 hover:bg-slate-100 hover:text-slate-950"
              >
                Login
              </a>
              <a
                href="#/signup"
                className="rounded-full bg-emerald-500 px-4 py-2 text-sm font-semibold text-white transition duration-200 hover:bg-emerald-400 hover:shadow-lg"
              >
                Sign up
              </a>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
