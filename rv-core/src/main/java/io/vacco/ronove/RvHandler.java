package io.vacco.ronove;

import java.util.Objects;
import java.util.function.Consumer;

public class RvHandler<Xc> {

  public RvDescriptor descriptor;
  public Consumer<Xc> consumer;

  public RvHandler<Xc> withDescriptor(RvDescriptor descriptor) {
    this.descriptor = Objects.requireNonNull(descriptor);
    return this;
  }

  public RvHandler<Xc> withConsumer(Consumer<Xc> consumer) {
    this.consumer = Objects.requireNonNull(consumer);
    return this;
  }
}
