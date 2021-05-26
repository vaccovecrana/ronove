package io.vacco.ronove;

import static j8spec.J8Spec.*;

import com.google.gson.Gson;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import io.vacco.ronove.codegen.RvContext;
import io.vacco.ronove.exampleapi.BookApi;
import io.vacco.ronove.undertow.RvJsonHandler;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.awt.*;
import java.util.Collections;
import java.util.function.Function;

import static io.vacco.ronove.undertow.RvHandlers.*;
import static io.vacco.ronove.exampleapi.BookApi.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class UndertowSpec {
  static {
    it("Can generate bindings for a test API", () -> {
      System.out.println(new RvContext().render(Collections.singletonList(BookApi.class)));
    });
    it("Can serve an API using Undertow", () -> {
      if (GraphicsEnvironment.isHeadless()) {
        System.out.println("Inside GH actions. Skipping.");
      } else {
        Gson g = new Gson();
        Function<?, String> jOut = g::toJson;
        BookApi bookService = new BookApi();
        RvJsonHandler jh = new RvJsonHandler(new Gson());
        Undertow server =
            Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(
                    logRequest(
                        new RoutingHandler()
                            .get(v1BookCatalogIdList, ex -> )
                            .get(v1, uHdl.forSupplier(ex -> assignmentHdl.list()))
                            .post(v1Space, uHdl.forBody(Space.class, assignmentHdl::add))
                            .delete(
                                v1Space,
                                uHdl.forSupplier(
                                    ex ->
                                        assignmentHdl.delete(
                                            ex.getQueryParameters().get(spaceId).getFirst())))
                            .get(
                                v1SpaceUser,
                                uHdl.forSupplier(
                                    ex ->
                                        assignmentHdl.load(
                                            ex.getQueryParameters().get(userIdentity).getFirst())))
                            .get(health, AbUtilHdl::healthHdl)
                            .get(any, uHdl::notFound)))
                .build();
        server.start();
      }
    });
  }
}
