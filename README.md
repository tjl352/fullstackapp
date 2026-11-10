# fullstackapp

Spring Boot + React (JHipster) **monolith** (original). Login: `admin` / `admin`.

https://fullstackapp-qwj8.onrender.com/

## Split into two projects (learning)

| Project | Path | Role |
|---------|------|------|
| **API** | [`../fullstackapp-api`](../fullstackapp-api) | Spring only |
| **UI** | [`../fullstackapp-ui`](../fullstackapp-ui) | React → `SERVER_API_URL` |

This folder stays as the reference monolith.

## Learning: Phase 1 — separate UI from API

**Step 2 (done in config):** the API can run without hosting React.

| Piece | What it does |
|-------|----------------|
| `jhipster.cors` in `application-prod.yml` | Allows a browser on another origin (e.g. `localhost:9000`) to call `/api/**` |
| Spring profile `api` (`application-api.yml`) | API-only: do not serve `classpath:/static` SPA |
| `CORS_ALLOWED_ORIGINS` env | Comma-separated UI origins for prod deploy |

Run API-only (dev):

```bash
./mvnw -Dspring-boot.run.profiles=dev,api -Dskip.installnodenpm -Dskip.npm
```

You should see a log like `API-only profile active`. `/api/authenticate` still works; opening `http://localhost:8080/` will **not** show the React app (that’s intentional).

**Next (Step 3):** point React `SERVER_API_URL` at `http://localhost:8080/` and run `npm start` separately.

### Step 3 — Point React at the API

`webpack/environment.js` now reads `SERVER_API_URL` from the environment (baked in at webpack build/start time). Axios uses that as `baseURL`.

| Mode | Command |
|------|---------|
| Monolith-style (relative `/api`, webpack proxy) | `npm start` (leave `SERVER_API_URL` unset) |
| Split UI → API on `:8080` | `npm run start:split` (sets `SERVER_API_URL=http://localhost:8080/`) |
| API-only Spring | `npm run backend:start:api` |

**Learning point:** empty `SERVER_API_URL` = same origin (or proxy). Absolute URL = browser talks to Spring **directly**; CORS on the API is required (Step 2).

Must end with `/` (JHipster convention).

**Next (Step 4):** run API with `dev,api` and UI with `SERVER_API_URL=…` in two terminals and log in.

## Run locally

```bash
./mvnw
```

Open http://localhost:8080. Default DB is H2. For Postgres in Docker:

```bash
./mvnw -Dspring-boot.run.profiles=dev-postgres-docker
```

Optional live-reload for the UI: `./npmw start`.

## Chat

Copy `src/main/resources/config/application-local.yml.example` to `application-local.yml` and add a [Groq](https://console.groq.com/keys) API key.

## Deploy (Render + Supabase)

Push this repo, then create a Docker **Web Service** on [Render](https://render.com). Use Supabase **session pooler** (port 5432) and set:

| Variable | Value |
|----------|--------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<pooler-host>:5432/postgres?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | `postgres.<project-ref>` |
| `SPRING_DATASOURCE_PASSWORD` | Supabase DB password |
| `GROQ_API_KEY` | Groq API key |
| `JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET` | `openssl rand -base64 64` |
| `JHIPSTER_MAIL_BASE_URL` | `https://<service>.onrender.com` |
