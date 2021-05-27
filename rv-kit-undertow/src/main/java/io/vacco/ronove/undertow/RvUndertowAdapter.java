package io.vacco.ronove.undertow;

import com.google.gson.Gson;
import io.undertow.server.*;
import io.undertow.util.StatusCodes;
import io.vacco.ronove.core.*;

import java.io.*;
import java.util.*;

import static io.vacco.ronove.undertow.RvHandlers.*;

public class RvUndertowAdapter<Controller> {

  public final RoutingHandler routingHandler = new RoutingHandler();

  private final Gson gson;
  private final Controller controller;

  public RvUndertowAdapter(Controller c, Gson g) {
    this.gson = Objects.requireNonNull(g);
    this.controller = Objects.requireNonNull(c);
    Map<String, RvDescriptor> idx = new RvContext().describe(c.getClass());

    for (RvDescriptor d : idx.values()) {
      routingHandler.add(d.httpMethodTxt, d.path.value(), wrap(forDescriptor(d), d));
    }
  }

  private HttpHandler wrap(HttpHandler rvh, RvDescriptor rvd) {
    return rvd.beanParam != null ? blockingTask(rvh) : rvh;
  }

  private HttpHandler forDescriptor(RvDescriptor rvd) {
    return wrap(ex -> {
      Object[] params = new Object[rvd.allParams.size()];
      for (RvParameter pp : rvd.pathParams) {
        String pVal = ex.getQueryParameters().get(pp.name).getFirst();
        params[pp.position] = gson.fromJson(pVal, pp.type);
      }
      for (RvParameter qp : rvd.queryParams) {
        String pVal = ex.getQueryParameters().get(qp.name).getFirst();
        params[qp.position] = gson.fromJson(pVal, qp.type);
      }
      for (RvParameter hp : rvd.headerParams) {
        String pVal = ex.getRequestHeaders().getFirst(hp.name);
        params[hp.position] = gson.fromJson(pVal, hp.type);
      }
      if (rvd.beanParam != null) {
        Object in = gson.fromJson(new BufferedReader(new InputStreamReader(ex.getInputStream())), rvd.beanParam.type);
        params[rvd.beanParam.position] = in;
      }
      Object out = rvd.handler.invoke(controller, params);
      asJson(gson, ex, out, StatusCodes.OK);
    }, rvd);
  }

}
