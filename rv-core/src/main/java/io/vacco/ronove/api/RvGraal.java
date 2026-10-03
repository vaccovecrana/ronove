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

  /**
   * @return the classes to include in reflection metadata
   */
  Class<?>[] include() default {};

  /**
   * @return
   *   <code>true</code> if these classes should also get included
   *   in TS RPC definitions, <code>false</code> otherwise.
   */
  boolean rpc() default false;

}
