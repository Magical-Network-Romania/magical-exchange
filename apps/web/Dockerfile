FROM oven/bun:1-alpine AS build

WORKDIR /workspace
COPY package.json bun.lock ./
COPY apps/web/package.json apps/web/package.json
RUN bun install

COPY apps/web apps/web
RUN bun run --cwd apps/web build

FROM nginx:stable-alpine

COPY apps/web/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/apps/web/dist /usr/share/nginx/html

EXPOSE 80
