# LockedIn — Frontend (React + Vite)

Talks to the LockedIn Spring Boot backend. Covers auth, skills,
matching, study groups (shared syllabus + doubt board), and credits
(balance, leaderboards, teaching portfolio).

## Run locally

```bash
npm install
cp .env.example .env
# edit .env if your backend isn't on http://localhost:8080
npm run dev
```

Opens on `http://localhost:5173`.

## Pages
- `/login`, `/register` — auth
- `/dashboard` — balance + match count + groups shortcut
- `/skills` — add skills you can teach / want to learn
- `/matches` — suggested matches, mutual ones first, accept to lock one in
- `/groups` — create or join a group by ID
- `/groups/:groupId` — members, shared syllabus (your own progress per topic), doubt board
- `/credits` — balance, your teaching portfolio, dual leaderboards

Every page talks to an endpoint that exists as-is in the backend repo.
