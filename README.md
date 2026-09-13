# ronove

[ronove](https://en.wikipedia.org/wiki/Ronove) is a Gradle plugin for minimal web applications.

It provides:

- Typescript RPC stubs defined by Jakarta RESTful annotated controllers.
- Server adapters for [Jakarta RESTful Web Services](https://jakarta.ee/specifications/restful-ws/3.0/jakarta-restful-ws-spec-3.0.html)
- Supplemental annotations for web controller method definitions.

Opinionated choices:

- DTOs defined as Controller parameters do not support inheritance (to keep code generation simple).

See examples at [rv-test](./rv-test/src/test/java/io/vacco/ronove).

## Results and validations

Controllers may return any object; when the object extends `RvResult` the
response carries an optional `error` string and a `validations` array. Each
validation is a locale-agnostic contract between backend and frontend:

```json
{
  "error": null,
  "validations": [
    { "name": "registrationAge", "key": "app.i18n.ageValidationFailed", "params": { "minAge": "18", "maxAge": "39" } }
  ]
}
```

- `key` is the i18n message key. The frontend owns the human-readable sentence
  templates per locale and renders them via its own i18n layer (e.g. `t(key, params)`).
- `params` holds the placeholder values the template interpolates. Values must be
  server-computed context (thresholds, limits) — never echoed user input.
- `name` optionally identifies the attribute the validation applies to, so forms
  can bind the error to the matching input.

```
RvValidation.vld("app.i18n.ageValidationFailed")
  .withName("registrationAge")
  .withParams(Map.of("minAge", "18", "maxAge", "39"));
```

TODO

- Describe controller definition annotations.
