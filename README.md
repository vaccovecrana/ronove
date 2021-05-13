# ronove

[ronove](https://en.wikipedia.org/wiki/Ronove) is a Gradle plugin for minimal web applications.

It provides:

- [feather](https://github.com/zsoltherpai/feather) as a configuration loading and application mechanism.
- Typescript RPC stubs through [typescript-generator-gradle-plugin](https://github.com/vojtechhabarta/typescript-generator).
- Supplemental annotations for web controller method definitions.

Opinionated choices:

- Query parameters only support single key/value definitions.
- Things like list query parameters in `GET` request are best sent as header parameters.

TODO

- Describe controller definition annotations.
