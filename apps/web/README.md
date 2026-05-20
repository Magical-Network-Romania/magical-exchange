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

The app uses Tailwind and shadcn-style local components in `src/components/ui`.

`bun run start` serves the already-built `dist` directory through `src/server.ts`. Use `bun run dev` for Bun's HTML dev server.
