package io.vacco.ronove;

import com.google.gson.Gson;
import io.undertow.Undertow;
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
public class UndertowSpec {
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
        RvJsonInput jin = g::fromJson;
        RvJsonOutput jout = g::toJson;
        RvUndertowAdapter<MyBookApi> utBookApi = new RvUndertowAdapter<>(bookApi, jin, jout);

        utBookApi.routingHandler.get(MyBookApi.any, ex -> notFound(jout, ex, () -> StatusCodes.NOT_FOUND_STRING));

        Undertow server =
            Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(logRequest(utBookApi.routingHandler, ex -> System.out.println(ex.toString())))
                .build();

        server.start();
        Thread.sleep(60000);
      }
    });
  }
}
