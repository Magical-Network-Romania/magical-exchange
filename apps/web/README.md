# Magical Exchange Web

React TSX website built with Bun and served in production by nginx. The first production surface is a compact exchange-rate dashboard and official-rate history view backed by the Spring API.

## Commands

```sh
bun install
bun run --cwd apps/web dev
bun run --cwd apps/web typecheck
bun run --cwd apps/web build
```

- Dev page: `http://localhost:3000`
- Dev history: `http://localhost:3000/history`

The app uses Tailwind and shadcn-style local components in `src/components/ui`.

Production serving happens through nginx in `apps/web/Dockerfile`; Bun is only used for dependency installation and `bun run build`.

Static public assets live in `public/` and are copied into `dist` by `scripts/build.ts`. UI translations live in typed TS modules under `src/i18n/translations`, not in `public/`, so the app does not need to fetch core copy at runtime.
