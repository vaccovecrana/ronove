package io.vacco.ronove.core;

import jakarta.ws.rs.core.Response;

public class RvException {

  public static class RvDuplicateMappingException extends RuntimeException {

    public static final long serialVersionUID = 1;
    public final String path;
    public final RvDescriptor newDesc, oldDesc;

    public RvDuplicateMappingException(String path, RvDescriptor newDesc, RvDescriptor oldDesc) {
      super(String.format("Can only map path descriptor once: [%s]", path));
      this.path = path;
      this.newDesc = newDesc;
      this.oldDesc = oldDesc;
    }
  }

  // We originally planned to use WebApplicationException, but found that it needs a RuntimeDelegate implementation.
  // ... sadness ensues...
  public static class RvApplicationException extends RuntimeException {

    public static final long serialVersionUID = 1;
    public final Response.Status status;

    public RvApplicationException(Throwable cause, Response.Status status) {
      super(cause);
      this.status = status == null ? Response.Status.INTERNAL_SERVER_ERROR : status;
    }

    public RvApplicationException(Throwable cause) {
      this(cause, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
