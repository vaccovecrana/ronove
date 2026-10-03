package io.vacco.ronove.api;

import io.vacco.ronove.reflect.RvMethod;

import java.util.Objects;
import java.util.function.Consumer;

public class RvHandler<Xc> {

  public RvMethod descriptor;
  public Consumer<Xc> consumer;

  public RvHandler<Xc> withDescriptor(RvMethod descriptor) {
    this.descriptor = Objects.requireNonNull(descriptor);
    return this;
  }

  public RvHandler<Xc> withConsumer(Consumer<Xc> consumer) {
    this.consumer = Objects.requireNonNull(consumer);
    return this;
  }

}
