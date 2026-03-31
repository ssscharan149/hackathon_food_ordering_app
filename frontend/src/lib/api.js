const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'

async function request(path, options = {}, token) {
  const hasJsonBody = options.body != null && !(options.body instanceof FormData)
  const config = {
    method: options.method ?? 'GET',
    headers: {
      ...(hasJsonBody ? { 'Content-Type': 'application/json' } : {}),
      ...(options.headers ?? {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: options.body,
  }

  const response = await fetch(`${API_BASE_URL}${path}`, config)
  const text = await response.text()
  let payload = null

  if (text) {
    try {
      payload = JSON.parse(text)
    } catch {
      payload = text
    }
  }

  if (!response.ok) {
    const message =
      payload?.message ||
      payload?.error ||
      (typeof payload === 'string' ? payload : 'Request failed')
    const details = Array.isArray(payload?.details) ? payload.details.join(', ') : ''
    throw new Error(details ? `${message}: ${details}` : message)
  }

  return payload
}

export const api = {
  get: (path, token) => request(path, { method: 'GET' }, token),
  post: (path, body, token) =>
    request(
      path,
      {
        method: 'POST',
        body: body == null ? null : JSON.stringify(body),
      },
      token,
    ),
  put: (path, body, token) =>
    request(
      path,
      {
        method: 'PUT',
        body: body == null ? null : JSON.stringify(body),
      },
      token,
    ),
  delete: (path, token) => request(path, { method: 'DELETE' }, token),
}
