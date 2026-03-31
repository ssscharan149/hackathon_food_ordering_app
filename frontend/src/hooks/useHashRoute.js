import { useEffect, useState } from 'react'

const normalizeRoute = () => {
  const hash = window.location.hash.replace(/^#/, '')
  return hash || '/'
}

export function useHashRoute() {
  const [route, setRoute] = useState(normalizeRoute)

  useEffect(() => {
    if (!window.location.hash) {
      window.location.hash = '#/'
    }

    const onHashChange = () => setRoute(normalizeRoute())
    window.addEventListener('hashchange', onHashChange)
    return () => window.removeEventListener('hashchange', onHashChange)
  }, [])

  const navigate = (path) => {
    window.location.hash = `#${path}`
  }

  return { route, navigate }
}
