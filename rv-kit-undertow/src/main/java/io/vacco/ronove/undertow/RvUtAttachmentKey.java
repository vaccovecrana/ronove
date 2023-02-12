package io.vacco.ronove.undertow;

import io.undertow.util.AttachmentKey;
import java.util.Objects;

/**
 * WTF maaaan? Undertow's @{@link AttachmentKey} class is so unfriendly to extension lol.
 * At least make it implement equals() and hashCode(), jeez...
 * @param <T> the key type.
 */
public class RvUtAttachmentKey<T> {

  public Class<T> type;
  public AttachmentKey<T> key;

  public RvUtAttachmentKey<T> withType(Class<T> type) {
    this.type = Objects.requireNonNull(type);
    return this;
  }

  public RvUtAttachmentKey<T> withKey(AttachmentKey<T> key) {
    this.key = Objects.requireNonNull(key);
    return this;
  }
}
