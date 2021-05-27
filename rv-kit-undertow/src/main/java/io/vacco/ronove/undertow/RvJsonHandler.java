package io.vacco.ronove.undertow;

import com.google.gson.Gson;
import io.undertow.server.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import static io.vacco.ronove.undertow.RvHandlers.*;

public class RvJsonHandler {

  private final Gson gson;
  private BiConsumer<HttpServerExchange, Exception> errorHandler;

  public RvJsonHandler(Gson gson) {
    this.gson = Objects.requireNonNull(gson);
    this.errorHandler = (ex, e) -> defaultError(gson, ex, e);
  }

  public RvJsonHandler withErrorHandler(BiConsumer<HttpServerExchange, Exception> errorHandler) {
    this.errorHandler = Objects.requireNonNull(errorHandler);
    return this;
  }

  public <I, O> HttpHandler forBody(Class<I> payloadType, Function<I, O> handlerFn, Supplier<Integer> statusCode) {
    return blockingTask(ex -> {
      try {
        I in = gson.fromJson(new BufferedReader(new InputStreamReader(ex.getInputStream())), payloadType);
        asJson(gson, ex, handlerFn.apply(in), statusCode.get());
      } catch (Exception e) {
        errorHandler.accept(ex, e);
      }
    });
  }

  public <O> O getHeader(HttpServerExchange ex, Class<O> headerType, String headerKey) {
    return gson.fromJson(ex.getRequestHeaders().getFirst(headerKey), headerType);
  }

  public <O> HttpHandler forSupplier(Function<HttpServerExchange, O> response, Supplier<Integer> statusCode) {
    return ex -> {
      try {
        asJson(gson, ex, response.apply(ex), statusCode.get());
      } catch (Exception e) {
        errorHandler.accept(ex, e);
      }
    };
  }

}
