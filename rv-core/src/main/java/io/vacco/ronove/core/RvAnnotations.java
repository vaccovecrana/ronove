package io.vacco.ronove.core;

import jakarta.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

public class RvAnnotations {

  public static boolean isNonBodyJaxRsMethod(Annotation an) {
    Class<? extends Annotation> anc = an.getClass();
    return GET.class.isAssignableFrom(anc)
        || DELETE.class.isAssignableFrom(anc)
        || HEAD.class.isAssignableFrom(anc)
        || OPTIONS.class.isAssignableFrom(anc);
  }

  public static boolean isJaxRsMethod(Annotation an) {
    Class<? extends Annotation> anc = an.getClass();
    return isNonBodyJaxRsMethod(an)
        || POST.class.isAssignableFrom(anc)
        || PUT.class.isAssignableFrom(anc)
        || PATCH.class.isAssignableFrom(anc);
  }

  public static boolean isJaxRsBodyParam(Annotation an) {
    return BeanParam.class.isAssignableFrom(an.getClass());
  }

  public static boolean isJaxRsParam(Annotation an) {
    Class<? extends Annotation> anc = an.getClass();
    return QueryParam.class.isAssignableFrom(anc)
        || PathParam.class.isAssignableFrom(anc)
        || HeaderParam.class.isAssignableFrom(anc)
        || isJaxRsBodyParam(an);
  }

  public static boolean isJaxRsPath(Annotation an) {
    return Path.class.isAssignableFrom(an.getClass());
  }

  public static boolean isRvStatus(Annotation an) {
    return RvStatus.class.isAssignableFrom(an.getClass());
  }

  public static boolean isRvAttachmentParam(Annotation an) { return RvAttachmentParam.class.isAssignableFrom(an.getClass()); }

  public static Annotation paramTypeOf(Parameter p) {
    return Arrays.stream(p.getAnnotations())
        .filter(an -> RvAnnotations.isJaxRsParam(an) || RvAnnotations.isRvAttachmentParam(an))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(String.format(
            "Parameter [%s] has no query, path, header or bean jax-rs parameter annotations, nor attachment parameter annotations. Verify method signature.",
            p
        )));
  }

  public static Optional<DefaultValue> defaultValueOf(Parameter p) {
    return Arrays.stream(p.getAnnotations())
        .filter(an -> DefaultValue.class.isAssignableFrom(an.getClass()))
        .map(an -> (DefaultValue) an)
        .findFirst();
  }

}
