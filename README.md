# fullstackapp

Spring Boot + React (JHipster). Login: `admin` / `admin`.

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
