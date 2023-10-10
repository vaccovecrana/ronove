# ronove

[ronove](https://en.wikipedia.org/wiki/Ronove) is a Gradle plugin for minimal web applications.

It provides:

- Typescript RPC stubs defined by Jakarta RESTful annotated controllers.
- Server adapters for [Jakarta RESTful Web Services](https://jakarta.ee/specifications/restful-ws/3.0/jakarta-restful-ws-spec-3.0.html)
- Supplemental annotations for web controller method definitions.

Opinionated choices:

- DTOs defined as Controller parameters do not support inheritance (to keep code generation simple).

See examples at [rv-test](./rv-test/src/test/java/io/vacco/ronove).

TODO

- Describe controller definition annotations.
