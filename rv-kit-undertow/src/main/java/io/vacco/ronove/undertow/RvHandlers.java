package io.vacco.ronove.undertow;

import io.undertow.server.*;
import io.undertow.server.handlers.*;
import io.undertow.util.*;
import io.vacco.ronove.core.*;

import java.io.*;
import java.util.function.*;

public class RvHandlers {

  public static HttpHandler forLogging(HttpHandler hdl, Consumer<HttpServerExchange> logCons) {
    return (ex) -> {
      logCons.accept(ex);
      hdl.handleRequest(ex);
    };
  }

  public static HttpHandler forBlocking(HttpHandler hdl) {
    return new BlockingHandler(hdl);
  }

  public static HttpHandler forJson(RvJsonOutput jOut, Function<HttpServerExchange, ?> bodyFn, int statusCode) {
    return ex -> {
      ex.setStatusCode(statusCode);
      ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
      Object body = bodyFn.apply(ex);
      if (body != null) {
        String json = jOut.toJson(body);
        ex.getResponseSender().send(json);
      }
    };
  }
  public static HttpHandler forError(RvJsonOutput jOut, BiFunction<HttpServerExchange, Throwable, ?> bodyFn) {
    return ex -> {
      Throwable t = ex.getAttachment(ExceptionHandler.THROWABLE);
      forJson(
          jOut, ex0 -> bodyFn.apply(ex0, t),
          t instanceof RvException.RvApplicationException
              ? ((RvException.RvApplicationException) t).status.getStatusCode()
              : StatusCodes.INTERNAL_SERVER_ERROR
      ).handleRequest(ex);
    };
  }

  public static String dumpStack(Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter out = new PrintWriter(sw);
    if (t != null) { t.printStackTrace(out); }
    return t != null ? sw.toString() : "?";
  }

}
