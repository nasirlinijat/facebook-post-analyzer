# Meta Graph API Post Performance Analysis

A lightweight Spring Boot application that fetches the last 20 posts from a Meta account via the Graph API, analyzes engagement metrics, and surfaces top-performing content and the best days for generating likes.

## Links (Deliverables)

- **GitHub repository:** `https://github.com/nasirlinijat/facebook-post-analyzer`
- **Railway deploy:** `https://facebook-post-analyzer.up.railway.app/`
  - Report page (UI): `https://facebook-post-analyzer.up.railway.app/`
  - Raw JSON: `https://facebook-post-analyzer.up.railway.app/api/report`
  - Swagger UI: `https://facebook-post-analyzer.up.railway.app/swagger-ui.html`

## Tech Stack

- Java 21
- Spring Boot 3.x
- Gradle
- Spring Web (`spring-boot-starter-web`)
- Jackson (JSON parsing)
- dotenv-java (`.env` management)
- Spring `RestClient` (HTTP client)
- springdoc-openapi (Swagger UI / OpenAPI 3 docs)
- MapStruct (compile-time DTO mapping)
- Railway (deployment)
- Git & GitHub (version control)

## Local Installation & Setup

1. Clone the repository:

```bash
git clone https://github.com/nasirlinijat/facebook-post-analyzer.git
cd facebook-post-analyzer
```

2. Create a `.env` file in the project root (use the template below):

```env
META_ACCESS_TOKEN=
META_PAGE_ID=
META_API_VERSION=v25.0
```

3. Fill in your Meta Graph API credentials in `.env` (see **Getting a Meta Access Token** below).

> **Security:** the access token is **never hardcoded**. It is read at runtime from the `.env` file (loaded via `dotenv-java`) and bound to `MetaProperties`. `.env` is listed in `.gitignore`, so credentials are never committed. On Railway the same values are provided as environment variables.

4. Run the application:

```bash
./gradlew bootRun
```

5. Open the report:

- Report page (UI): `http://localhost:8080/`
- Raw JSON: `http://localhost:8080/api/report`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

> The server port defaults to `8080` and can be overridden with the `PORT` environment variable (used automatically by Railway).

## Getting a Meta Access Token

The Graph API can only read posts from a **Facebook Page** you administer (reading a personal profile's posts is not supported by the API). While your app is in *Development* mode, you can read Pages you admin **without App Review**.

1. In [Meta for Developers](https://developers.facebook.com), create an app (type **Business**) and note the **App ID** / **App Secret**.
2. Open the [Graph API Explorer](https://developers.facebook.com/tools/explorer), select your app, and add the permissions: `pages_show_list`, `pages_read_engagement`, `pages_read_user_content`, `read_insights`. Generate a User token.
3. Run `GET /me/accounts` — the response contains your Page `id` (→ `META_PAGE_ID`) and a Page `access_token`.
4. Make the token long-lived so it does not expire mid-review:

   ```bash
   curl "https://graph.facebook.com/v25.0/oauth/access_token?grant_type=fb_exchange_token&client_id=APP_ID&client_secret=APP_SECRET&fb_exchange_token=SHORT_LIVED_TOKEN"
   ```

   For a **non-expiring** Page token, exchange the *User* token first, then call `GET /me/accounts` again with the long-lived User token and use the Page token it returns.
5. Put the Page token and Page ID into `.env`.

> If a valid token/Page is not available, the app still runs and returns a meaningful report from a built-in **sample dataset** — see [Data Source & Sample Fallback](#data-source--sample-fallback).

## API Documentation (Swagger / OpenAPI)

The API is documented with springdoc-openapi. With the app running:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html` — interactive docs to explore and try the `GET /api/report` endpoint.
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs` — the raw OpenAPI 3 spec.

API metadata (title, description, version) is configured in `config/OpenApiConfig.java`, and the endpoint, its `200` response, and the response schemas are described with annotations on `ReportController` and the response DTOs. The endpoint always returns `200` — when live data is unavailable it falls back to sample data rather than erroring (see below).

## Project Architecture

The project follows a layered Controller-Service-Model architecture with interface-based services and a dedicated DTO/mapper layer for the public API:

- **Model** — `Post`, `Engagement`, `MetaResponse` bind the inbound Meta Graph API JSON; `AnalysisReport` is the internal domain result produced by the analysis service. These are never serialized directly to clients.
- **Service** — interfaces live in `service`, concrete implementations in `service/impl`, to keep callers decoupled from the implementation (loose coupling):
  - `MetaApiClientService` / `MetaApiClientServiceImpl` — external HTTP calls to Meta.
  - `DataAnalysisService` / `DataAnalysisServiceImpl` — sorting, aggregation, and summary generation, free of web concerns.
  - `SamplePostProvider` / `SamplePostProviderImpl` — loads the built-in sample dataset used as a fallback.
- **DTO** — response DTOs in `dto/response` define the stable public API shape, decoupled from the Meta wire format:
  - `PostResponse` — a flattened post (`totalLikes`, `totalComments`, `engagementScore`) instead of Meta's nested `likes.summary.total_count`.
  - `AnalysisReportResponse` — the full report (`topPosts`, `likesByDay`, `bestDaysForLikes`, `summary`, `dataSource`).
  - (Request DTOs would go in `dto/request`; the current API has no request bodies.)
- **Mapper** — [MapStruct](https://mapstruct.org/) mappers in `mapper` convert domain models to response DTOs at compile time:
  - `PostMapper` — `Post` → `PostResponse` (derives `totalLikes`/`engagementScore` from existing getters).
  - `AnalysisReportMapper` — `AnalysisReport` → `AnalysisReportResponse`, delegating `topPosts` to `PostMapper` (uses constructor injection).
- **Controller** — `ReportController` exposes `GET /api/report`, delegates to the services, and returns an `AnalysisReportResponse` via the mapper. Annotated for Swagger.
- **Config** — `AppConfig` (RestClient), `OpenApiConfig` (Swagger metadata), `MetaProperties` / `DotenvConfig` (`.env`-backed configuration).
- **UI** — a static `index.html` (served at `/`) fetches `/api/report` and renders the summary, the top-3 posts table, and a likes-by-day bar chart.

This separation keeps external API integration, business analysis, presentation, and the public API contract independent, which makes the code easier to test, extend, and maintain.

## Tests

Run the test suite:

```bash
./gradlew test
```

- **`DataAnalysisServiceTest`** — covers top-3 ordering, best-day aggregation, empty input, and skipping posts with unparseable dates.
- **`MapperTest`** — covers the MapStruct mappers: post flattening (`totalLikes`/`engagementScore`), missing-engagement defaults, the report mapper delegating `topPosts` to `PostMapper`, empty reports, and null-safety.

## Analysis Explanation

For each of the most recent (up to 20) posts the app reads four fields — **post text** (`message`), **publish date** (`created_time`), **like count**, and **comment count** — and computes:

- **Top 3 posts** ranked by `likes + comments` engagement score.
- **Likes by day of week** — likes summed per weekday (Monday–Sunday) using each post's `created_time`.
- **Best day(s)** — the weekday(s) with the highest cumulative likes.
- A **text summary** describing totals, averages, and the standout post/day.

### Sample Results

The example below is the actual `/api/report` output from the built-in sample dataset:

```json
{
  "topPosts": [
    { "message": "Big milestone today — we just crossed 1,000 users! ...", "createdTime": "2026-06-17T18:00:00+0000", "totalLikes": 22, "totalComments": 8, "engagementScore": 30 },
    { "message": "Friday wins thread! ...", "createdTime": "2026-06-19T13:00:00+0000", "totalLikes": 12, "totalComments": 6, "engagementScore": 18 },
    { "message": "What's the one VS Code (or browser) extension ...", "createdTime": "2026-06-16T15:45:00+0000", "totalLikes": 9, "totalComments": 4, "engagementScore": 13 }
  ],
  "likesByDay": { "MONDAY": 10, "TUESDAY": 12, "WEDNESDAY": 27, "THURSDAY": 11, "FRIDAY": 17, "SATURDAY": 3, "SUNDAY": 6 },
  "bestDaysForLikes": ["WEDNESDAY"],
  "summary": "Analyzed 12 posts with 86 total likes and 30 total comments (avg engagement: 9). Top post scored 30 engagement (22 likes, 8 comments). Best day(s) for likes: WEDNESDAY with 27 total likes across analyzed posts.",
  "dataSource": "sample"
}
```

**Conclusion (sample):** the milestone announcement was by far the strongest post (engagement 30), and **Wednesday** generated the most likes (27), with Friday second (17) — suggesting mid-week and end-of-week posts perform best. With a live token (`dataSource: "live"`) the same analysis runs on your real Page data.

## Data Source & Sample Fallback

The app prefers **live** Meta Graph API data. If the live call returns no posts (e.g. an empty, new, or temporarily unavailable Page) or fails, it transparently falls back to a built-in **sample dataset** (`src/main/resources/sample-posts.json`) so the endpoint and the deployed demo always return a meaningful analysis.

The response always reports its origin via the `dataSource` field:

- `"live"` — analysis of real posts from the Meta Graph API (no banner in the UI).
- `"sample"` — analysis of the bundled sample data (the UI shows a clear "sample data" notice).

This keeps the Railway deployment working for reviewers even if a Page token lapses or a Page is unavailable, while real API integration remains the primary path.

## Railway Deployment

1. Push the project to GitHub (ensure `.env` is not committed).
2. Create a Railway project linked to the repository (Root Directory = repository root).
3. Set environment variables in Railway: `META_ACCESS_TOKEN`, `META_PAGE_ID`, and optionally `META_API_VERSION`.
4. Railway detects the included **`Dockerfile`**, which builds the app on **JDK 21** and runs the executable jar (`build/libs/app.jar`). Railway injects a `PORT` environment variable and the app binds to it automatically (`server.port=${PORT:8080}`).

> If the Meta variables are absent or the live Page returns no posts, the app still serves a meaningful report from the built-in sample dataset (see *Data Source & Sample Fallback*).

## Requirements Coverage

How this project maps to the task requirements:

| Requirement | Where / How |
|---|---|
| Create a test app on Meta for Developers | App created (Business type); used to obtain tokens |
| Obtain an access token | Long-lived Page token via Graph API Explorer + `fb_exchange_token` (see *Getting a Meta Access Token*) |
| Fetch last 20 posts: text, date, likes, comments | `MetaApiClientService` requests `id,message,created_time,likes.summary(true),comments.summary(true)` with `limit=20` |
| Top 3 posts by engagement | `DataAnalysisService` ranks by `likes + comments` |
| Which days collect the most likes | `DataAnalysisService` aggregates likes per day of week and reports the best day(s) |
| Short engagement conclusion | Generated `summary` text (see *Analysis Explanation*) |
| Output in terminal or a simple report page | Report page UI at `/` + JSON at `/api/report` |
| Token not hardcoded; stored in `.env` | Read at runtime via `dotenv-java` → `MetaProperties`; `.env` is git-ignored |
| README with setup & usage | This file |
| Analysis explanation & sample results | *Analysis Explanation* + *Sample Results* sections |
| GitHub repo & Railway deploy links | *Links (Deliverables)* section |

**Bonus / initiative:** Swagger/OpenAPI docs, MapStruct DTO mapping with a clean request/response boundary, interface-based services for loose coupling, unit tests, and a sample-data fallback that keeps the deployed demo working even when live data is unavailable.
