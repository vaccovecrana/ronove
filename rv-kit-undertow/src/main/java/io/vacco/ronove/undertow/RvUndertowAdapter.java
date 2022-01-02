package io.vacco.ronove.undertow;

import io.undertow.server.*;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.*;
import io.vacco.ronove.core.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.vacco.ronove.undertow.RvHandlers.*;
import static java.lang.String.format;

public class RvUndertowAdapter<Controller> {

  private final RoutingHandler routingHandler = new RoutingHandler();
  private final RvJsonInput jIn;
  private final RvJsonOutput jOut;
  private final Controller controller;

  private BiFunction<HttpServerExchange, Class<?>, Object> attachmentFn;
  private Function<HttpHandler, BlockingHandler> blockingHandlerCustomizer;

  public RvUndertowAdapter(Controller c, RvJsonInput jIn, RvJsonOutput jOut) {
    this.jIn = Objects.requireNonNull(jIn);
    this.jOut = Objects.requireNonNull(jOut);
    this.controller = Objects.requireNonNull(c);
  }

  public RoutingHandler build() {
    Map<String, RvDescriptor> idx = new RvContext().describe(controller.getClass());
    for (RvDescriptor d : idx.values()) {
      routingHandler.add(d.httpMethodTxt, d.path.value(), forDescriptor(d));
    }
    return routingHandler;
  }

  private HttpHandler wrap(HttpHandler rvh, RvDescriptor rvd, Function<HttpHandler, BlockingHandler> customizer) {
    return rvd.beanParam != null ? forBlocking(rvh, customizer) : rvh;
  }

  private Object extract(Map<String, Deque<String>> pMap, RvParameter p) {
    Deque<String> dq = pMap.get(p.name);
    if (dq != null) {
      return jIn.fromJson(dq.getFirst(), p.type);
    } else if (p.defaultValue != null) {
      return jIn.fromJson(p.defaultValue.value(), p.type);
    }
    String msg = format("Missing parameter with no default value: [%s]", p.paramType.toString().replace("@jakarta.ws.rs.", ""));
    throw new IllegalArgumentException(msg);
  }

  private Object extract(HeaderMap headers, RvParameter p) {
    String val = headers.getFirst(p.name);
    return jIn.fromJson(val, p.type);
  }

  private HttpHandler forDescriptor(RvDescriptor rvd) {
    return wrap(
        forJson(jOut, ex -> {
          try {
            Object[] params = new Object[rvd.allParams.size()];
            for (RvParameter pp : rvd.pathParams) {
              params[pp.position] = extract(ex.getQueryParameters(), pp);
            }
            for (RvParameter qp : rvd.queryParams) {
              params[qp.position] = extract(ex.getQueryParameters(), qp);
            }
            for (RvParameter hp : rvd.headerParams) {
              params[hp.position] = extract(ex.getRequestHeaders(), hp);
            }
            if (!rvd.attachmentParams.isEmpty()) {
              if (attachmentFn == null) {
                String msg = String.format("Missing request attachment processor for parameters %s", rvd.attachmentParams);
                throw new IllegalStateException(msg);
              }
              for (RvParameter ap: rvd.attachmentParams) {
                RvAttachmentParam attP = (RvAttachmentParam) ap.paramType;
                params[ap.position] = attachmentFn.apply(ex, attP.value());
              }
            }
            if (rvd.beanParam != null) {
              Object in = jIn.fromJson(ex.getInputStream(), rvd.beanParam.type);
              params[rvd.beanParam.position] = in;
            }
            return rvd.handler.invoke(controller, params);
          } catch (Exception e) {
            if (e instanceof RvException.RvApplicationException) {
              throw (RvException.RvApplicationException) e;
            }
            else {
              throw new RvException.RvApplicationException(e, null, null);
            }
          }
        }, rvd.httpStatus != null ? rvd.httpStatus.value().getStatusCode() : StatusCodes.OK),
        rvd, blockingHandlerCustomizer
    );
  }

  public RvUndertowAdapter<Controller> withAttachmentProcessor(BiFunction<HttpServerExchange, Class<?>, Object> attachmentFn) {
    this.attachmentFn = attachmentFn;
    return this;
  }

  public RvUndertowAdapter<Controller> withBlockingHandlerCustomizer(Function<HttpHandler, BlockingHandler> customizer) {
    this.blockingHandlerCustomizer = customizer;
    return this;
  }

}
