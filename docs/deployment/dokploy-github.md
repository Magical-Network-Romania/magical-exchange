# Dokploy GitHub Deployment

This project should deploy through Dokploy directly from GitHub. No GitHub Actions workflow is needed for the current setup.

Dokploy will clone the repository, build the backend and web Docker images from the Dockerfiles, and run the full stack from `infra/compose.dokploy.yml`.

## Recommended Shape

Create one Dokploy project and one Dokploy service:

- Project: `exchange`
- Service type: `Compose`
- Service name: `magical-exchange` or `exchange-stack`
- Compose type: `Docker Compose`

That single Compose service runs three containers internally:

- `database` - PostgreSQL with the persistent `database_data` Docker volume.
- `backend` - Spring Boot API on container port `8080`.
- `web` - Bun-served React site on container port `3000`.

Do not create three Dokploy services for the current version. Separate Dokploy services are useful later if you want Dokploy's built-in database service, independent scaling, or separate deployments, but the one-Compose-service setup is simpler and matches this repository.

Choose `Docker Compose`, not `Stack`. Stack is for Docker Swarm and is not the right fit for this simple single-server deployment.

## Files Used By Dokploy

- `infra/compose.dokploy.yml` - production Compose file.
- `services/backend/Dockerfile` - backend image build.
- `apps/web/Dockerfile` - web image build.
- `.env.example` - environment variable template.

`infra/compose.local.yml` is only for local development.

## DNS

Create these DNS records before or shortly after the first deploy:

```text
exchange.magical.md  A  <your-vps-ip>
```

Add `AAAA` records too if the VPS has IPv6.

## Create The Dokploy Service

In Dokploy:

1. Open the `exchange` project.
2. Create a new service.
3. Select `Compose`.
4. Set the Compose type to `Docker Compose`.
5. Select provider `GitHub`.
6. Select this repository.
7. Select branch `main`.
8. Set Compose Path to:

```text
infra/compose.dokploy.yml
```

## Environment Variables

In the Dokploy Compose service Environment tab, add:

```env
POSTGRES_DB=magical_exchange
POSTGRES_USER=magical_exchange
POSTGRES_PASSWORD=<long-random-production-password>
```

Keep `POSTGRES_PASSWORD` stable after the first deployment. Changing it later does not automatically update the password inside an existing PostgreSQL data volume.

The local port variables from `.env.example` are not needed in Dokploy. Dokploy routes domains to container ports internally.

## Domains

In the Dokploy Compose service Domains tab:

1. Add `exchange.magical.md`.
2. Route it to service `web`, port `3000`.
3. Add `exchange.magical.md` again with path `/api`.
4. Route the `/api` path to service `backend`, port `8080`.

Do not expose the database with a domain.

The result should be:

```text
https://exchange.magical.md/      -> web:3000
https://exchange.magical.md/api   -> backend:8080
```

The numbers `3000` and `8080` are container ports. In production, users should access the app through HTTPS domains, not through public VPS ports.

## Auto Deploy

Enable Auto Deploy in the Dokploy service.

With GitHub connected as the provider, Dokploy can redeploy on pushes to the configured branch. Make sure the branch in Dokploy is exactly `main`; otherwise Dokploy can reject deploys because the pushed branch does not match.

After this, the normal flow is:

1. Push to GitHub `main`.
2. Dokploy receives the GitHub event.
3. Dokploy clones the repository.
4. Dokploy builds `backend` and `web`.
5. Dokploy starts or updates `database`, `backend`, and `web`.

You can also click Deploy manually from Dokploy for the first deployment or when testing configuration changes.

## Flyway

Flyway runs from the backend application. There is no separate Flyway service.

On backend startup:

1. PostgreSQL starts.
2. The backend waits for the PostgreSQL health check.
3. Spring Boot starts.
4. Flyway applies SQL files from `services/backend/src/main/resources/db/migration`.
5. The API starts serving requests.

## Database Health Issues

If deployment fails with this message:

```text
dependency failed to start: container ...-database-1 is unhealthy
```

open the `database` container logs in Dokploy first. The backend depends on a healthy database, so the backend will not start until this is fixed.

The most common causes are:

- The first PostgreSQL initialization took longer than expected on the VPS.
- `POSTGRES_DB`, `POSTGRES_USER`, or `POSTGRES_PASSWORD` is missing in the Dokploy environment values.
- A previous deployment already created the `database_data` volume with different database/user values.
- A previous deployment created a Postgres 18 volume using the old `/var/lib/postgresql/data` mount path.

For a first test deployment where there is no real data yet, the fastest fix for an old or broken volume is to delete the Compose service's `database_data` volume from Dokploy/Docker and redeploy. Do not delete that volume after real data exists unless you have a backup.

To inspect from the VPS shell:

```sh
docker logs <database-container-name>
docker inspect <database-container-name> --format '{{json .State.Health}}'
```

If the logs say the database directory already exists, PostgreSQL is reusing the existing volume and will ignore new `POSTGRES_*` initialization values. Keep the old values, manually create the missing DB/user, or reset the volume if this is only a test deployment.

Postgres 18 Docker images expect the named volume to be mounted at `/var/lib/postgresql`, not `/var/lib/postgresql/data`. The Compose files already use the Postgres 18-compatible mount path. If an earlier deployment created a volume with the old path, reset that test volume before redeploying.

## Built-In Database Alternative

Dokploy's built-in PostgreSQL service is also valid, but it is not the current repository setup.

If you use the built-in database later, the shape changes to:

- One Dokploy database service for PostgreSQL.
- One Compose service for `backend` and `web`, or two separate Application services.
- Backend environment variables set from the database credentials Dokploy gives you.

For now, keep PostgreSQL inside `infra/compose.dokploy.yml`. It keeps the first deployment atomic, gives the backend a stable hostname of `database`, and lets Compose handle the database health check.

## References

- Dokploy Auto Deploy: https://docs.dokploy.com/docs/core/auto-deploy
- Dokploy Docker Compose: https://docs.dokploy.com/docs/core/docker-compose
- Dokploy Docker Compose Example: https://docs.dokploy.com/docs/core/docker-compose/example
