package io.vacco.ronove.undertow;

import io.undertow.server.*;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.*;
import io.vacco.ronove.core.RvJsonOutput;
import jakarta.ws.rs.WebApplicationException;

import java.io.*;
import java.util.function.*;

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

  public static void asJson(RvJsonOutput jout, HttpServerExchange ex, Object response, int statusCode) {
    String json = jout.toJson(response);
    ex.setStatusCode(statusCode);
    ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
    ex.getResponseSender().send(json);
  }

  public static void defaultError(RvJsonOutput jout, HttpServerExchange ex, Exception e) {
    StringWriter sw = new StringWriter();
    PrintWriter out = new PrintWriter(sw);
    e.printStackTrace(out);
    asJson(
        jout, ex, sw.toString(),
        e instanceof WebApplicationException
            ? ((WebApplicationException) e).getResponse().getStatus()
            : StatusCodes.INTERNAL_SERVER_ERROR
    );
  }

  public static void notFound(RvJsonOutput jout, HttpServerExchange ex, Supplier<?> forResponse) {
    asJson(jout, ex, forResponse.get(), StatusCodes.NOT_FOUND);
  }

  public static Integer httpOk() { return StatusCodes.OK; }

}
