export default function Spinner({ size = 'md', label = 'Loading...' }) {
  const dimensions =
    size === 'sm' ? 'h-4 w-4 border-2' : size === 'lg' ? 'h-10 w-10 border-4' : 'h-6 w-6 border-2'

  return (
    <span className="inline-flex items-center gap-2 text-sm font-medium text-slate-600">
      <span className={`${dimensions} animate-spin rounded-full border-slate-300 border-t-slate-900`} />
      <span>{label}</span>
    </span>
  )
}
