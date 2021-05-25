package io.vacco.ronove;

import static j8spec.J8Spec.*;

import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import io.vacco.ronove.codegen.RvContext;
import io.vacco.ronove.exampleapi.BookApi;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.awt.*;
import java.util.Collections;

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
        /*
        Undertow server =
            Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(
                    logRequest(
                        new RoutingHandler()
                            .get(v1Space, uHdl.forSupplier(ex -> assignmentHdl.list()))
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
        */
      }
    });
  }
}
