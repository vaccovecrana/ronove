# ronove

[ronove](https://en.wikipedia.org/wiki/Ronove) is a Gradle plugin + adapter kit for minimal web applications.

One annotated Jakarta REST controller class is the single source of truth for both the **HTTP server** and the *
*generated TypeScript RPC client**.

## Modules

| Module             | Purpose                                                           |
|--------------------|-------------------------------------------------------------------|
| `rv-core`          | Annotations, reflection engine (`RvContext`), results/validations |
| `rv-kit-undertow`  | [Undertow](https://undertow.io/) adapter                          |
| `rv-kit-murmux`    | [Murmux](https://github.com/vaccovecrana/murmux) adapter          |
| `rv-gradle-plugin` | TypeScript RPC code generation                                    |

## Quickstart

Annotate a plain class with Jakarta REST annotations:

```java
public class MyApi {

  @GET
  @Path("/v1/echo/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public MyReply echo(@PathParam("id") int id) {
    return MyReply.ok("echo: " + id);
  }

  @POST
  @Path("/v1/update")
  @Consumes(MediaType.APPLICATION_JSON)
  public RvResponse<Void> update(@BeanParam MyUpdate body) {
    return new RvResponse<Void>().withStatus(Response.Status.NO_CONTENT);
  }
}
```

Wire it with an adapter — the `build(...)` call turns one or more controller instances into a request
handler:

```java
var g = new Gson();
var jIn = (RvJsonInput) g::fromJson;
var jOut = (RvJsonOutput) g::toJson;
var api = new RvMxAdapter((xc, e) -> log.error("Err", e), jIn, jOut).build(new MyApi());
new Murmux().rootHandler(api::handle).listen(8080);
```

`build(...)` accepts any number of controller instances; `(HTTP method, path)` pairs must be unique across
all of them. Undertow wiring is analogous:
`new RvUtAdapter(errorHandler, jIn, jOut, attachmentKeys...).build(new MyApi(), new OtherApi())`, with
`@RvAttachmentParam` keys declared via `RvUtAttachmentKey`.

## Gradle setup

```kotlin
plugins { id("io.vacco.ronove") version "<latest>" }
ronove {
  controllerClasses = arrayOf("com.example.MyApi")
  outFile.set(layout.projectDirectory.file("src/web/rpc.ts"))
  optionalFields = false
  // optional: emit GraalVM reflection metadata for the same DTO type graph
  reflectConfigFile.set(
    layout.projectDirectory.file(
      "src/main/resources/META-INF/native-image/com.example/app/reflect-config.json"
    )
  )
  // optional: emit the newer GraalVM 23.1+ reachability metadata instead
  reachabilityMetadataFile.set(
    layout.projectDirectory.file(
      "src/main/resources/META-INF/native-image/com.example/app/reachability-metadata.json"
    )
  )
}
```

The plugin adds `jakarta.ws.rs-api` and registers the `ronoveTypescriptRpc` task (runs after `classes`),
which scans `controllerClasses` and writes the TS client to `outFile`.

When `reflectConfigFile` and/or `reachabilityMetadataFile` is set, the same task also writes GraalVM
reflection metadata covering every DTO reachable from the controllers **and the controller classes
themselves**, so route discovery and JSON (de)serialization work in a `native-image` build without running
the tracing agent. Controller classes are registered so `Class.getMethods()` + `Method.getAnnotations()`
stay intact at runtime, which is required for controllers whose methods use parameter annotations such as
`@BeanParam`. Every entry is emitted with `unsafeAllocated` so gson can allocate DTOs through
`Unsafe.allocateInstance`.

- `reflectConfigFile` emits the classic `reflect-config.json`, a supported input for GraalVM 22+ (and still
  read by 23.x/24.x).
- `reachabilityMetadataFile` emits the newer `reachability-metadata.json` (GraalVM 23.1+) with the reflection
  entries under a top-level `reflection` array.

Either file can be set independently, or both at once. **The files must be named exactly
`reflect-config.json` / `reachability-metadata.json`** — GraalVM only auto-merges configuration files with
those canonical names, so a differently-named file is silently ignored. Place them under
`META-INF/native-image/<group>/<artifact>/` so native-image picks them up from the classpath automatically.

## GraalVM metadata opt-in

`@RvGraal` on a controller registers additional server-side classes for reflection
(internal DTOs, DB models, etc.) that are not reachable from any RPC signature:

```java
@RvGraal(include = { MyDbCar.class }, rpc = true)
public class MyApi { /* ... */ }
```

- `include` lists extra classes to register for native-image reflection.
- `rpc` controls whether those classes are also emitted as TypeScript interfaces.
  With the default `rpc = false`, they are registered for GraalVM only and never
  leak into the generated TS client.

Attachment parameter types (`@RvAttachmentParam`) are always treated as server-side
internals: they are registered for GraalVM but omitted from the TS schema unless they
are also reachable from a response or request body type.

## Request parameters

| Annotation                         | Source                                                             |
|------------------------------------|--------------------------------------------------------------------|
| `@PathParam("x")`                  | Path segment (`@Path("/a/{x}")`)                                   |
| `@QueryParam("x")`                 | Query string                                                       |
| `@CookieParam("x")`                | Request cookie                                                     |
| `@HeaderParam("x")`                | Request header                                                     |
| `@FormParam("x")`                  | Form-encoded body field                                            |
| `@BeanParam`                       | JSON body, deserialized into a POJO                                |
| `@RvAttachmentParam(MyUser.class)` | Object placed on the exchange by a parent handler (e.g. a session) |
| `@DefaultValue("...")`             | Fallback value when a parameter is absent                          |

## Method & response annotations

| Annotation                                       | Meaning                         |
|--------------------------------------------------|---------------------------------|
| `@GET` / `@POST` / `@PUT` / `@PATCH` / `@DELETE` | HTTP method                     |
| `@Path("/v1/x")`                                 | Route                           |
| `@Consumes(MediaType.APPLICATION_JSON)`          | Request media type              |
| `@Produces(MediaType.APPLICATION_JSON)`          | Response media type             |
| `@RvStatus(Response.Status.X)`                   | Fixed status code for the route |

## Return types

| Returns                                 | Behavior                                           |
|-----------------------------------------|----------------------------------------------------|
| POJO, `List<T>`, enum, array, primitive | Serialized to JSON; status from `@RvStatus` or 200 |
| `void`                                  | No body; status from `@RvStatus`                   |
| `RvResponse<T>`                         | Full control over the response                     |

## `RvResponse<T>`

| Field          | Purpose                                                        |
|----------------|----------------------------------------------------------------|
| `status`       | `Response.Status` (required)                                   |
| `body`         | JSON/string body payload                                       |
| `bodyUrl`      | Stream a resource as the body (mutually exclusive with `body`) |
| `mediaType`    | `Content-Type`                                                 |
| `headers`      | Extra response headers                                         |
| `redirectPath` | Sends a redirect (`Location`)                                  |
| `error`        | Error text; if set without a status, defaults to `500`         |

## Rules

- DTO parameters do not support inheritance (keeps code generation simple).
- Only `public` DTO fields will be exposed as TypeScript interface fields.
- Non-body methods (`GET`, `DELETE`, `HEAD`, `OPTIONS`) cannot take `@BeanParam` or `@FormParam`.
- `@BeanParam` and `@FormParam` are mutually exclusive.
- Each `(HTTP method, path)` pair must be unique.
- Every `@PathParam` must appear in the method's `@Path`.
- A controller with no handler methods is an error.

## Known limitations

The type mapper intentionally covers only plain DTOs with public fields:

- Raw (non-generic) collection fields map to an empty TypeScript type.
- Wildcard (`List<?>`, `? extends T`) and generic-array (`T[]`) fields are not
  supported and fail type mapping.
- Two distinct Java classes whose simple names collide cannot both be emitted to
  TypeScript; generation fails fast with a `TypeScript interface name clash` error.
- Body (`@BeanParam`) parameter names in the generated client require compiling with
  `-parameters`; otherwise the compiler reports `arg0`.
- Overloaded controller methods sharing a Java name produce duplicate TS exports.

## Results and validations

Controllers may return any object; when the object extends `RvResult` the
response carries an optional `error` string and a `validations` array. Each
validation is a locale-agnostic contract between backend and frontend:

```json
{
  "error": null,
  "validations": [
    {
      "name": "registrationAge",
      "key": "app.i18n.ageValidationFailed",
      "params": {
        "minAge": "18",
        "maxAge": "39"
      }
    }
  ]
}
```

- `key` is the i18n message key. The frontend owns the human-readable sentence
  templates per locale and renders them via its own i18n layer (e.g. `t(key, params)`).
- `params` holds the placeholder values the template interpolates. Values must be
  server-computed context (thresholds, limits) — never echoed user input.
- `name` optionally identifies the attribute the validation applies to, so forms
  can bind the error to the matching input.

```java
return result.validate(r -> {
  r.validations.add(RvValidation.of("app.i18n.ageValidationFailed")
    .withName("registrationAge")
    .withParams(Map.of("minAge", "18", "maxAge", "39")));
  return r;
});
```

## TypeScript client

`ronoveTypescriptRpc` emits one exported RPC stub per controller method, plus
interfaces for every referenced type. Fields are optional when
`optionalFields = true`. `Object` maps to `any`; `void` returns become
`Promise<void>` (prefer `RvResponse<Void>`).

```ts
const data = await v1Echo(42);          // GET /v1/echo/42
await update({field: "value"});       // POST /v1/update (JSON body)
```

The `rv-test` module type-checks the generated clients with the TypeScript 7
native (`tsc`) compiler. The test suite writes them to
`rv-test/build/generated/ronove/`, and the `typescriptVerify` task (wired into
`check`) fails the build if `tsc` reports any error:

```
gradle :rv-test:typescriptVerify
```

See runnable examples at [rv-test](./rv-test/src/test/java/io/vacco/ronove).