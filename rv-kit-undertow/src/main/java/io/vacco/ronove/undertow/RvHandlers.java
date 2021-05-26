package io.vacco.ronove.undertow;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;

import java.util.function.Consumer;

public class RvHandlers {

  public static HttpHandler logRequest(HttpHandler hdl, Consumer<HttpServerExchange> logCons) {
    return (ex) -> {
      logCons.accept(ex);
      hdl.handleRequest(ex);
    };
  }

  public static HttpHandler blockingTask(HttpHandler hdl) {
    return new BlockingHandler(hdl);
  }

}
