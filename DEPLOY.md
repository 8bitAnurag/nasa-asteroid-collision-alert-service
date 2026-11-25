# Deploying NASA Asteroid Collision Alert Service (Free Tier)

This repo supports two modes:
- **Local Docker + Kafka** (`ALERT_DELIVERY_MODE=kafka`, default)
- **Cloud Free Tier** (`ALERT_DELIVERY_MODE=http`, configured in `render.yaml`)

## Why Brevo HTTPS API instead of Gmail SMTP on Render Free?
Render Free Web Services block outbound SMTP ports (`25`, `465`, `587`). Using **Brevo's free HTTPS API** (port `443`, 300 emails/day free, no custom domain required) lets your service email any visitor reliably from Render Free.

## 1. Get Free Keys (2 minutes)
1. **Brevo API Key** (free): Create an account at https://www.brevo.com/, verify your Gmail as a sender under **Senders & Domains**, and generate an API key under **SMTP & API -> API Keys**.
2. **NASA API Key** (free): Get a key at https://api.nasa.gov/ (or use `DEMO_KEY` for light testing).

## 2. Deploy Backends to Render (Free Blueprint)
1. Push your repo to GitHub (`8bitAnurag/nasa-asteroid-collision-alert-service`).
2. In **Render Dashboard** -> **New +** -> **Blueprint**, select your GitHub repository.
3. Fill in the prompted environment variables:
   - `BREVO_API_KEY`: your Brevo API key
   - `MAIL_FROM`: your verified sender Gmail (e.g. `anuraggautam3395@gmail.com`)
   - `NASA_API_KEY`: your NASA API key
4. After both services turn **Live**, verify `asteroid-notification-service`'s exact public URL in Render. If Render added a suffix (like `-xyz1`), update `NOTIFICATION_SERVICE_URL` on `asteroid-alerting-service` to match it.
5. Copy the public URL of **`asteroid-alerting-service`**.

## 3. Deploy Frontend to Vercel (Free)
1. In **Vercel** -> **Add New Project**, import `8bitAnurag/nasa-asteroid-collision-alert-service`.
2. Set **Root Directory** to `frontend`.
3. Add Environment Variable:
   - `VITE_API_URL` = your Render `asteroid-alerting-service` URL (no trailing slash).
4. Click **Deploy** and copy your live Vercel URL!
