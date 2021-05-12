package io.vacco.ronove.codegen;

import io.vacco.ronove.core.*;

import java.io.Serializable;
import java.lang.reflect.*;
import java.math.*;
import java.util.*;

import static io.vacco.oruzka.util.OzMaps.*;

public class RvDescriptorFactory {

  private static final String any = "any";
  private static final String number = "number";
  private static final String string = "string";
  private static final String date = "Date";

  public final Set<String> tsSchemaTypes = new TreeSet<>();

  private static final Map<Serializable, String> tsTypes = mapOf(
      kv(Object.class.getTypeName(), any),
      kv(byte.class.getTypeName(), number), kv(Byte.class.getTypeName(), number),
      kv(short.class.getTypeName(), number), kv(Short.class.getTypeName(), number),
      kv(int.class.getTypeName(), number), kv(Integer.class.getTypeName(), number),
      kv(long.class.getTypeName(), number), kv(Long.class.getTypeName(), number),
      kv(float.class, number), kv(Float.class.getTypeName(), number),

      kv(double.class.getTypeName(), number), kv(Double.class.getTypeName(), number),
      kv(boolean.class.getTypeName(), number), kv(Boolean.class.getTypeName(), number),
      kv(char.class.getTypeName(), string), kv(Character.class.getTypeName(), string),

      kv(String.class.getTypeName(), string),

      kv(BigDecimal.class.getTypeName(), number), kv(BigInteger.class.getTypeName(), number),
      kv(Date.class.getTypeName(), date),
      kv(UUID.class.getTypeName(), string)
  );

  private Optional<Class<?>> getSchemaclass(Type t) {
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
    getSchemaclass(t).ifPresent(cl -> tsSchemaTypes.add(simpleNameOf(cl.getCanonicalName())));
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

}
