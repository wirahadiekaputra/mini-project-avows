Run both microservices with Docker (Windows PowerShell)

This repository contains two Spring Boot microservices: `user-service` and `order-service`.
The provided `docker-compose.yml` builds and runs both services plus a Postgres database for each.

Quick run (PowerShell):

1) Open PowerShell and go to the repo root:

   Set-Location -Path 'C:\Project-Avows\spring-microservices-demo'

2) Build images and start the stack:

   docker-compose up -d --build

3) Check containers and logs:

   docker-compose ps
   docker-compose logs --tail=200 user-service
   docker-compose logs --tail=200 order-service
   docker-compose logs --tail=200 -f order-service -> for realtime running
   docker-compose logs --tail=200 -f user-service -> for realtime running

4) Health checks (from host):

   # user-service -> http://localhost:8081/actuator/health
   # order-service -> http://localhost:8082/actuator/health

   # Using PowerShell's Invoke-WebRequest to show the body
   (Invoke-WebRequest -UseBasicParsing http://localhost:8081/actuator/health).Content
   (Invoke-WebRequest -UseBasicParsing http://localhost:8082/actuator/health).Content

Troubleshooting

- UnknownHostException for postgres-user or postgres-order:
  Ensure containers are on a shared network. Run `docker network ls` and `docker network inspect microservices-net`.
  If the services were started before the network existed, run `docker-compose down` then `docker-compose up -d --build` to recreate them.

- Database connection refused or authentication errors:
  Verify environment variables: SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD in `docker-compose.yml`.
  Postgres containers are exposed on host ports 5433 (user) and 5434 (order) for local access.

- Logs are the fastest way to diagnose failures:
  docker-compose logs --tail=300 user-service
  docker-compose logs --tail=300 order-service
  docker-compose logs postgres-user
  docker-compose logs postgres-order

Notes & next steps

- The Dockerfiles copy pre-built JARs from each service's `target/` directory. Make sure you've built both services with Maven (from each service folder):

   .\mvnw clean package -DskipTests

  Or run at repo root if you prefer:

   Set-Location -Path '.\user-service'; .\mvnw clean package -DskipTests; Set-Location -Path '..\order-service'; .\mvnw clean package -DskipTests

- Consider adding a shared configuration (Spring profiles) or an API gateway for inter-service calls.
- Add docker-compose healthchecks for Postgres and the Spring apps (already present). Adjust start_period if necessary.

If you want, I can:
- Add a single script `run-docker.ps1` to run the build and startup commands.
- Add a health / smoke-test script that waits for both /actuator/health endpoints to report UP.

Quick reference — host vs container DB

- Use your local Postgres (recommended for development when you already have the DB):
   - The compose file sets app services to use host.docker.internal by default. This resolves to your Windows host when running Docker Desktop.
   - Verify that your local Postgres listens on the network (check `postgresql.conf`: `listen_addresses = '*'` or includes `localhost`) and that `pg_hba.conf` allows connections from your Docker bridge / host.
   - Make sure the DB names and credentials match `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` in `docker-compose.yml`.

- Use Dockerized Postgres (self-contained stack):
   - Compose defines two Postgres services (ports 5433 and 5434 mapped to containers). If you prefer to use them, change `SPRING_DATASOURCE_URL` in the app services to point to `postgres-user:5432` or `postgres-order:5432` and ensure the username/password match the POSTGRES_* env values.
   - Note: SQL files mounted into `/docker-entrypoint-initdb.d/` run only when the database volume is empty (first-time init).

In-container checks (run these if you want to debug connectivity from inside the container):

1) Exec into the running container (PowerShell):

    docker exec -it spring-microservices-demo-user-service-1 sh

2) From inside the container try:

    # Ping the host (may be limited depending on the image)
    ping host.docker.internal

    # Check the Spring actuator (from inside container, localhost:8080):
    curl -f http://localhost:8080/actuator/health

3) If you need to test Postgres directly and there's no psql in the image, run a temporary client on the host network:

    docker run --rm -it --network host postgres:15 psql -h host.docker.internal -U postgres -d USER_SERVICE

Helpful troubleshooting tips

- If you see 'The underlying connection was closed' from PowerShell when calling an endpoint, try:
   - curl -v http://localhost:8081/v1/users/active (shows raw TCP/HTTP exchange)
   - Confirm the container is still running: `docker ps` and `docker-compose logs --tail=200 user-service`.
   - Check host firewall/antivirus — sometimes outbound/inbound connections originating from Docker are blocked.

- If you see authentication failure from Postgres (FATAL: password authentication failed):
   - Double-check SPRING_DATASOURCE_USERNAME and SPRING_DATASOURCE_PASSWORD match the POSTGRES_USER/POSTGRES_PASSWORD (if using the Docker Postgres image) or your local DB credentials.
   - Remember that Postgres authentication also depends on `pg_hba.conf` entries and the host the connection comes from.

Small helper scripts (optional)

1) run-docker.ps1 (suggested): build and start the stack, then show health endpoints

```powershell
Set-Location -Path "$PSScriptRoot"
docker-compose up -d --build
Start-Sleep -Seconds 6
Write-Host "user-service health:"; (Invoke-WebRequest -UseBasicParsing http://localhost:8081/actuator/health).Content
Write-Host "order-service health:"; (Invoke-WebRequest -UseBasicParsing http://localhost:8082/actuator/health).Content
```

2) smoke-test.ps1 (suggested): wait for both services to be UP (simple loop)

```powershell
function Wait-ForUp($url, $timeoutSec=60) {
   $start = Get-Date
   while ((Get-Date) - $start -lt (New-TimeSpan -Seconds $timeoutSec)) {
      try {
         $r = Invoke-WebRequest -UseBasicParsing $url -ErrorAction Stop
         $body = $r.Content | ConvertFrom-Json
         if ($body.status -eq 'UP') { return $true }
      } catch { }
      Start-Sleep -Seconds 2
   }
   return $false
}

if (-not (Wait-ForUp 'http://localhost:8081/actuator/health' 60)) { Write-Error "user-service not UP"; exit 1 }
if (-not (Wait-ForUp 'http://localhost:8082/actuator/health' 60)) { Write-Error "order-service not UP"; exit 1 }
Write-Host "Both services are UP"
```

If you'd like, I can add these two PowerShell scripts to the repo (`run-docker.ps1` and `smoke-test.ps1`) and finish the remaining todo items: testing DB connectivity from inside the container and verifying `order-service` endpoints. Let me know which step to run next and I'll proceed.

Recent changes I made (and actions you may need)

The following edits were performed so the services work smoothly inside Docker Compose. Only run the commands below if you want to rebuild the image or apply the updated code/config locally.

- Updated `order-service` Feign client to use a configurable property and default to the compose service host:
   - File changed: `order-service/src/main/java/com/example/order_service/client/UserClient.java`
   - New client uses property `user.service.url` (can be provided via env var `USER_SERVICE_URL`).

- Added an environment entry for `USER_SERVICE_URL` in `docker-compose.yml` under `order-service` so the container resolves `user-service` inside the compose network.

- Fixed YAML indentation in `docker-compose.yml` so env/healthcheck/networks are correctly nested.

Commands you may need to run locally (PowerShell)

If you edited Java code (or after pulling the changes), rebuild the `order-service` artifact and image and recreate the container to pick up the new JAR and environment:

```powershell
Set-Location -Path 'C:\Project-Avows\spring-microservices-demo\order-service'
.\mvnw.cmd -DskipTests clean package
Set-Location -Path 'C:\Project-Avows\spring-microservices-demo'
docker-compose build --no-cache order-service
docker-compose up -d --no-deps --force-recreate order-service
docker-compose logs --tail=200 order-service
```

If you only changed configuration in `docker-compose.yml` (for example the `USER_SERVICE_URL`), recreate the container so it receives the updated environment:

```powershell
Set-Location -Path 'C:\Project-Avows\spring-microservices-demo'
docker-compose up -d --no-deps --force-recreate order-service
```

How to verify the fix

- Call an order endpoint that triggers a user lookup (this will exercise the Feign client):

```powershell
curl -UseBasicParsing http://localhost:8082/v1/orders?userId=1
```

- Check `order-service` logs for successful Feign calls or errors:

```powershell
docker-compose logs --tail=200 order-service
```

If you'd like, I can also create the `run-docker.ps1` and `smoke-test.ps1` files in the repo so you can run them directly instead of copying the commands above.


# RERUN PROJECT

# Build the jar
Set-Location 'C:\Project-Avows\spring-microservices-demo\order-service'
.\mvnw.cmd -DskipTests clean package

# Recreate the container with the new jar
Set-Location 'C:\Project-Avows\spring-microservices-demo'
docker-compose build --no-cache order-service
docker-compose up -d --no-deps --force-recreate order-service

# Tail logs and make the request
docker-compose logs --tail=200 -f order-service
# (open a new PowerShell window/tab for the request)
curl -UseBasicParsing http://localhost:8082/v1/orders?userId=1

# USER

# Build the jar
Set-Location 'C:\Project-Avows\spring-microservices-demo\user-service'
.\mvnw.cmd -DskipTests clean package

# Recreate the container with the new jar
Set-Location 'C:\Project-Avows\spring-microservices-demo'
docker-compose build --no-cache user-service
docker-compose up -d --no-deps --force-recreate user-service

# Tail logs and make the request
docker-compose logs --tail=200 -f user-service
# (open a new PowerShell window/tab for the request)
curl -UseBasicParsing http://localhost:8082/v1/users