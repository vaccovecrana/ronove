package io.vacco.ronove;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RvOp {
  RvMethod method();
  String path();
}
