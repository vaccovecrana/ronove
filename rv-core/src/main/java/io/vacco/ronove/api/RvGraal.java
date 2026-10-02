package io.vacco.ronove.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Place this annotation under a controller class to generate
 * GraalVM reflection metadata for all types associated with
 * the target controller methods, plus any additional required
 * types used by the controller's depednencies (i.e. internal DTO
 * models, DB classes, etc.) included in the <code>value</code> array.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RvGraal {
  Class<?>[] value() default {};
}
