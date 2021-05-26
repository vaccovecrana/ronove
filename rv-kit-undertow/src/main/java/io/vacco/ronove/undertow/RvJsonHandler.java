package io.vacco.ronove.undertow;

import com.google.gson.Gson;
import io.undertow.server.*;
import io.undertow.util.*;

import java.io.InputStream;
import java.util.*;
import java.util.function.*;

public class RvJsonHandler {

  private final Gson gson;

  public RvJsonHandler(Gson gson) {
    this.gson = Objects.requireNonNull(gson);
  }

  public void asJson(HttpServerExchange ex, Object response) {
    String json = gson.toJson(response);
    ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
    ex.getResponseSender().send(json);
  }

  public static String asString(InputStream in) {
    Scanner s = new Scanner(in).useDelimiter("\\A");
    return s.next();
  }

  public void notFound(HttpServerExchange ex, Supplier<?> forResponse) {
    ex.setStatusCode(StatusCodes.NOT_FOUND);
    if (forResponse != null) {
      asJson(ex, forResponse.get());
    }
  }

  public <I, O> HttpHandler forBody(Function<I, O> handlerFn) {
    return blockingTask(ex -> {
      String raw = asString(ex.getInputStream());
      I in = jsonFn.apply(raw);
      handlerFn.apply(in);
    });
  }

  public static <O> HttpHandler forSupplier(Function<HttpServerExchange, O> sup) {
    return sup::apply;
  }

}
