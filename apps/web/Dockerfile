FROM oven/bun:1-alpine AS build

WORKDIR /workspace
COPY package.json bun.lock ./
COPY apps/web/package.json apps/web/package.json
RUN bun install

COPY apps/web apps/web
RUN bun run --cwd apps/web build

FROM oven/bun:1-alpine

WORKDIR /app
COPY --from=build /workspace/apps/web/src/server.ts src/server.ts
COPY --from=build /workspace/apps/web/dist dist

ENV HOST=0.0.0.0
ENV PORT=3000
EXPOSE 3000

CMD ["bun", "src/server.ts"]
