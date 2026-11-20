# Contributing to hushh

Thanks for helping. hushh is pre-alpha, and most of its code is written by AI coding agents (Claude Code) working from GitHub issues. Contributions from people are just as welcome, and the same rules apply to both.

## Issues

- **Bugs and ideas:** open a GitHub issue.
- **Security problems:** do **not** open an issue. Follow [`SECURITY.md`](SECURITY.md).
- **Work ready to pick up:** issues labelled `ready-for-agent` are fully specified and can be picked up by anyone, human or agent. Issues labelled `ready-for-human` need a person, for example for signing keys or testing on real phones.
- **Start here:** the full spec is [issue #1](https://github.com/sanverite/hushh/issues/1). Its sub-issues are the build tickets, in dependency order.

## Before you write code

Read these first:

- [`GLOSSARY.md`](GLOSSARY.md): use these words in code, issues and UI text. For example, write "Peer", not "node", and "Direct Message", not "DM".
- [`docs/adr/`](docs/adr): decisions already made, and why. To change one, open an issue first.

## Rules

1. **Keep it simple.** Write the minimum code that does the job. Don't add interfaces, abstractions, layers or configuration that aren't needed right now.
2. **Keep tests plain.** Unit, integration, performance and security tests against the real code are welcome. Don't add mock frameworks, simulators or interfaces that exist only for tests.
3. **Never copy code from bitchat-android.** [bitchat-android](https://github.com/permissionlesstech/bitchat-android) is GPL-3.0, and hushh is Apache-2.0. You may read it to understand the protocol. Code from bitchat iOS ([permissionlesstech/bitchat](https://github.com/permissionlesstech/bitchat)) is public domain and may be reused.
4. **Stay wire-compatible with bitchat.** If you touch the protocol, check it against the current bitchat iOS source. A mismatch fails silently on real phones.
5. **Keep design exports current.** If you change `design/hushh.figcraft`, run `design/export.sh` and commit the regenerated `design/exports/` in the same commit.

## Pull requests

- Keep each PR to one ticket or one fix, and link the issue.
- Say what you tested, and on which phones if you tested by hand.

## Licence

By contributing, you agree that your contributions are licensed under the project's [Apache-2.0 licence](LICENSE).
