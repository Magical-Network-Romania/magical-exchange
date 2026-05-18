# Magical Exchange

Minimal deployment smoke-test scaffold for Magical Exchange.

The database migrations are still present, but the runtime apps are intentionally tiny right now:

- `services/backend` - Spring Boot API with `GET /api/hello`.
- `apps/web` - React TSX + Tailwind/shadcn-style smoke-test page served directly by Bun.
- `infra/compose.local.yml` - local Docker Compose for PostgreSQL, backend, and web.
- `infra/compose.dokploy.yml` - production Docker Compose for Dokploy.
- `docs/deployment/dokploy-github.md` - production deployment guide for Dokploy's GitHub integration.

## Local Run

```sh
cp .env.example .env
docker compose -f infra/compose.local.yml up --build
```

- Backend: `http://localhost:8080/api/hello`
- Backend ping: `http://localhost:8080/api/ping`
- Backend health: `http://localhost:8080/actuator/health`
- Web: `http://localhost:3000`

## VPS Deployment

Use the Dokploy production Compose file described in `docs/deployment/dokploy-github.md`.
