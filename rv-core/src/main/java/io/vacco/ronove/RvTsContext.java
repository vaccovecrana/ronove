package io.vacco.ronove;

import java.io.Serializable;
import java.lang.reflect.*;
import java.math.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.*;

public class RvTsContext {

  private static final String any = "any";
  private static final String tVoid = "void";
  private static final String tBoolean = "boolean";
  private static final String number = "number";
  private static final String string = "string";
  private static final String date = "Date";

  public final Set<Type> javaTypes = new HashSet<>();

  private static final Map<Serializable, String> tsTypes = new HashMap<>();

  static {
    tsTypes.put(Void.class.getTypeName(), tVoid);
    tsTypes.put(void.class.getTypeName(), tVoid);
    tsTypes.put(Object.class.getTypeName(), any);

    tsTypes.put(byte.class.getTypeName(), number);
    tsTypes.put(Byte.class.getTypeName(), number);
    tsTypes.put(short.class.getTypeName(), number);
    tsTypes.put(Short.class.getTypeName(), number);
    tsTypes.put(int.class.getTypeName(), number);
    tsTypes.put(Integer.class.getTypeName(), number);
    tsTypes.put(long.class.getTypeName(), number);
    tsTypes.put(Long.class.getTypeName(), number);
    tsTypes.put(float.class, number);
    tsTypes.put(Float.class.getTypeName(), number);
    tsTypes.put(double.class.getTypeName(), number);
    tsTypes.put(Double.class.getTypeName(), number);

    tsTypes.put(boolean.class.getTypeName(), tBoolean);
    tsTypes.put(Boolean.class.getTypeName(), tBoolean);

    tsTypes.put(char.class.getTypeName(), string);
    tsTypes.put(Character.class.getTypeName(), string);

    tsTypes.put(String.class.getTypeName(), string);
    tsTypes.put(BigDecimal.class.getTypeName(), number);
    tsTypes.put(BigInteger.class.getTypeName(), number);
    tsTypes.put(Date.class.getTypeName(), date);
    tsTypes.put(UUID.class.getTypeName(), string);
  }

  private Optional<Class<?>> getSchemaClass(Type t) {
    if (t instanceof Class) {
      if (void.class.isAssignableFrom((Class<?>) t) || Void.class.isAssignableFrom((Class<?>) t)) {
        return Optional.empty();
      }
      return Optional.of((Class<?>) t);
    } else if (t instanceof ParameterizedType) {
      var pt = (ParameterizedType) t;
      if (pt.getRawType() instanceof Class) {
        var prc = (Class<?>) pt.getRawType();
        if (!Collection.class.isAssignableFrom(prc)) {
          return Optional.of(prc);
        }
      }
    }
    return Optional.empty();
  }

  public String simpleNameOf(String canonicalName) {
    var classComps = canonicalName.split("\\.");
    return classComps[classComps.length - 1];
  }

  public String simpleNameOf(Type t) {
    var rawClass = t instanceof ParameterizedType
      ? ((ParameterizedType) t).getRawType().getTypeName()
      : t.getTypeName();
    getSchemaClass(t).ifPresent(javaTypes::add);
    return simpleNameOf(rawClass);
  }

  public String tsTypeFormatOf(ParameterizedType t) {
    var rawClass = simpleNameOf(t);
    if (List.class.getSimpleName().equals(rawClass) || Set.class.getSimpleName().equals(rawClass)) {
      return "%s[]";
    }
    return rawClass + "<%s>";
  }

  public String tsTypeOf(Type t) {
    var tClass = (Class<?>) t;
    var tClassTsType = tsTypes.get(t.getTypeName());
    if (tClass.isArray()) {
      var aClass = tClass.getComponentType();
      var aClassTsType = tsTypes.get(aClass.getTypeName());
      return String.format("%s[]", aClassTsType);
    }
    return tClassTsType != null ? tClassTsType : simpleNameOf(t);
  }

  public String tsArgsOf(ParameterizedType opType) {
    var opTypeFmt = tsTypeFormatOf(opType);
    var tType = stream(opType.getActualTypeArguments())
      .map(jt -> jt instanceof ParameterizedType
        ? tsArgsOf((ParameterizedType) jt)
        : tsTypeOf(jt)
      ).collect(Collectors.joining(", "));
    return String.format(opTypeFmt, tType);
  }

  private String tsReturnTypeTail(Type rt) {
    if (rt instanceof TypeVariable) {
      return rt.getTypeName();
    }
    return rt instanceof ParameterizedType
      ? tsArgsOf((ParameterizedType) rt)
      : tsTypeOf(rt);
  }

  public String tsReturnTypeOf(Method m) {
    var rt = m.getGenericReturnType();
    if (rt instanceof ParameterizedType) {
      var pt = ((ParameterizedType) rt);
      var rtr = pt.getRawType();
      if (rtr instanceof Class && RvResponse.class.isAssignableFrom((Class<?>) rtr)) {
        return tsReturnTypeTail(pt.getActualTypeArguments()[0]);
      }
    }
    return tsReturnTypeTail(rt);
  }

  private RvTsType mapEnum(Class<?> jc) {
    var tse = new RvTsType();
    tse.name = jc.getSimpleName();
    tse.type = "const enum";
    for (var ec : jc.getEnumConstants()) {
      tse.enumValues.add(ec.toString());
    }
    return tse;
  }

  private RvTsType mapClass(Class<?> jc) {
    var tsc = new RvTsType();
    tsc.type = "interface";
    tsc.name = jc.getSimpleName();
    if (jc.getTypeParameters().length > 0) {
      tsc.name = String.format(
          "%s<%s>", tsc.name,
          Arrays.stream(jc.getTypeParameters())
              .map(Object::toString)
              .collect(Collectors.joining(", "))
      );
    }
    for (var f : jc.getFields()) {
      var gt = f.getGenericType();
      var ft = (gt instanceof ParameterizedType || gt instanceof TypeVariable)
          ? f.getGenericType()
          : f.getType();
      var prop = new RvTsType();
      prop.name = f.getName();
      prop.type = tsReturnTypeTail(ft);
      tsc.properties.add(prop);
    }
    return tsc;
  }

  @SuppressWarnings("rawtypes")
  public RvTsType mapJavaType(Type jt) {
    if (jt instanceof Class) {
      Class cl = (Class) jt;
      return cl.isEnum() ? mapEnum(cl) : mapClass(cl);
    }
    throw new IllegalArgumentException("Unsupported type " + jt);
  }

  public List<RvTsType> tsSchemaTypes() {
    return javaTypes.stream()
        .map(this::mapJavaType)
        .collect(Collectors.toList());
  }

}
