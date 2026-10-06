# tutoring-analytics-report
A Kotlin-based reporting system that generates administrative analytics from tutoring program data. Connects to PostgreSQL database to produce PDF and Excel reports with department trends, teacher performance metrics, and student engagement statistics.

## Overview

## Features

## Tech

## Startup
Two ways to run it:

- **On demand from the RR Tutoring app (Railway).** `serve` starts a small HTTP server
  (JDK built-in, no extra dependencies). The app's Admin Dashboard has a
  "Download full report" button that calls `GET /report` and streams the `.xlsx` back.
- **One-off locally.** Run with no arguments to write `tutoring_report_<date>.xlsx` into the
  current directory, as before: `./gradlew run`.

### Railway setup
1. In the **same Railway project** as the RR Tutoring app, add a new service from this repo.
   It builds from the `Dockerfile` and starts `serve` by default.
2. Set its variables:
   - `DATABASE_URL`: reference the Postgres service's **private** URL (`${{Postgres.DATABASE_URL}}`).
   - `DB_SSL=false`: the private network isn't TLS. Leave it unset if you use the public proxy URL.
   - `REPORT_TOKEN`: a long random string, shared with the app server.
   - `PORT=8080`: pin the port so the internal URL below stays stable.
3. Don't generate a public domain. The app reaches it at `http://<service-name>.railway.internal:8080`.
4. Turn on **Serverless** (sleep when idle). It costs almost nothing between clicks; the first click
   after a quiet spell waits a few seconds for the JVM to start.
5. On the RR Tutoring **server** service, set `REPORT_SERVICE_URL` to that internal URL and the same `REPORT_TOKEN`.

## Config
Environment variables take precedence; `src/main/resources/config.properties` (gitignored,
keys `db.url`, `db.user`, `db.password`) is an optional fallback for local runs.

| Variable | Purpose |
| --- | --- |
| `DATABASE_URL` | `postgres://user:pass@host:port/db` (Railway's format) or a `jdbc:postgresql://` URL |
| `DB_USER` / `DB_PASSWORD` | Override the credentials in the URL (needed for a bare JDBC URL) |
| `DB_SSL` | `false` disables `sslmode=require` (Railway private network) |
| `REPORT_TOKEN` | Required for `serve`; callers must send it as `X-Report-Token` |
| `PORT` | HTTP port for `serve` (default 8080) |

The grade-level sheet works out the current school year (Aug–Jul) from today's date.

## Usage
- `GET /health` returns `ok`
- `GET /report` with header `X-Report-Token: <REPORT_TOKEN>` returns the `.xlsx`. A missing or wrong token gets 401.
