package io.vacco.ronove.plugin;

import io.vacco.ronove.RvResponse;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.ronove.plugin.RvTsDeclarations.*;
import static io.vacco.ronove.RvPrimitives.*;
import static java.lang.String.format;

public class RvTsContext {

  public final Set<Type> types = new LinkedHashSet<>();

  private void add(Type t) {
    if (t == RvResponse.class) {
      return;
    }
    if (!types.contains(t)) {
      if (t instanceof Class) {
        var c = (Class<?>) t;
        if (c.isArray()) {
          add(c.getComponentType());
        } else if (!(isVoid(c) || isString(c) || isPrimitiveOrWrapper(c) || c.isEnum())) {
          for (var f : c.getFields()) {
            if (f.getType() != f.getGenericType()) {
              add(f.getGenericType());
            } else {
              add(f.getType());
            }
          }
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
      return new RvTsType(null, mapReturn(t));
    } else if (t instanceof Class) {
      var c = (Class<?>) t;
      if (isPrimitiveOrWrapper(c) || isVoid(c) || isString(c) || isCollection(c) || c.isArray()) {
        return new RvTsType(null, mapReturn(c));
      } else if (c.isEnum()) {
        var tse = new RvTsType(c.getSimpleName(), mapReturn(c));
        for (var ec : c.getEnumConstants()) {
          tse.enumValues.add(ec.toString());
        }
        return tse;
      } else {
        var ts = new RvTsType(c.getSimpleName(), mapReturn(c));
        for (var f : c.getFields()) {
          var fts = map(f.getGenericType()).withName(f.getName());
          ts.properties.add(fts);
        }
        return ts;
      }
    } else if (t instanceof ParameterizedType) {
      return new RvTsType(null, mapReturn(t));
    }
    throw new IllegalStateException(
        format("Unable to map type [%s], please file a bug at https://github.com/vaccovecrana/ronove/issues", t)
    );
  }

  public List<RvTsType> schemaTypes() {
    var out = new ArrayList<RvTsType>();
    for (var t : types) {
      if (t instanceof Class) {
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
