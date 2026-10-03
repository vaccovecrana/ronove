package io.vacco.ronove.reflect;

import io.vacco.ronove.util.RvResponse;
import jakarta.ws.rs.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.ronove.reflect.RvTypes.isCollection;
import static io.vacco.ronove.reflect.RvTypes.isPrimitiveOrWrapper;
import static java.lang.String.format;

public class RvContext {

  public final List<RvController> controllers = new ArrayList<>();
  public final Map<String, RvMethod> paths = new TreeMap<>();
  public final RvTypeCache typeCache = new RvTypeCache();

  public Map<String, RvMethod> describe(List<Class<?>> apis) {
    for (var ct : apis) {
      var rvc = new RvController(ct, typeCache);
      for (var e : rvc.methods.entrySet()) {
        if (paths.containsKey(e.getKey())) {
          var m1 = paths.get(e.getKey());
          var m2 = e.getValue();
          throw new IllegalStateException(format(
            "Request path [%s] mapped by controllers [%s] and [%s]",
            e.getKey(), m1.javaMethod, m2.javaMethod
          ));
        } else {
          paths.put(e.getKey(), e.getValue());
        }
      }
      controllers.add(rvc);
    }
    if (paths.isEmpty()) {
      throw new IllegalStateException(format(
        "No handler methods found for controller classes: %s", apis
      ));
    }
    return paths;
  }

  public Map<String, RvMethod> describe(Class<?> controller) {
    return describe(Collections.singletonList(controller));
  }

  public List<RvType> schemaTypes() {
    var ptl = new HashSet<Type>();
    return typeCache.idx.values().stream()
      .filter(rvt -> {
        var p = rvt.from.getTypeName();
        var isRvRes = p.startsWith(RvResponse.class.getCanonicalName());
        var isJdk = p.startsWith("java.")
          || p.startsWith("javax.")
          || p.startsWith("jdk.")
          || p.startsWith("sun.")
          || p.startsWith("com.sun")
          || p.startsWith("jakarta.");
        if (!(isJdk || isRvRes)) {
          if (rvt.from instanceof Class) {
            var cl = (Class<?>) rvt.from;
            return !(cl.isArray() || isCollection(cl));
          } else if (rvt.from instanceof ParameterizedType) {
            var pt =  (ParameterizedType) rvt.from;
            if (ptl.contains(pt.getRawType())) {
              return false;
            }
            ptl.add(pt.getRawType());
          }
          return true;
        }
        return false;
      })
      .filter(rvt -> {
        if (rvt.from instanceof Class<?>) {
          var c = (Class<?>) rvt.from;
          return !isPrimitiveOrWrapper(c);
        }
        return !(rvt.from instanceof TypeVariable<?>);
      })
      .filter(rvt -> rvt.rpc)
      .sorted(Comparator.comparing(ts0 -> ts0.name))
      .collect(Collectors.toList());
  }

}
