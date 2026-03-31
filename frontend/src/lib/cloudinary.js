export async function uploadImageToCloudinary(file) {
  const cloudName = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME
  const uploadPreset = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET
  const folder = import.meta.env.VITE_CLOUDINARY_FOLDER

  if (!cloudName || !uploadPreset) {
    throw new Error('Cloudinary env is missing. Set VITE_CLOUDINARY_CLOUD_NAME and VITE_CLOUDINARY_UPLOAD_PRESET.')
  }

  const formData = new FormData()
  formData.append('file', file)
  formData.append('upload_preset', uploadPreset)

  if (folder) {
    formData.append('folder', folder)
  }

  const response = await fetch(`https://api.cloudinary.com/v1_1/${cloudName}/image/upload`, {
    method: 'POST',
    body: formData,
  })

  const payload = await response.json()

  if (!response.ok) {
    throw new Error(payload?.error?.message ?? 'Cloudinary upload failed')
  }

  return payload.secure_url
}
