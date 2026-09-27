package io.vacco.ronove.plugin;

import io.vacco.ronove.RvDescriptor;
import io.vacco.ronove.RvResponse;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.ronove.RvPrimitives.*;
import static io.vacco.ronove.plugin.RvTsDeclarations.genericTypesOf;
import static io.vacco.ronove.plugin.RvTsDeclarations.mapReturn;
import static java.lang.String.format;

public class RvTsContext {

  public final Set<Type> types = new LinkedHashSet<>();

  /**
   * Assembles the resolved type graph reachable from the given controller
   * descriptors. Shared by all code generators.
   */
  public static RvTsContext from(Map<String, RvDescriptor> idx) {
    return new RvTsContext().add(
      idx.values().stream()
        .flatMap(RvDescriptor::allTypes)
        .collect(Collectors.toSet())
    );
  }

  private Optional<Type> superClass(Class<?> c) {
    if (c.getSuperclass() != null && c.getSuperclass() != Object.class) {
      if (c.getSuperclass() != c.getGenericSuperclass()) {
        return Optional.of(c.getGenericSuperclass());
      } else {
        return Optional.of(c.getSuperclass());
      }
    }
    return Optional.empty();
  }

  private void add(Type t) {
    if (t == RvResponse.class || t == Object.class) {
      return;
    }
    if (!types.contains(t)) {
      if (t instanceof Class) {
        var c = (Class<?>) t;
        if (c.isArray()) {
          add(c.getComponentType());
        } else if (!(isVoid(c) || isString(c) || isPrimitiveOrWrapper(c) || c.isEnum())) {
          for (var f : c.getFields()) {
            if (Modifier.isTransient(f.getModifiers())) {
              continue;
            }
            if (f.getType() != f.getGenericType()) {
              add(f.getGenericType());
            } else {
              add(f.getType());
            }
          }
          superClass(c).ifPresent(this::add);
        }
      } else if (t instanceof ParameterizedType) {
        var pt = (ParameterizedType) t;
        var gtl = genericTypesOf(pt);
        for (var gt : gtl) {
          add(gt);
        }
        add(pt.getRawType());
      }
      types.add(t);
    }
  }

  public RvTsContext add(Collection<Type> types) {
    for (var t : types) {
      add(t);
    }
    return this;
  }

  private RvTsType map(Type t) {
    if (t instanceof TypeVariable) {
      return new RvTsType(null, mapReturn(t), t);
    } else if (t instanceof Class) {
      var c = (Class<?>) t;
      if (isPrimitiveOrWrapper(c) || isVoid(c) || isString(c) || isCollection(c) || c.isArray()) {
        return new RvTsType(null, mapReturn(c), t);
      } else if (c.isEnum()) {
        var tse = new RvTsType(c.getSimpleName(), mapReturn(c), t);
        for (var ec : c.getEnumConstants()) {
          tse.enumValues.add(ec.toString());
        }
        return tse;
      } else {
        var ts = new RvTsType(c.getSimpleName(), mapReturn(c), t);
        for (var f : c.getFields()) {
          if (f.getDeclaringClass() == c && !Modifier.isTransient(f.getModifiers())) {
            var fts = map(f.getGenericType()).withName(f.getName());
            ts.properties.add(fts);
          }
        }
        superClass(c).ifPresent(st -> ts.extendz = map(st));
        return ts;
      }
    } else if (t instanceof ParameterizedType) {
      return new RvTsType(null, mapReturn(t), t);
    }
    throw new IllegalStateException(
      format("Unable to map type [%s], please file a bug at https://github.com/vaccovecrana/ronove/issues", t)
    );
  }

  private static boolean isJdkType(Class<?> c) {
    var pkg = c.getPackage();
    var p = pkg != null ? pkg.getName() : "";
    return p.startsWith("java.")
      || p.startsWith("javax.")
      || p.startsWith("jdk.")
      || p.startsWith("sun.")
      || p.startsWith("jakarta.");
  }

  /**
   * Returns the resolved set of application types reachable from the controllers,
   * suitable for GraalVM reflection registration. JDK/Jakarta types and types
   * GraalVM handles out of the box (collections, maps, primitives, String) are
   * excluded.
   */
  public List<Class<?>> reflectTypes() {
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

  public List<RvTsType> schemaTypes() {
    var out = new ArrayList<RvTsType>();
    for (var t : types) {
      if (t instanceof Class) {
        if (isMap((Class<?>) t)) {
          continue;
        }
        var ts = map(t);
        if (((Class<?>) t).getTypeParameters().length > 0) {
          ts.name = ts.type;
        }
        if (!ts.enumValues.isEmpty()) {
          ts.type = "const enum";
        } else {
          ts.type = "interface";
        }
        if (ts.name != null && !ts.name.isEmpty()) {
          out.add(ts);
        }
      }
    }
    return out;
  }

}
