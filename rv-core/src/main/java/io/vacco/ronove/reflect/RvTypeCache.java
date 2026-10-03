package io.vacco.ronove.reflect;

import io.vacco.ronove.util.RvResponse;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import static io.vacco.ronove.reflect.RvTypes.*;

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

  private RvType mapEnum(Class<?> c, Type t) {
    var tse = new RvType("const enum", c.getSimpleName(), t);
    for (var ec : c.getEnumConstants()) {
      tse.enumValues.add(ec.toString());
    }
    return tse;
  }

  private RvType mapClass(Class<?> c, Type t, ParameterizedType pt) {
    var ts = new RvType("interface", c.getSimpleName(), t);
    for (var f : c.getFields()) {
      if (f.getDeclaringClass() == c && !Modifier.isTransient(f.getModifiers())) {
        var fts = get(f.getGenericType());
        ts.properties.put(f.getName(), fts);
      }
    }
    if (pt != null) {
      for (var pta : pt.getActualTypeArguments()) {
        get(pta);
      }
    }
    superClass(c).ifPresent(st -> ts.extendz = get(st));
    return ts;
  }

  private RvType map(Type t) {
    if (t instanceof TypeVariable) {
      return new RvType(null, null, t);
    } else if (t instanceof Class) {
      var c = (Class<?>) t;
      if (isPrimitiveOrWrapper(c) || isVoid(c) || isString(c) || isCollection(c) || c.isArray()) {
        var tc = new RvType(null, null, t);
        tc.declaration = "interface";
        return tc;
      } else if (c.isEnum()) {
        return mapEnum(c, t);
      } else {
        return mapClass(c, t, null);
      }
    } else if (t instanceof ParameterizedType) {
      var pt = (ParameterizedType) t;
      return mapClass((Class<?>) pt.getRawType(), t, pt);
    }
    throw new IllegalStateException(
      format("Unable to map type [%s], please file a bug at https://github.com/vaccovecrana/ronove/issues", t)
    );
  }

  public RvType get(Type t) {
    if (idx.containsKey(t.getTypeName())) {
      return idx.get(t.getTypeName());
    }
    var rt = map(t);
    idx.put(t.getTypeName(), rt);
    return rt;
  }

}
