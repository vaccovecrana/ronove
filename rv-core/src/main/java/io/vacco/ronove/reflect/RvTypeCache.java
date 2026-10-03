package io.vacco.ronove.reflect;

import io.vacco.ronove.util.RvResponse;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import static io.vacco.ronove.reflect.RvTypes.*;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public class RvTypeCache {

  public final Map<String, RvType> idx = new TreeMap<>();

  public String nameParams(RvMethod d) {
    return d.allParams.stream()
      .filter(prm -> !RvAnnotations.isRvAttachmentParam(prm.paramType))
      .map(prm -> format(
        "%s: %s", prm.name.replaceAll("[^a-zA-Z0-9]", ""),
        nameTail(prm.type.from)
      )).collect(joining(", "));
  }

  private String nameGeneric(Type gt, Type[] gtArgs) {
    var gtTxt = gt instanceof Class ? ((Class<?>) gt).getSimpleName() : nameTail(gt);
    var ptArgsTxt = stream(gtArgs).map(this::nameTail).collect(joining(", "));
    if (gt instanceof Class && (isCollection((Class<?>) gt) || ((Class<?>) gt).isArray())) {
      return format("%s[]", ptArgsTxt);
    }
    return format("%s<%s>", gtTxt, ptArgsTxt);
  }

  private String nameGeneric(ParameterizedType pt) {
    if (pt.getRawType() == RvResponse.class) {
      return nameTail(pt.getActualTypeArguments()[0]);
    }
    return nameGeneric(pt.getRawType(), genericTypesOf(pt));
  }

  public String nameGenericRaw(Type t) {
    if (t instanceof ParameterizedType) {
      var pt = (ParameterizedType) t;
      var cl = (Class<?>) pt.getRawType();
      var ptn = format(
        "%s<%s>",
        cl.getSimpleName(),
        Arrays.stream(cl.getTypeParameters())
          .map(TypeVariable::getName)
          .collect(joining(", "))
      );
      return ptn;
    }
    return nameTail(t);
  }

  private String nameTail(Type t) {
    if (t instanceof Class) {
      var c = (Class<?>) t;
      if (isPrimitiveOrWrapper(c) || isVoid(c) || isString(c)) {
        return tsTypes.get(c);
      } else if (isCollection(c)) {
        return "";
      } else if (c.isArray()) {
        return format("%s[]", nameTail(c.getComponentType()));
      } else if (c.getTypeParameters().length > 0) {
        return nameGeneric(c, c.getTypeParameters());
      } else {
        return c.getSimpleName();
      }
    } else if (t instanceof ParameterizedType) {
      return nameGeneric((ParameterizedType) t);
    } else if (t instanceof TypeVariable) {
      return ((TypeVariable<?>) t).getName();
    }
    throw new IllegalStateException(
      format("Unable to map type [%s], please file a bug at https://github.com/vaccovecrana/ronove/issues", t)
    );
  }

  public String nameReturn(Type t) {
    return nameTail(t);
  }

  /**
   * Creates an empty {@link RvType} node for the given type. The node is
   * registered in the cache before its properties are populated, so that
   * self-referential or mutually-referential DTO graphs resolve to the same
   * node instead of recursing indefinitely.
   */
  private RvType create(Type t) {
    if (t instanceof TypeVariable) {
      return new RvType(null, null, t);
    } else if (t instanceof Class) {
      var c = (Class<?>) t;
      if (c.isEnum()) {
        return new RvType("const enum", c.getSimpleName(), t);
      }
      if (isPrimitiveOrWrapper(c) || isVoid(c) || isString(c) || isCollection(c) || c.isArray()) {
        var tc = new RvType(null, null, t);
        tc.declaration = "interface";
        return tc;
      }
      return new RvType("interface", c.getSimpleName(), t);
    } else if (t instanceof ParameterizedType) {
      var c = (Class<?>) ((ParameterizedType) t).getRawType();
      if (c.isEnum()) {
        return new RvType("const enum", c.getSimpleName(), t);
      }
      return new RvType("interface", c.getSimpleName(), t);
    }
    throw new IllegalStateException(
      format("Unable to map type [%s], please file a bug at https://github.com/vaccovecrana/ronove/issues", t)
    );
  }

  private void populate(Type t, RvType rt) {
    if (t instanceof Class) {
      var c = (Class<?>) t;
      if (isPrimitiveOrWrapper(c) || isVoid(c) || isString(c) || isCollection(c)) {
        return;
      }
      if (c.isArray()) {
        get(c.getComponentType());
        return;
      }
      if (c.isEnum()) {
        for (var ec : c.getEnumConstants()) {
          rt.enumValues.add(ec.toString());
        }
        return;
      }
      mapClass(c, rt, null);
    } else if (t instanceof ParameterizedType) {
      var pt = (ParameterizedType) t;
      mapClass((Class<?>) pt.getRawType(), rt, pt);
    }
  }

  private void mapClass(Class<?> c, RvType ts, ParameterizedType pt) {
    for (var f : c.getFields()) {
      if (f.getDeclaringClass() == c && !Modifier.isTransient(f.getModifiers())) {
        ts.properties.put(f.getName(), get(f.getGenericType()));
      }
    }
    if (pt != null) {
      for (var pta : pt.getActualTypeArguments()) {
        get(pta);
      }
    }
    superClass(c).ifPresent(st -> ts.extendz = get(st));
  }

  public RvType get(Type t) {
    var key = t.getTypeName();
    var existing = idx.get(key);
    if (existing != null) {
      return existing;
    }
    var rt = create(t);
    idx.put(key, rt);
    populate(t, rt);
    return rt;
  }

}
