package io.vacco.ronove.undertow;

import io.undertow.server.*;
import java.util.function.*;

public class RvUtHandlers {

  public static HttpHandler logged(HttpHandler hdl, Consumer<HttpServerExchange> logCons) {
    return (ex) -> {
      logCons.accept(ex);
      hdl.handleRequest(ex);
    };
  }

}
