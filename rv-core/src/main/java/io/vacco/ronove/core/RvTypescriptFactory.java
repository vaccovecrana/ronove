package io.vacco.ronove.core;

import java.io.Serializable;
import java.lang.reflect.*;
import java.math.*;
import java.util.*;

public class RvTypescriptFactory {

  private static final String any = "any";
  private static final String tBoolean = "boolean";
  private static final String number = "number";
  private static final String string = "string";
  private static final String date = "Date";

  public final Set<String> tsSchemaTypes = new TreeSet<>();

  private static final Map<Serializable, String> tsTypes = new HashMap<>();

  static {
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
      return Optional.of((Class<?>) t);
    } else if (t instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) t;
      if (pt.getRawType() instanceof Class) {
        Class<?> prc = (Class<?>) pt.getRawType();
        if (!Collection.class.isAssignableFrom(prc)) {
          return Optional.of(prc);
        }
      }
    }
    return Optional.empty();
  }

  public String simpleNameOf(String canonicalName) {
    String[] classComps = canonicalName.split("\\.");
    return classComps[classComps.length - 1];
  }

  public String simpleNameOf(Type t) {
    String rawClass = t instanceof ParameterizedType ?
        ((ParameterizedType) t).getRawType().getTypeName() : t.getTypeName();
    getSchemaClass(t).ifPresent(cl -> tsSchemaTypes.add(simpleNameOf(cl.getCanonicalName())));
    return simpleNameOf(rawClass);
  }

  public String tsTypeFormatOf(ParameterizedType t) {
    String rawClass = simpleNameOf(t);
    if (List.class.getSimpleName().equals(rawClass) || Set.class.getSimpleName().equals(rawClass)) {
      return "%s[]";
    }
    return rawClass + "<%s>";
  }

  public String tsTypeOf(Type t) {
    Class<?> tClass = (Class<?>) t;
    String tClassTsType = tsTypes.get(t.getTypeName());
    if (tClass.isArray()) {
      Class<?> aClass = tClass.getComponentType();
      String aClassTsType = tsTypes.get(aClass.getTypeName());
      return String.format("%s[]", aClassTsType);
    }
    return tClassTsType != null ? tClassTsType : simpleNameOf(t);
  }

  public String tsArgsOf(ParameterizedType opType) {
    String opTypeFmt = tsTypeFormatOf(opType);
    Type tType = opType.getActualTypeArguments()[0];
    return String.format(opTypeFmt,
        tType instanceof ParameterizedType ?
            tsArgsOf((ParameterizedType) tType) : tsTypeOf(tType)
    );
  }

  public String tsReturnTypeOf(Method m) {
    Type rt = m.getGenericReturnType();
    return rt instanceof ParameterizedType ? tsArgsOf((ParameterizedType) rt) : tsTypeOf(rt);
  }

}
