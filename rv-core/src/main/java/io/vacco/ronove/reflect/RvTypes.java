package io.vacco.ronove.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class RvTypes {

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

  public static boolean isMap(Class<?> clazz) {
    return clazz != null && Map.class.isAssignableFrom(clazz);
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

  public static boolean isJdkType(Class<?> c) {
    var pkg = c.getPackage();
    var p = pkg != null ? pkg.getName() : "";
    return p.startsWith("java.")
      || p.startsWith("javax.")
      || p.startsWith("jdk.")
      || p.startsWith("sun.")
      || p.startsWith("jakarta.");
  }

  /**
   * Returns the resolved set of application types reachable from an input type
   * collection, suitable for GraalVM reflection registration. JDK/Jakarta types and types
   * GraalVM handles out of the box (collections, maps, primitives, String) are
   * excluded.
   */
  public static List<Class<?>> reflectTypes(Collection<Type> types) {
    var out = new LinkedHashSet<Class<?>>();
    for (var t : types) {
      Class<?> c = null;
      if (t instanceof Class) {
        c = (Class<?>) t;
      } else if (t instanceof ParameterizedType) {
        var raw = ((ParameterizedType) t).getRawType();
        if (raw instanceof Class) {
          c = (Class<?>) raw;
        }
      }
      if (c == null
        || isPrimitiveOrWrapper(c)
        || isVoid(c)
        || isString(c)
        || c.isArray()
        || isCollection(c)
        || isMap(c)
        || isJdkType(c)) {
        continue;
      }
      out.add(c);
    }
    var list = new ArrayList<>(out);
    list.sort(Comparator.comparing(Class::getCanonicalName));
    return list;
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
