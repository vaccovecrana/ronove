package io.vacco.ronove.plugin;

import io.vacco.ronove.*;
import java.lang.reflect.*;

import static io.vacco.ronove.RvPrimitives.*;
import static io.vacco.ronove.plugin.RvTsPrimitives.*;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.lang.String.format;

public class RvTsDeclarations {

  public static Type[] genericTypesOf(ParameterizedType pt) {
    if (pt.getRawType() instanceof Class) {
      return pt.getActualTypeArguments();
    }
    return new Type[0];
  }

  private static String mapGeneric(Type gt, Type[] gtArgs) {
    var gtTxt = gt instanceof Class ? ((Class<?>) gt).getSimpleName() : mapTail(gt);
    var ptArgsTxt = stream(gtArgs).map(RvTsDeclarations::mapTail).collect(joining(", "));
    if (gt instanceof Class && (isCollection((Class<?>) gt) || ((Class<?>) gt).isArray())) {
      return format("%s[]", ptArgsTxt);
    }
    return format("%s<%s>", gtTxt, ptArgsTxt);
  }

  private static String mapGeneric(ParameterizedType pt) {
    if (pt.getRawType() == RvResponse.class) {
      return mapTail(pt.getActualTypeArguments()[0]);
    }
    return mapGeneric(pt.getRawType(), genericTypesOf(pt));
  }

  private static String mapTail(Type t) {
    if (t instanceof Class) {
      var c = (Class<?>) t;
      if (isPrimitiveOrWrapper(c) || isVoid(c) || isString(c)) {
        return tsTypes.get(c);
      } else if (isCollection(c)) {
        return "";
      } else if (c.isArray()) {
        return format("%s[]", mapTail(c.getComponentType()));
      } else if (c.getTypeParameters().length > 0) {
        return mapGeneric(c, c.getTypeParameters());
      } else {
        return c.getSimpleName();
      }
    } else if (t instanceof ParameterizedType) {
      return mapGeneric((ParameterizedType) t);
    } else if (t instanceof TypeVariable) {
      return ((TypeVariable<?>) t).getName();
    }
    throw new IllegalStateException(
      format("Unable to map type [%s], please file a bug at https://github.com/vaccovecrana/ronove/issues", t)
    );
  }

  public static String mapReturn(Type t) {
    return mapTail(t);
  }

  public static String mapParams(RvDescriptor d) {
    return d.allParams.stream()
      .filter(prm -> !RvAnnotations.isRvAttachmentParam(prm.paramType))
      .map(prm -> format(
        "%s: %s", prm.name.replaceAll("[^a-zA-Z0-9]", ""),
        mapTail(prm.type)
      )).collect(joining(", "));
  }

}
