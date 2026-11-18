# AGENTS.md — FlipRSS

Repository-wide instructions for coding agents. This file is the canonical copy of
the 3-AI collaboration protocol for this repository, with PROJECT DETAILS filled in.

GitHub is the single source of truth. The `.ai/` files summarise state; if they ever
conflict with GitHub history, GitHub is right — investigate and correct the file.

---

## PROJECT DETAILS

- **Purpose:** FlipRSS is a native Android client for a self-hosted FreshRSS
  instance. It presents the user's feeds in a Flipboard-style reading experience:
  a magazine card grid, a full-screen swipe/flip article reader, and content
  grouped into topic sections (FreshRSS folders/tags).
- **Stack:** Kotlin, Jetpack Compose (Material 3), Android (min SDK 26, compile/target SDK 35 for the prototype).
- **Key libraries:** Retrofit + OkHttp (FreshRSS Google Reader API),
  Kotlin Coroutines/Flow, Coil (image loading), Jsoup (extract cover images from
  item HTML), Jetpack DataStore + EncryptedSharedPreferences (credentials/token),
  Room (offline cache, planned), optional Hilt (DI, deferred).
- **Backend API:** FreshRSS "GReader" API at `<instance>/api/greader.php` —
  `ClientLogin` for the auth token, `subscription/list`, `tag/list`,
  `stream/contents/{streamId}`, `unread-count`, `edit-tag` (mark read/starred).
  The instance is reachable over the public internet, so a native HTTP client
  talks to it directly (no CORS layer needed).
- **Install:** Open the project in Android Studio and let Gradle sync, or
  `./gradlew assembleDebug` once the Gradle project exists.
- **Run:** `./gradlew installDebug` (or Run ▶ in Android Studio) onto a device/emulator.
- **Test:** `./gradlew test` (unit) and `./gradlew connectedAndroidTest` (instrumented).
- **Lint / format:** `./gradlew lint`; code style via ktlint/detekt if added.

> Status: #2 added the offline prototype; #6 adds FreshRSS authentication,
> catalogue/article loading, pagination and read/star synchronization. Live article
> caching remains in memory; Room and background sync are not implemented. Build
> with JDK 17+ and Android SDK 35. See README.md for setup and validation.

---

## PRIMARY OBJECTIVE

Maintain a shared understanding of the project, coordinate tasks, track exactly
which AI performed each action, and prevent duplicate or conflicting work.

## AUTHORITY

Rick assigns tasks and is the only one who merges into `main`. "Authorized" means
Rick has assigned the task to you, either by labelling the GitHub Issue or by
telling you in the session.

## TEAM ROLES

**ChatGPT** — Architecture and planning; feature specifications; task creation and
prioritization.

**Claude** — Independent code review; code analysis and debugging; architecture review.

**ChatGPT Codex** — Code implementation; refactoring; automated testing; feature
branches and pull requests; repository maintenance.

These are default responsibilities, not exclusive restrictions. Any agent may
perform an authorized task it has the tools to complete. One rule is fixed: the AI
that wrote a change does not review it. Rick names the reviewer.

## SHARED MEMORY

- `.ai/PROJECT_STATE.md` — Current project overview
- `.ai/TASK_BOARD.md` — Summary of active GitHub Issues
- `.ai/AI_ACTIVITY_LOG.md` — Who completed what (append-only)
- `.ai/HANDOFF.md` — Instructions for the next agent (latest only)
- `.ai/DECISIONS.md` — Architectural decisions (append-only)
- `.ai/FILE_OWNERSHIP.md` — Planned file modifications for in-progress tasks
- `AGENTS.md` — This file

GitHub Issues are authoritative for task assignments and statuses. Commits and PRs
are authoritative for implemented changes.

To avoid merge conflicts in the shared files:

- Update the `.ai/` files as the last step before opening a PR, after bringing your
  branch up to date with `main`.
- `AI_ACTIVITY_LOG.md` and `DECISIONS.md` are append-only — add one dated entry at
  the end; never edit or reorder earlier entries.
- `TASK_BOARD.md` is rebuilt from current GitHub Issues each time; do not hand-edit
  rows from memory.
- `HANDOFF.md` holds only the most recent handoff; replace its contents.
- `FILE_OWNERSHIP.md` lists files for in-progress tasks only; add yours when you
  claim a task and remove them in the PR that completes it.
- If a `.ai/` file conflicts when updating your branch, keep both agents' entries —
  never discard another agent's entry to resolve a conflict.

## SESSION START PROCEDURE

1. Identify yourself as ChatGPT, Claude, or Codex.
2. Determine whether GitHub access is available. If not, follow WEBSITE MODE.
3. Retrieve the latest repository state.
4. Read `AGENTS.md` and all relevant `.ai/` files.
5. Review open Issues, active PRs, and recent commits.
6. Identify work completed by other agents.
7. Check `FILE_OWNERSHIP.md` and open PRs for overlapping tasks and file changes.
8. Confirm the task Rick has given you, or recommend the next unclaimed task and
   wait for his approval.

Never assume information from a previous conversation is current.

## TASK OWNERSHIP

Every task must have a GitHub Issue with: Task ID / Issue number, Description,
Acceptance criteria, Responsible AI, Status, Dependencies, Related branch and PR.

Labels:

- `ai:chatgpt`, `ai:claude`, `ai:codex`
- `status:in-progress`, `status:blocked`, `status:review`

An AI identity label records the agent, not necessarily a separate GitHub account.

Claiming a task: work only on an Issue Rick assigned to you; check no other agent's
label and no open PR / `FILE_OWNERSHIP.md` entry covers the same files; apply your
`ai:` label and `status:in-progress` and comment that you have claimed it; re-read
the Issue to confirm no simultaneous claim. If another agent has claimed it or the
files overlap, stop and tell Rick. Work found without an Issue → propose a new
Issue; do not fold it into the current task.

## DEVELOPMENT WORKFLOW

1. Claim the assigned task.
2. Branch from latest `main`, named `<agent>/<issue-number>-<short-name>`
   (e.g. `codex/42-user-auth`). One Issue per branch.
3. Implement only what the Issue asks.
4. Run the test and lint commands.
5. Review the diff.
6. Commit with AI identity and task reference (format below).
7. Bring the branch up to date with `main`, then update the `.ai/` files.
8. Open a pull request.
9. Set the Issue to `status:review` and request review.

Commit message format:

```
Add login endpoint (#42)

AI: Codex
```

Never force-push shared branches or overwrite another agent's work. Do not push to
another agent's branch unless Rick asks. Do not merge into `main` — Rick merges.

## REQUIRED AI ATTRIBUTION

Each development report must identify: AI responsible, Task ID, Work performed,
Files changed, Tests performed, Commit SHA or PR link, Outstanding issues.

```
AI: Codex
Task: #42
Action: Implemented user authentication
Tests: Passed
PR: #47
Status: Awaiting review
```

If tests were not run, write "Not run" and give the reason. Do not attribute work
to an AI that did not perform it.

## WEBSITE MODE

If operating through the normal ChatGPT/Claude website without repository access:
state you cannot see the repo; ask Rick for the specific files/Issue text/`.ai/`
files you need; state your output is unchecked against the current repo; prepare
changes as complete files or a diff plus commit message, activity-log entry, and
handoff text; never claim a GitHub update succeeded unless it did.

## CODEX MODE

With repository access: follow `AGENTS.md`; sync remote; check Issues and PRs;
implement the authorized task; run tests and lint; commit and open a PR; update
shared docs; record what Codex completed; prepare the next-agent handoff.

## HANDOFF PROTOCOL

At the end of every session, write to `.ai/HANDOFF.md` and include in the PR body:
Agent, Completed, In progress, Branch/PR, Tests, Blockers, Next action, Shared
records updated (Yes/No), Remote synchronization (Confirmed/Unavailable).

## CRITICAL RULES

- Never invent project progress. Report only work you did and checked yourself.
- Never claim a GitHub update succeeded without confirmation.
- Never expose secrets or credentials in commits, Issues, PRs, or logs. If you find
  one already committed, tell Rick immediately and do not repeat its value.
- Always preserve project history.
- Prefer small, reviewable pull requests.
- Ask Rick when task ownership is ambiguous.
