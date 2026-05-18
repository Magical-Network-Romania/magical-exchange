# Magical Exchange Web

React TSX smoke-test website served directly by Bun.

## Commands

```sh
bun install
bun run --cwd apps/web typecheck
bun run --cwd apps/web build
bun run --cwd apps/web start
```

- Page: `http://localhost:3000`
- API button: calls `http://localhost:8080/api/ping` locally, or `https://api.<web-domain>/api/ping` on a deployed host.

The app uses Tailwind and shadcn-style local components in `src/components/ui`.
