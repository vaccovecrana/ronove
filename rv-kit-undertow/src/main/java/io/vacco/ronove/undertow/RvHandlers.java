package io.vacco.ronove.undertow;

import com.google.gson.Gson;
import io.undertow.server.*;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.*;
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

  public static void asJson(Gson gson, HttpServerExchange ex, Object response, int statusCode) {
    String json = gson.toJson(response);
    ex.setStatusCode(statusCode);
    ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
    ex.getResponseSender().send(json);
  }

  public static void defaultError(Gson gson, HttpServerExchange ex, Exception e) {
    StringWriter sw = new StringWriter();
    PrintWriter out = new PrintWriter(sw);
    e.printStackTrace(out);
    asJson(
        gson, ex, sw.toString(),
        e instanceof WebApplicationException
            ? ((WebApplicationException) e).getResponse().getStatus()
            : StatusCodes.INTERNAL_SERVER_ERROR
    );
  }

  public static void notFound(Gson gson, HttpServerExchange ex, Supplier<?> forResponse) {
    asJson(gson, ex, forResponse.get(), StatusCodes.NOT_FOUND);
  }

  public static Integer httpOk() { return StatusCodes.OK; }

}
