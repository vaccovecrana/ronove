package io.vacco.ronove;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A server-side request attachment parameter. Usually this is implemented
 * by server implementations extracting some sort of request information
 * (i.e. a session cookie), validating against the backend, and placing
 * a generated value (i.e. a session object) on the incoming http request.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RvAttachmentParam {
  Class<?> value();
}
