import { useRef } from 'react'
import Button from '../common/Button'
import Spinner from '../common/Spinner'
import { uploadImageToCloudinary } from '../../lib/cloudinary'
import { useToast } from '../../context/ToastContext'

export default function ImageUploader({ label, value, onChange, uploading, setUploading }) {
  const inputRef = useRef(null)
  const toast = useToast()

  const onFileChange = async (event) => {
    const file = event.target.files?.[0]
    if (!file) return

    try {
      setUploading(true)
      const url = await uploadImageToCloudinary(file)
      onChange(url)
      toast.success('Image uploaded', 'Cloudinary returned a hosted image URL.')
    } catch (error) {
      toast.error('Upload failed', error.message)
    } finally {
      setUploading(false)
      event.target.value = ''
    }
  }

  return (
    <div className="space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="text-sm font-medium text-slate-700">{label}</p>
          <p className="text-xs text-slate-500">Upload a poster/image to Cloudinary and save the returned URL.</p>
        </div>
        <Button variant="secondary" onClick={() => inputRef.current?.click()} loading={uploading}>
          Select file
        </Button>
      </div>
      <input ref={inputRef} type="file" accept="image/*" className="hidden" onChange={onFileChange} />
      {uploading && <Spinner label="Uploading to Cloudinary" />}
      {value ? (
        <div className="overflow-hidden rounded-[1.5rem] border border-slate-200 bg-slate-50">
          <img src={value} alt={label} className="h-52 w-full object-cover" />
          <div className="border-t border-slate-200 px-4 py-3 text-xs break-all text-slate-500">{value}</div>
        </div>
      ) : null}
    </div>
  )
}
