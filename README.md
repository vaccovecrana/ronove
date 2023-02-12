# ronove

[ronove](https://en.wikipedia.org/wiki/Ronove) is a Gradle plugin for minimal web applications.

It provides:

- Typescript RPC stubs through [typescript-generator-gradle-plugin](https://github.com/vojtechhabarta/typescript-generator).
- Server adapters for [Jakarta RESTful Web Services](https://jakarta.ee/specifications/restful-ws/3.0/jakarta-restful-ws-spec-3.0.html)
- Supplemental annotations for web controller method definitions.

Opinionated choices:

- Query parameters only support single key/value definitions.
- Things like list query parameters in `GET` request are best sent as header parameters.

TODO

- Describe controller definition annotations.
