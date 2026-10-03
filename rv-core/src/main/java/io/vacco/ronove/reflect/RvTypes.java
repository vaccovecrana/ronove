package io.vacco.ronove.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class RvTypes {

  private static final String any = "any";
  private static final String tVoid = "void";
  private static final String tBoolean = "boolean";
  private static final String number = "number";
  private static final String string = "string";
  private static final String date = "Date";

  public static final Map<Class<?>, String> tsTypes = new HashMap<>();

  static {
    tsTypes.put(Void.class, tVoid);
    tsTypes.put(void.class, tVoid);
    tsTypes.put(Object.class, any);

    tsTypes.put(byte.class, number);
    tsTypes.put(Byte.class, number);
    tsTypes.put(short.class, number);
    tsTypes.put(Short.class, number);
    tsTypes.put(int.class, number);
    tsTypes.put(Integer.class, number);
    tsTypes.put(long.class, number);
    tsTypes.put(Long.class, number);
    tsTypes.put(float.class, number);
    tsTypes.put(Float.class, number);
    tsTypes.put(double.class, number);
    tsTypes.put(Double.class, number);

    tsTypes.put(boolean.class, tBoolean);
    tsTypes.put(Boolean.class, tBoolean);

    tsTypes.put(char.class, string);
    tsTypes.put(Character.class, string);

    tsTypes.put(String.class, string);
    tsTypes.put(BigDecimal.class, number);
    tsTypes.put(BigInteger.class, number);
    tsTypes.put(Date.class, date);
    tsTypes.put(UUID.class, string);
  }

  public static Class<?> toWrapperClass(Class<?> type) {
    if (!type.isPrimitive()) return type;
    else if (int.class.equals(type)) {
      return Integer.class;
    } else if (double.class.equals(type)) {
      return Double.class;
    } else if (char.class.equals(type)) {
      return Character.class;
    } else if (boolean.class.equals(type)) {
      return Boolean.class;
    } else if (long.class.equals(type)) {
      return Long.class;
    } else if (float.class.equals(type)) {
      return Float.class;
    } else if (short.class.equals(type)) {
      return Short.class;
    } else if (byte.class.equals(type)) {
      return Byte.class;
    }
    return type;
  }

  public static boolean isWrapperType(Class<?> type) {
    return type == Boolean.class
      || type == Integer.class
      || type == Character.class
      || type == Byte.class
      || type == Short.class
      || type == Double.class
      || type == Long.class
      || type == Float.class;
  }

  public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
    if (clazz == null) {
      return false;
    }
    return clazz.isPrimitive() || isWrapperType(toWrapperClass(clazz));
  }

  public static boolean isCollection(Class<?> clazz) {
    return clazz != null && Collection.class.isAssignableFrom(clazz);
  }

  public static boolean isVoid(Class<?> clazz) {
    if (clazz == null) {
      return false;
    }
    return void.class.isAssignableFrom(clazz) || Void.class.isAssignableFrom(clazz);
  }

  public static boolean isString(Class<?> clazz) {
    if (clazz == null) {
      return false;
    }
    return String.class.isAssignableFrom(clazz);
  }

  public static Optional<Type> superClass(Class<?> c) {
    if (c.getSuperclass() != null && c.getSuperclass() != Object.class) {
      if (c.getSuperclass() != c.getGenericSuperclass()) {
        return Optional.of(c.getGenericSuperclass());
      } else {
        return Optional.of(c.getSuperclass());
      }
    }
    return Optional.empty();
  }

  public static Type[] genericTypesOf(ParameterizedType pt) {
    if (pt.getRawType() instanceof Class) {
      return pt.getActualTypeArguments();
    }
    return new Type[0];
  }

  public static Optional<Object> instance(Class<?> fType, String rawValue) {
    try {
      if (isPrimitiveOrWrapper(fType)) {
        fType = toWrapperClass(fType);
        var vOf = fType.getMethod("valueOf", String.class);
        return Optional.of(vOf.invoke(null, rawValue));
      } else if (Enum.class.isAssignableFrom(fType)) {
        var eValues = (Object[]) fType.getMethod("values").invoke(null);
        for (var o : eValues) {
          if (o.toString().equalsIgnoreCase(rawValue)) {
            return Optional.of(o);
          }
        }
        throw new IllegalArgumentException(String.format(
          "Enum value not found: [%s, %s]",
          rawValue, Arrays.toString(eValues)
        ));
      } else if (String.class.isAssignableFrom(fType)) {
        return Optional.of(rawValue);
      }
      return Optional.empty();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

}
