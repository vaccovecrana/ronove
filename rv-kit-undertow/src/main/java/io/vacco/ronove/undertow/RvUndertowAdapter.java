package io.vacco.ronove.undertow;

import io.undertow.server.*;
import io.undertow.util.*;
import io.vacco.ronove.core.*;

import java.util.*;
import java.util.function.*;

import static io.vacco.ronove.undertow.RvHandlers.*;
import static java.lang.String.format;

public class RvUndertowAdapter<Controller> {

  public final RoutingHandler routingHandler = new RoutingHandler();

  private final RvJsonInput jin;
  private final RvJsonOutput jout;
  private final Controller controller;
  private BiConsumer<HttpServerExchange, Exception> errorHandler;

  public RvUndertowAdapter(Controller c, RvJsonInput jin, RvJsonOutput jout) {
    this.jin = Objects.requireNonNull(jin);
    this.jout = Objects.requireNonNull(jout);
    this.controller = Objects.requireNonNull(c);
    Map<String, RvDescriptor> idx = new RvContext().describe(c.getClass());
    this.errorHandler = (ex, e) -> defaultError(jout, ex, e);
    for (RvDescriptor d : idx.values()) {
      routingHandler.add(d.httpMethodTxt, d.path.value(), forDescriptor(d));
    }
  }

  private HttpHandler wrap(HttpHandler rvh, RvDescriptor rvd) {
    return rvd.beanParam != null ? blockingTask(rvh) : rvh;
  }

  private Object extract(Map<String, Deque<String>> pMap, RvParameter p) {
    Deque<String> dq = pMap.get(p.name);
    if (dq != null) {
      return jin.fromJson(dq.getFirst(), p.type);
    } else if (p.defaultValue != null) {
      return jin.fromJson(p.defaultValue.value(), p.type);
    }
    String msg = format("Missing parameter with no optional value: [%s]", p.paramType.toString().replace("@jakarta.ws.rs.", ""));
    throw new IllegalArgumentException(msg);
  }

  private Object extract(HeaderMap headers, RvParameter p) {
    String val = headers.getFirst(p.name);
    return jin.fromJson(val, p.type);
  }

  private HttpHandler forDescriptor(RvDescriptor rvd) {
    return wrap(ex -> {
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
        if (rvd.beanParam != null) {
          Object in = jin.fromJson(ex.getInputStream(), rvd.beanParam.type);
          params[rvd.beanParam.position] = in;
        }
        Object out = rvd.handler.invoke(controller, params);
        asJson(jout, ex, out, StatusCodes.OK); // TODO so how do we return different success status codes?
      } catch (Exception e) {
        this.errorHandler.accept(ex, e);
      }
    }, rvd);
  }

  public RvUndertowAdapter<Controller> withErrorHandler(BiConsumer<HttpServerExchange, Exception> errorHandler) {
    this.errorHandler = Objects.requireNonNull(errorHandler);
    return this;
  }

}
