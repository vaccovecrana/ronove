package io.vacco.ronove.core;

import jakarta.ws.rs.core.Response;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RvStatus {
  Response.Status value();
}
