# OpenBoxes Docker

Run [OpenBoxes](https://openboxes.com) — open-source supply chain management — in Docker with minimal setup.

## Quick Start

The fastest way to get OpenBoxes running:

```bash
docker compose up -d
```

This starts OpenBoxes and a MariaDB database. On first startup, OpenBoxes runs database migrations (Liquibase) which takes **2-3 minutes**. You can monitor progress:

```bash
docker compose logs -f openboxes
```

Once you see `Server startup in [xxx] milliseconds`, open **http://localhost:8080** and log in with:

- **Username:** `admin`
- **Password:** `password`

> **Important:** Change the default admin password immediately after first login.

## Requirements

- Docker Engine 20.10+ and Docker Compose v2
- At least **2GB of RAM** available to Docker (OpenBoxes uses ~1.4GB during startup)

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `OPENBOXES_DB_HOST` | `localhost` | MariaDB/MySQL hostname |
| `OPENBOXES_DB_PORT` | `3306` | Database port |
| `OPENBOXES_DB_NAME` | `openboxes` | Database name (created automatically) |
| `OPENBOXES_DB_USERNAME` | `openboxes` | Database user |
| `OPENBOXES_DB_PASSWORD` | *(empty)* | Database password |
| `OPENBOXES_SERVER_URL` | `http://localhost:8080` | Public URL (used for redirects and email links) |
| `EXTRA_CATALINA_OPTS` | *(empty)* | Additional JVM options (appended to defaults) |

Copy `.env.example` to `.env` to customize:

```bash
cp .env.example .env
# Edit .env with your values
```

### Using an External Database

To connect to an existing MariaDB/MySQL instance instead of the bundled one:

```bash
docker run -d -p 8080:8080 \
  -e OPENBOXES_DB_HOST=your-db-host \
  -e OPENBOXES_DB_PORT=3306 \
  -e OPENBOXES_DB_NAME=openboxes \
  -e OPENBOXES_DB_USERNAME=openboxes \
  -e OPENBOXES_DB_PASSWORD=your-password \
  -e OPENBOXES_SERVER_URL=https://your-domain.com \
  -v openboxes-data:/app/data \
  openboxes
```

**Database requirements:**
- MariaDB 10.x+ or MySQL 5.7+ (MariaDB 11 recommended)
- The database specified in `OPENBOXES_DB_NAME` will be created automatically if it doesn't exist
- The user needs `CREATE`, `ALTER`, `DROP`, `INSERT`, `UPDATE`, `DELETE`, `SELECT`, `INDEX`, `REFERENCES` privileges

### Data Persistence

The Docker Compose setup uses named volumes for persistence:

| Volume | Mount Point | Contents |
|--------|------------|----------|
| `openboxes-data` | `/app/data` | Uploaded files and documents |
| `mariadb-data` | `/var/lib/mysql` | Database files |

To back up your data:

```bash
# Database backup
docker compose exec mariadb mariadb-dump -u root -proot openboxes > backup.sql

# File backup
docker compose cp openboxes:/app/data ./data-backup
```

To restore:

```bash
# Database restore
docker compose exec -T mariadb mariadb -u root -proot openboxes < backup.sql
```

### JVM Memory Tuning

The default JVM settings allocate up to 1.5GB of heap memory. To adjust:

```yaml
# docker-compose.yml
services:
  openboxes:
    environment:
      EXTRA_CATALINA_OPTS: "-Xmx2g"
```

Or when using `docker run`:

```bash
docker run -e EXTRA_CATALINA_OPTS="-Xmx2g" ...
```

**Minimum recommended:** 1.5GB (`-Xmx1536m`)
**For larger deployments:** 2-4GB (`-Xmx2g` to `-Xmx4g`)

## Building a Specific Version

To build an image for a different OpenBoxes release:

```bash
# Use any tag from https://github.com/openboxes/openboxes/releases
docker build --build-arg OPENBOXES_VERSION=v0.9.6-hotfix1 -t openboxes:0.9.6 .
```

## Production Deployment

For production use, make sure to:

1. **Set strong passwords** for both MariaDB and the OpenBoxes admin user
2. **Set `OPENBOXES_SERVER_URL`** to your public URL (required for correct redirects)
3. **Use a reverse proxy** (nginx, Traefik, etc.) for TLS termination
4. **Back up regularly** — both the database and the `/app/data` volume
5. **Monitor disk space** — uploaded documents accumulate in `/app/data`

### Example with TLS (nginx reverse proxy)

```yaml
# docker-compose.prod.yml
version: "3.8"

services:
  openboxes:
    build: .
    environment:
      OPENBOXES_DB_HOST: mariadb
      OPENBOXES_DB_NAME: openboxes
      OPENBOXES_DB_USERNAME: openboxes
      OPENBOXES_DB_PASSWORD: ${DB_PASSWORD}
      OPENBOXES_SERVER_URL: https://openboxes.example.com
    volumes:
      - openboxes-data:/app/data
    depends_on:
      mariadb:
        condition: service_healthy
    restart: unless-stopped

  mariadb:
    image: mariadb:11
    environment:
      MARIADB_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
      MARIADB_DATABASE: openboxes
      MARIADB_USER: openboxes
      MARIADB_PASSWORD: ${DB_PASSWORD}
    volumes:
      - mariadb-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "healthcheck.sh", "--connect", "--innodb_initialized"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
      - /etc/letsencrypt:/etc/letsencrypt:ro
    depends_on:
      - openboxes
    restart: unless-stopped

volumes:
  openboxes-data:
  mariadb-data:
```

## Troubleshooting

### OpenBoxes won't start / exits immediately

Check the logs:
```bash
docker compose logs openboxes
```

Common causes:
- **Database not ready:** OpenBoxes waits for MariaDB but if it takes too long, restart: `docker compose restart openboxes`
- **Out of memory:** Increase Docker's memory limit to at least 2GB
- **Port conflict:** Change the host port: `ports: ["9090:8080"]`

### Liquibase lock error on startup

If a container was killed during database migrations, Liquibase leaves a stale lock:

```bash
docker compose exec mariadb mariadb -u openboxes -popenboxes openboxes -e \
  "UPDATE DATABASECHANGELOGLOCK SET LOCKED=0, LOCKGRANTED=NULL, LOCKEDBY=NULL WHERE ID=1;"
docker compose restart openboxes
```

### First startup is very slow

This is normal. Liquibase runs ~1000 database migration changesets on first startup, creating 125+ tables. Subsequent startups are much faster (30-60 seconds).

### Cannot connect to database

Verify the database is running and accessible:

```bash
docker compose exec mariadb mariadb -u openboxes -popenboxes -e "SELECT 1"
```

## Architecture

```
┌──────────────┐     ┌──────────────┐
│   Browser    │────▶│   Tomcat 9   │
│              │     │  (port 8080) │
└──────────────┘     │              │
                     │  OpenBoxes   │
                     │   (WAR)     │
                     └──────┬───────┘
                            │
                     ┌──────▼───────┐
                     │  MariaDB 11  │
                     │ (port 3306)  │
                     └──────────────┘
```

OpenBoxes is a Grails application packaged as a WAR file, deployed on Tomcat 9 with JDK 11. It uses Liquibase for database migrations, which run automatically on startup.

## License

OpenBoxes is licensed under the [Eclipse Public License 1.0](https://www.eclipse.org/legal/epl-v10.html).
