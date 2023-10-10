package io.vacco.ronove;

import jakarta.ws.rs.core.Response;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Sadly, the Jakarta @{@link Response} object has some
 * weird dependency on @{@link jakarta.ws.rs.ext.RuntimeDelegate}
 * that can't play well with Graal applications
 * (i.e. we can't use @{@link Response} directly).
 * So we just use the status codes defined in the former.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RvStatus {
  Response.Status value();
}
