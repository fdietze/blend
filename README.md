# Blend

Character based git conflict resolution tool.

Use the GUI: https://fdietze.github.io/blend

## Idea
Instead of using line-based resolution (like git), use a string diffing/patching library that works on the character level: https://github.com/google/diff-match-patch.

## Future ideas
- Paste full file with multiple conflicts to detect moves between different conflicts
- CLI to run in a git-repository to detect cross file movements, run webserver to provide resolution GUI

## Resources
- https://neil.fraser.name/writing/patch/

## Development
### Prerequisites

Please make sure that the following software is installed:

 - [Node.js](https://nodejs.org/en/download/)
 - [Yarn](https://yarnpkg.com/en/docs/install)
 - [Sbt](https://www.scala-sbt.org/download.html)

### Local Development

```sh
sbt dev
```

Then open `http://localhost:12345` in your browser.

