# Por Trás do Clique

Uma aplicação que expõe em tempo real o que acontece por trás de uma requisição HTTP.

## Stack

- **Backend:** Spring Boot 3 · PostgreSQL · Redis · RabbitMQ · Resilience4j
- **Frontend:** React · Vite · Tailwind CSS
- **Infra:** Docker · Nginx · VPS própria

## Como rodar localmente
```bash
docker compose up -d
cd api && ./mvnw spring-boot:run
cd web && npm run dev
```
