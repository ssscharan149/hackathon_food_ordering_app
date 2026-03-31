export default function Field({
  label,
  value,
  onChange,
  placeholder,
  type = 'text',
  required = false,
  as = 'input',
  min,
  step,
}) {
  const sharedProps = {
    value,
    onChange,
    placeholder,
    required,
    min,
    step,
    className:
      'min-h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-emerald-400 focus:bg-white',
  }

  return (
    <label className="block space-y-2">
      <span className="text-sm font-medium text-slate-700">{label}</span>
      {as === 'textarea' ? <textarea rows={4} {...sharedProps} /> : <input type={type} {...sharedProps} />}
    </label>
  )
}
