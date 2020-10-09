# ronove

[ronove](https://en.wikipedia.org/wiki/Ronove) is a Gradle plugin for minimal web applications.

Applies one of two conventions on two sub-projects of a root project.

It provides:

## 1. Backend conventions for

- An HTTP server: `java-express`.
- A configuration loading and application mechanism: `feather`.
- Minimal logging (`shax`).
- Include support libraries for HTTP backend configuration. Namely, additional handlers for `java-express`.

## 2. Frontend conventions

- Typescript schema generation.
- Webpack build processing.
- Project dependency wiring (i.e. backend requires the assembled frontend project).
