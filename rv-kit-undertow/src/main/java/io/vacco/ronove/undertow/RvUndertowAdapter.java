package io.vacco.ronove.undertow;

import io.undertow.server.RoutingHandler;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class RvUndertowAdapter<Controller> {

  public final RoutingHandler routingHandler = new RoutingHandler();
  private final Controller controller;

  public RvUndertowAdapter(Controller c) {
    this.controller = Objects.requireNonNull(c);
  }



}
