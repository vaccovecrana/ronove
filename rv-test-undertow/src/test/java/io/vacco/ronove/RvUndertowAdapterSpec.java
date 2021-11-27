package io.vacco.ronove;

import com.google.gson.Gson;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.ExceptionHandler;
import io.undertow.util.StatusCodes;
import io.vacco.ronove.core.*;
import io.vacco.ronove.undertow.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.awt.*;
import java.util.*;

import static j8spec.J8Spec.*;
import static io.vacco.ronove.undertow.RvHandlers.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class RvUndertowAdapterSpec {
  static {
    it("Can generate bindings for a test API", () -> {
      System.out.println(new RvTypescript().render(Collections.singletonList(MyBookApi.class)));
    });
    it("Can serve an API using Undertow", () -> {
      if (GraphicsEnvironment.isHeadless()) {
        System.out.println("Inside GH actions. Skipping.");
      } else {
        Gson g = new Gson();
        MyBookApi bookApi = new MyBookApi();
        RvJsonInput jIn = g::fromJson;
        RvJsonOutput jOut = g::toJson;
        RvUndertowAdapter<MyBookApi> utBookApi = new RvUndertowAdapter<>(bookApi, jIn, jOut);
        HttpHandler errorHdl = forError(jOut, (xc, t) -> t != null ? t.getMessage() : "???");
        HttpHandler notFoundHdl = forJson(jOut, ex -> StatusCodes.NOT_FOUND_STRING, StatusCodes.NOT_FOUND);
        Undertow server =
            Undertow.builder()
                .addHttpListener(8888, "0.0.0.0")
                .setHandler(
                    Handlers.exceptionHandler(
                        forLogging(
                            utBookApi.routingHandler
                                .setFallbackHandler(notFoundHdl)
                                .setInvalidMethodHandler(notFoundHdl),
                            ex -> System.out.println(ex.toString())
                        )
                    ).addExceptionHandler(Exception.class, errorHdl)
                ).build();
        server.start();
        Thread.sleep(60000);
      }
    });
  }
}
