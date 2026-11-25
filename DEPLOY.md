# Deploying NASA Asteroid Collision Alert Service (Free Tier)

This repo is configured to run in two modes:
- **Local Docker + Kafka** (`ALERT_DELIVERY_MODE=kafka`, default)
- **Cloud Free Tier** (`ALERT_DELIVERY_MODE=http`, wired automatically via `render.yaml`)

## 1. Deploy Backends to Render (Free Docker Blueprint)
1. Push your updated repo to GitHub (`8bitAnurag/nasa-asteroid-collision-alert-service`).
2. In **Render Dashboard** -> **New +** -> **Blueprint**, select your GitHub repository.
3. Render detects `render.yaml` and creates two services:
   - `asteroid-notification-service`
   - `asteroid-alerting-service`
4. Fill in the prompt values:
   - `NASA_API_KEY`: Your free key from https://api.nasa.gov/ (or `DEMO_KEY` for light testing)
   - `MAIL_USERNAME`: Your Gmail address (e.g. `anuraggautam3395@gmail.com`)
   - `MAIL_PASSWORD`: Your 16-character **Google App Password** (https://support.google.com/accounts/answer/185833)
5. Once deployed, copy the URL of **`asteroid-alerting-service`** (e.g. `https://asteroid-alerting-service.onrender.com`).

> Note: Render passes internal hostnames or URLs via `fromService`; if `NOTIFICATION_SERVICE_URL` resolves without `https://`, ensure it has `https://` in Render env vars or use the `*.onrender.com` public URL.

## 2. Deploy Frontend to Vercel (Free)
1. In **Vercel** -> **Add New Project**, import `8bitAnurag/nasa-asteroid-collision-alert-service`.
2. Set **Root Directory** to `frontend`.
3. Add Environment Variable:
   - `VITE_API_URL` = `https://asteroid-alerting-service.onrender.com` (your Render URL, no trailing slash).
4. Click **Deploy** and copy your live Vercel URL (e.g. `https://nasa-asteroid-alerts.vercel.app`).

## 3. Connect Your Portfolio
Update `liveUrl` for the NASA Asteroid project in your portfolio's `src/data/profile.ts` with your new Vercel URL!
