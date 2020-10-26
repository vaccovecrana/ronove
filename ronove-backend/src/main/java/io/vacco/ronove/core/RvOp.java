package io.vacco.ronove.core;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RvOp {
  RvMethod method();
  String path();
}
