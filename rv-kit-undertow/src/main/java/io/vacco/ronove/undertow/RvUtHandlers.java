package io.vacco.ronove.undertow;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.function.Consumer;

public class RvUtHandlers {

  public static HttpHandler logged(HttpHandler hdl, Consumer<HttpServerExchange> logCons) {
    return (ex) -> {
      logCons.accept(ex);
      hdl.handleRequest(ex);
    };
  }

}
