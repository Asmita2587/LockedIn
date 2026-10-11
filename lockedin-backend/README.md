# LockedIn — Backend (Spring Boot)

Implements every feature from the abstract: skill-based matching, shared
syllabus with individual progress, flexible live sessions (tutor-led /
co-study), the hybrid credit economy, the doubt board, auto-compiled group
wiki, dual leaderboards, public teaching portfolio + trust score, and
availability-based scheduling.

## Stack
Spring Boot 3.3, Spring Security (JWT), Spring Data JPA, MySQL, Spring
WebSocket (STOMP) for the live doubt board, Spring Mail for reminders,
Spring `@Scheduled` for the weekly credit top-up.

## Run it
1. Create a MySQL database (or let `createDatabaseIfNotExist=true` do it).
2. Edit `src/main/resources/application.properties`:
   - `spring.datasource.username` / `password`
   - `lockedin.jwt.secret` — any long random string
   - `lockedin.signup.allowed-email-domain` — e.g. `@yourcollege.edu`
   - `spring.mail.username` / `password` (only needed for reminder emails)
3. `mvn spring-boot:run`

The API comes up on `http://localhost:8080`.

## Auth
Every endpoint except `/api/auth/**` and `/ws/**` requires a JWT:

```
Authorization: Bearer <token>
```

Get a token from `POST /api/auth/register` or `POST /api/auth/login`.
Registration is rejected unless the email ends with the configured college
domain.

## Endpoint map

| Feature | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Skills | `POST/GET /api/skills/me` |
| Matching | `GET /api/matches/suggestions`, `POST /api/matches/accept` |
| Groups | `POST /api/groups`, `POST /api/groups/{id}/join`, `GET /api/groups/{id}/members` |
| Syllabus | `POST/GET /api/groups/{id}/syllabus/topics`, `PUT .../topics/{id}/progress`, `GET .../lag-summary` |
| Doubt board | `POST/GET /api/groups/{id}/doubts`, `POST /api/groups/{id}/doubts/{id}/answers` (also broadcast live over `/topic/groups/{id}/doubts`) |
| Sessions | `POST /api/groups/{id}/sessions`, `.../{id}/start`, `.../{id}/complete`, `.../{id}/documents` |
| Ratings | `POST /api/sessions/{id}/ratings` |
| Wiki | `POST/GET /api/groups/{id}/wiki` |
| Availability | `POST/GET /api/availability/me`, `GET /api/availability/overlap` |
| Credits | `GET /api/credits/balance` |
| Leaderboards & portfolio | `GET /api/leaderboard/teachers`, `GET /api/leaderboard/learners`, `GET /api/users/{id}/portfolio` |

## Live doubt board (WebSocket)
Connect to `ws://localhost:8080/ws` (SockJS), subscribe to
`/topic/groups/{groupId}/doubts`. Every new doubt posted through the REST
endpoint is also pushed to that topic in real time.

## What's stubbed, on purpose
- **File storage** (`SessionDocument.fileUrl`): the service layer just stores
  a URL. Wire up the actual Cloudinary/S3 SDK call in the controller before
  calling `attachDocument(...)` — the shape is ready, the vendor call isn't
  included so you can pick credentials/bucket yourself.
- **Video/audio**: WebRTC/Jitsi runs client-side. The backend only tracks
  `videoEnabled`/`isAnonymous` flags and session metadata.
- **Reminder emails**: `spring-boot-starter-mail` is wired in
  `application.properties`; add a small `@Scheduled` job next to
  `CreditService.weeklyBaselineTopUp()` to actually send them once you have
  session `scheduledAt` times to check against.

## Notes on design choices
- `CreditService` is the one place credits move — teaching, co-study, and
  doubt-answer rewards all go through it, and it auto-creates a `Nudge` the
  moment a user's balance drops to the low-balance threshold.
- `SyllabusService.getGroupLagSummary` gives the group-level "who's behind on
  what" view described in the abstract, without ever exposing one member's
  progress as the whole group's.
- `MatchingService` ranks `MUTUAL` matches above `ONE_DIRECTIONAL` ones, per
  the abstract's matching priority.
