package io.vacco.ronove;

import jakarta.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

public class RvAnnotations {

  public static boolean isNonBodyJaxRsMethod(Annotation an) {
    var anc = an.getClass();
    return GET.class.isAssignableFrom(anc)
        || DELETE.class.isAssignableFrom(anc)
        || HEAD.class.isAssignableFrom(anc)
        || OPTIONS.class.isAssignableFrom(anc);
  }

  public static boolean isJaxRsMethod(Annotation an) {
    var anc = an.getClass();
    return isNonBodyJaxRsMethod(an)
        || POST.class.isAssignableFrom(anc)
        || PUT.class.isAssignableFrom(anc)
        || PATCH.class.isAssignableFrom(anc);
  }

  public static boolean isJaxRsBodyParam(Annotation an) {
    return BeanParam.class.isAssignableFrom(an.getClass());
  }

  public static boolean isJaxRsParam(Annotation an) {
    var anc = an.getClass();
    return PathParam.class.isAssignableFrom(anc)
        || QueryParam.class.isAssignableFrom(anc)
        || CookieParam.class.isAssignableFrom(anc)
        || FormParam.class.isAssignableFrom(anc)
        || HeaderParam.class.isAssignableFrom(anc)
        || isJaxRsBodyParam(an);
  }

  public static boolean isJaxRsPath(Annotation an) {
    return Path.class.isAssignableFrom(an.getClass());
  }

  public static boolean isJaxRsConsumes(Annotation an) {
    return Consumes.class.isAssignableFrom(an.getClass());
  }

  public static boolean isJaxRsProduces(Annotation an) {
    return Produces.class.isAssignableFrom(an.getClass());
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
            "Parameter [%s] has no Jakarta RESTful annotations, nor attachment parameter annotations. Verify method signature.",
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
