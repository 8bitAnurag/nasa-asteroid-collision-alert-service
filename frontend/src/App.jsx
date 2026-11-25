import { useState } from 'react'
import './App.css'

const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '')

function App() {
  const [email, setEmail] = useState('')
  const [status, setStatus] = useState('idle') // 'idle' | 'loading' | 'success' | 'error'
  const [message, setMessage] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()

    if (!email.trim()) {
      setStatus('error')
      setMessage('Please enter an email address.')
      return
    }

    try {
      setStatus('loading')
      setMessage('')

      const response = await fetch(`${API_BASE_URL}/api/v1/asteroid-alerting/alerts`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email: email.trim() }),
      })

      if (!response.ok) {
        const errorText = await response.text().catch(() => '')
        throw new Error(errorText || `Request failed with status ${response.status}`)
      }

      setStatus('success')
      setMessage('List of hazardous asteroids has been sent to your inbox.')
      setEmail('')
    } catch (err) {
      setStatus('error')
      setMessage(
        err?.message && !err.message.includes('Failed to fetch')
          ? err.message
          : 'Could not reach the alert service. If the server just woke up, wait a few seconds and try again.'
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
            {status === 'loading' ? 'Checking NASA & Sending...' : 'See Hazardous Asteroids'}
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
