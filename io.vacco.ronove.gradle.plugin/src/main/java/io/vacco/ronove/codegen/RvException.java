package io.vacco.ronove.codegen;

import io.vacco.ronove.core.RvDescriptor;

public class RvException {

  public static class RvDuplicateMappingException extends RuntimeException {
    public final String path;
    public final RvDescriptor newDesc, oldDesc;
    public RvDuplicateMappingException(String path, RvDescriptor newDesc, RvDescriptor oldDesc) {
      super(String.format("Can only map path descriptor once: [%s]", path));
      this.path = path;
      this.newDesc = newDesc;
      this.oldDesc = oldDesc;
    }
  }

}
