# Magical Exchange Web

React TSX website served directly by Bun. The first production surface is a compact exchange-rate dashboard and official-rate history view backed by the Spring API.

## Commands

```sh
bun install
bun run --cwd apps/web typecheck
bun run --cwd apps/web build
bun run --cwd apps/web start
```

- Page: `http://localhost:3000`
- History: `http://localhost:3000/history`

The app uses Tailwind and shadcn-style local components in `src/components/ui`.

`bun run start` serves the already-built `dist` directory through `src/server.ts`. Use `bun run dev` for Bun's HTML dev server.
