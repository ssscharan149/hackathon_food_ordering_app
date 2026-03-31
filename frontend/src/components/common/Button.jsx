import Spinner from './Spinner'
import { classNames } from '../../lib/format'

export default function Button({
  children,
  type = 'button',
  onClick,
  loading = false,
  variant = 'primary',
  className = '',
  disabled = false,
}) {
  const palette = {
    primary: 'bg-slate-950 text-white hover:bg-slate-800 hover:shadow-lg',
    secondary: 'bg-white text-slate-900 ring-1 ring-slate-200 hover:bg-slate-50 hover:ring-slate-300',
    accent: 'bg-emerald-500 text-white hover:bg-emerald-400 hover:shadow-lg',
    danger: 'bg-rose-500 text-white hover:bg-rose-400 hover:shadow-lg',
  }

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled || loading}
      className={classNames(
        'inline-flex min-h-11 items-center justify-center rounded-2xl px-4 py-2 text-sm font-semibold transition duration-200 disabled:cursor-not-allowed disabled:opacity-60',
        palette[variant],
        className,
      )}
    >
      {loading ? <Spinner size="sm" label="Please wait" /> : children}
    </button>
  )
}
