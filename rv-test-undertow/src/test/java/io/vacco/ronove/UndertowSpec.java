package io.vacco.ronove;

import static j8spec.J8Spec.*;

import com.google.gson.Gson;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import io.undertow.util.StatusCodes;
import io.vacco.ronove.exampleapi.BookApi;
import io.vacco.ronove.undertow.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import jakarta.ws.rs.HttpMethod;
import org.junit.runner.RunWith;

import java.awt.*;
import java.util.*;
import java.util.List;

import static io.vacco.ronove.undertow.RvHandlers.*;
import static io.vacco.ronove.exampleapi.BookApi.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class UndertowSpec {
  static {
    it("Can generate bindings for a test API", () -> {
      System.out.println(new RvTypescript().render(Collections.singletonList(BookApi.class)));
    });
    it("Can serve an API using Undertow", () -> {
      if (GraphicsEnvironment.isHeadless()) {
        System.out.println("Inside GH actions. Skipping.");
      } else {
        Gson g = new Gson();
        BookApi bookApi = new BookApi();
        RvJsonHandler jh = new RvJsonHandler(new Gson());
        RoutingHandler rh = new RoutingHandler()
            .get(v1BookCatalogIdList, jh.forSupplier(
                ex -> bookApi.v1BookCatalogIdList(queryParamInt(ex, "catalogId")),
                RvHandlers::httpOk
            ))
            .get(v1BookCatalog, jh.forSupplier(
                ex -> bookApi.v1BookCatalog(jh.getHeader(ex, List.class, "catalogIds")),
                RvHandlers::httpOk
            ))
            .get(v1BookGenre, jh.forSupplier(ex -> bookApi.v1BookGenre(queryParamString(ex, "genre"), queryParamString(ex, "sort")), RvHandlers::httpOk))
            .get(v1BookGenreList, jh.forSupplier(ex -> bookApi.v1BookGenreList(), RvHandlers::httpOk))
            .add(HttpMethod.PATCH, v1GenreUpdate, ex -> jh.forBody(List.class, ls -> bookApi.v1GenreUpdate(ls), RvHandlers::httpOk))
            .get(any, ex -> notFound(g, ex, () -> StatusCodes.NOT_FOUND_STRING));

        Undertow server =
            Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(logRequest(rh, ex -> System.out.println(ex.toString())))
                .build();
        server.start();

        Thread.sleep(25000);
      }
    });
  }
}
