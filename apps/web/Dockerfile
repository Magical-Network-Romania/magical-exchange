FROM oven/bun:1-alpine AS build

WORKDIR /workspace
COPY package.json bun.lock ./
COPY apps/web/package.json apps/web/package.json
RUN bun install

COPY apps/web apps/web
RUN bun run --cwd apps/web build

FROM oven/bun:1-alpine

WORKDIR /app
COPY --from=build /workspace/apps/web/bunfig.toml bunfig.toml
COPY --from=build /workspace/apps/web/package.json package.json
COPY --from=build /workspace/bun.lock bun.lock
COPY --from=build /workspace/node_modules node_modules
COPY --from=build /workspace/apps/web/src src
COPY --from=build /workspace/apps/web/dist dist

ENV PORT=3000
EXPOSE 3000

CMD ["bun", "src/index.html"]
