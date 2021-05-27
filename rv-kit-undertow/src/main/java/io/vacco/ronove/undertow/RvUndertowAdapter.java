package io.vacco.ronove.undertow;

import com.google.gson.Gson;
import io.undertow.server.RoutingHandler;
import io.vacco.ronove.core.RvContext;
import io.vacco.ronove.core.RvDescriptor;
import io.vacco.ronove.core.RvParameter;

import java.util.Map;
import java.util.Objects;

public class RvUndertowAdapter<Controller> {

  public final RoutingHandler routingHandler = new RoutingHandler();

  private final Gson gson;
  private final Controller controller;

  public RvUndertowAdapter(Controller c, Gson g) {
    this.gson = Objects.requireNonNull(g);
    this.controller = Objects.requireNonNull(c);
    Map<String, RvDescriptor> idx = new RvContext().describe(c.getClass());

    for (RvDescriptor d : idx.values()) {
      routingHandler.add(d.httpMethodTxt, d.path.value(), ex -> {
        for (RvParameter pp : d.pathParams) {
          String pVal = ex.getQueryParameters().get(pp.name).getFirst();
          Object o = gson.fromJson(pVal, pp.type);
        }
      });
    }
  }

}
