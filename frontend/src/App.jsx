import { useState } from 'react'
import './App.css'

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

      // Calls your Spring Boot controller: POST /api/v1/asteroid-alerting/alerts
      const response = await fetch('http://localhost:8080/api/v1/asteroid-alerting/alerts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email }),
      })

      if (!response.ok) {
        throw new Error('Subscription failed')
      }

      setStatus('success')
      setMessage('List of hazardous asteroids have been sent to your inbox.')
      setEmail('')
    } catch (err) {
      setStatus('error')
      setMessage('Something went wrong. Please try again.')
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
          />
          <button className="button" type="submit" disabled={status === 'loading'}>
            {status === 'loading' ? 'Sending...' : 'See Hazardous Asteroids'}
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
