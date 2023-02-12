package io.vacco.ronove;

import com.google.gson.Gson;
import io.undertow.*;
import io.undertow.server.*;
import io.undertow.server.handlers.*;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.util.*;
import io.vacco.ronove.myapi.*;
import io.vacco.ronove.undertow.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;

import java.util.function.BiConsumer;

import static io.undertow.Handlers.exceptionHandler;
import static io.vacco.ronove.undertow.RvUtHandlers.*;
import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class RvUtAdapterTest {

  private static Undertow server;
  private static final Logger log = LoggerFactory.getLogger(RvUtAdapterTest.class);

  static {
    beforeAll(() -> {
      var g = new Gson();
      var jIn = (RvJsonInput) g::fromJson;
      var jOut = (RvJsonOutput) g::toJson;
      var errorHdl = (BiConsumer<HttpServerExchange, Exception>) (xc, ex) -> {
        log.error("Err", ex);
        xc.setStatusCode(500);
        xc.getResponseSender().send("Oops");
      };

      var uk = new RvUtAttachmentKey<MyUser>()
        .withType(MyUser.class)
        .withKey(AttachmentKey.create(MyUser.class));
      var utBookApi = new RvUtAdapter<>(new MyApi(), errorHdl, jIn, jOut, uk).build();
      var sessionHdl = (HttpHandler) xc -> {
        // Perform some sort of authentication in a parent handler,
        // place a derived object, and retrieve it here.
        // This is just a quick example.
        var ses = xc.getAttachment(uk.key);
        if (ses == null) {
          xc.putAttachment(uk.key,
            new MyUser()
              .withNickName("gopher")
              .withAvatarUrl("https://avatar.me/gopher")
          );
        }
        utBookApi.handleRequest(xc);
      };

      server = Undertow.builder()
        .addHttpListener(8080, "localhost")
        .setHandler(
          exceptionHandler(
            logged(
              (xc0 -> {
                var fp = new FormEncodedDataDefinition().create(xc0);
                if (fp != null) {
                  fp.parse(sessionHdl);
                } else {
                  sessionHdl.handleRequest(xc0);
                }
              }),
              ex -> log.info(ex.toString())
            )
          ).addExceptionHandler(
            Exception.class,
            xc -> errorHdl.accept(xc, (Exception) xc.getAttachment(ExceptionHandler.THROWABLE)))
        ).build();
      server.start();
    });

    it("Can serve an API using Undertow", () -> {
      if (server != null) {
        RvRequestRunner.go();
      }
    });

    it("Stops the server", () -> server.stop());
  }
}
