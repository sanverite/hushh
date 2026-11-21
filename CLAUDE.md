# hushh

An offline chat app for iOS and Android that sends end-to-end encrypted messages over a Bluetooth mesh and is wire-compatible with bitchat. It is written entirely by Claude Code. Read `GLOSSARY.md` for the vocabulary and `docs/adr/` for the decisions already made.

## Rules

- **Keep it simple. Do not over-engineer.** Write the minimum code that does the job. Don't add interfaces, abstractions, layers or configuration that aren't needed right now. Never add an abstraction just to make code testable.
- **Tests are welcome, but keep them simple.** Write plain unit, integration, performance and security tests against the real code. Don't build mock frameworks, simulators or test-only interfaces. The maintainer also tests by hand on real phones.
- **Licence is Apache-2.0.** You may read and copy from bitchat iOS (`permissionlesstech/bitchat`, public domain). **Never copy code from bitchat-android** (`permissionlesstech/bitchat-android`, GPL-3.0). Reading it to understand the protocol is fine.

- **Designs live in `design/hushh.figcraft`**, edited through the figCraft MCP server. Whenever that file changes, run `design/export.sh` and commit the regenerated `design/exports/` in the same commit, so the PNGs never drift from the source.
- **Never commit unless the maintainer explicitly says so.** This overrides any skill or workflow step that says to commit. Leave changes uncommitted in the working tree and say what's ready.
- **Branch names are `<type>/<issue>-<short-slug>`**, e.g. `feature/2-project-skeleton` or `bugfix/14-nickname-tag-clash`. The types are:
  - `feature`: new behaviour
  - `bugfix`: a bug fix
  - `hotfix`: an urgent fix to a release
  - `chore`: build, CI, dependencies or tooling
  - `docs`: documentation only

  Leave out `<issue>-` when there's no issue.

## Agent skills

### Issue tracker

Issues are tracked in this repo's GitHub Issues, using the `gh` CLI. See `docs/agents/issue-tracker.md`.

### Triage labels

Uses the default label names: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: one `GLOSSARY.md` and `docs/adr/` at the repo root. See `docs/agents/domain.md`.
