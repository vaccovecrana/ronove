package io.vacco.ronove.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A server-side request attachment params. Usually this is implemented
 * by server implementations extracting some sort of request information,
 * validating against the backend, and placing a generated value on the
 * incoming http request.
 *
 * Typical use case, extended user session data.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RvAttachmentParam {
  Class<?> value();
}
