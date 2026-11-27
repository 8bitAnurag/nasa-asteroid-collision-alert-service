import { useEffect, useState } from 'react'
import './App.css'

const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '')

function formatErrorMessage(rawText, statusCode) {
  const trimmed = (rawText || '').trim()
  const looksLikeHtml =
    trimmed.startsWith('<') ||
    trimmed.includes('<!DOCTYPE') ||
    trimmed.includes('<html') ||
    trimmed.includes('<style')

  if (statusCode === 502 || statusCode === 503 || statusCode === 504 || looksLikeHtml) {
    return 'The free-tier backend is still waking up from sleep. Wait a few seconds and click again!'
  }

  try {
    const parsed = JSON.parse(trimmed)
    if (parsed?.message && !parsed.message.includes('<style') && !parsed.message.includes('<!DOCTYPE')) {
      return parsed.message
    }
  } catch {
    // plain text error
  }

  return trimmed && trimmed.length < 180
    ? trimmed
    : 'The alert service is waking up. Please wait a few seconds and try again.'
}

function App() {
  const [email, setEmail] = useState('')
  const [status, setStatus] = useState('idle') // 'idle' | 'loading' | 'success' | 'error'
  const [message, setMessage] = useState('')
  const [elapsedSec, setElapsedSec] = useState(0)

  // Proactively wake the Render backend as soon as the visitor opens the page
  useEffect(() => {
    fetch(`${API_BASE_URL}/api/v1/asteroid-alerting/health`).catch(() => {})
  }, [])

  useEffect(() => {
    if (status !== 'loading') {
      setElapsedSec(0)
      return
    }
    const start = Date.now()
    const timer = window.setInterval(() => {
      setElapsedSec(Math.floor((Date.now() - start) / 1000))
    }, 1000)
    return () => window.clearInterval(timer)
  }, [status])

  const sendAlertRequest = async (cleanEmail) => {
    return fetch(`${API_BASE_URL}/api/v1/asteroid-alerting/alerts`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email: cleanEmail }),
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

    const cleanEmail = email.trim()
    if (!cleanEmail) {
      setStatus('error')
      setMessage('Please enter an email address.')
      return
    }

    try {
      setStatus('loading')
      setMessage('')

      let response = await sendAlertRequest(cleanEmail)

      // Auto-retry once if Render returned a 502/503/504 cold-start gateway error
      if (response.status === 502 || response.status === 503 || response.status === 504) {
        setMessage('Backend waking up from sleep, retrying automatically...')
        await new Promise((r) => setTimeout(r, 4000))
        response = await sendAlertRequest(cleanEmail)
      }

      if (!response.ok) {
        const errorText = await response.text().catch(() => '')
        throw new Error(formatErrorMessage(errorText, response.status))
      }

      setStatus('success')
      setMessage('List of hazardous asteroids has been sent to your inbox.')
      setEmail('')
    } catch (err) {
      setStatus('error')
      setMessage(
        err?.message && !err.message.includes('Failed to fetch')
          ? err.message
          : 'The free-tier backend is waking up from sleep. Wait a few seconds and click again!'
      )
    }
  }

  return (
    <div className="page">
      <div className="card">
        <h1 className="title">Asteroid Collision Alerts</h1>
        <p className="subtitle">
          Do you want to know about the nearest potentially hazardous asteroids? Enter your email and check inbox.
        </p>

        <form className="form" onSubmit={handleSubmit}>
          <input
            type="email"
            className="input"
            placeholder="youremail@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <button className="button" type="submit" disabled={status === 'loading'}>
            {status === 'loading'
              ? elapsedSec >= 3
                ? `Waking servers & checking NASA (${elapsedSec}s)...`
                : 'Checking NASA & Sending...'
              : 'See Hazardous Asteroids'}
          </button>
        </form>

        {message && (
          <p className={`message ${status === 'success' ? 'message-success' : 'message-error'}`}>
            {message}
          </p>
        )}
      </div>
    </div>
  )
}

export default App
