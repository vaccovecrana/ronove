package io.vacco.ronove.core;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RvOp {

  String HEADER_GET = "Rv-Get-Body";

  RvMethod method();
  String path();
}
