package io.vacco.ronove.reflect;

import io.vacco.ronove.util.RvResponse;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static io.vacco.ronove.reflect.RvTypes.*;
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
    resolveRpc();
    return paths;
  }

  public Map<String, RvMethod> describe(Class<?> controller) {
    return describe(Collections.singletonList(controller));
  }

  /**
   * Marks every cached type reachable from an RPC root (response types,
   * non-attachment parameters and {@code @RvGraal(rpc = true)} includes) as
   * {@code rpc}. Attachment parameters are server-side internals and are kept
   * out of the TypeScript schema unless they are also reachable from a real
   * RPC type.
   */
  private void resolveRpc() {
    var roots = new ArrayList<RvType>();
    for (var rvc : controllers) {
      if (rvc.graal != null) {
        for (var inc : rvc.graal.include()) {
          var irt = typeCache.get(inc);
          if (rvc.graal.rpc()) {
            roots.add(irt);
          }
        }
      }
      for (var m : rvc.methods.values()) {
        roots.add(m.responseType);
        for (var p : m.allParams) {
          if (!RvAnnotations.isRvAttachmentParam(p.paramType)) {
            roots.add(p.type);
          }
        }
      }
    }
    for (var rt : typeCache.idx.values()) {
      rt.rpc = false;
    }
    var visited = new HashSet<RvType>();
    var stack = new ArrayDeque<RvType>();
    for (var rt : roots) {
      if (visited.add(rt)) {
        stack.push(rt);
      }
    }
    while (!stack.isEmpty()) {
      var rt = stack.pop();
      rt.rpc = true;
      for (var p : rt.properties.values()) {
        if (visited.add(p)) {
          stack.push(p);
        }
      }
      if (rt.extendz != null && visited.add(rt.extendz)) {
        stack.push(rt.extendz);
      }
      if (rt.from instanceof Class && ((Class<?>) rt.from).isArray()) {
        var art = typeCache.get(((Class<?>) rt.from).getComponentType());
        if (visited.add(art)) {
          stack.push(art);
        }
      } else if (rt.from instanceof ParameterizedType) {
        for (var pta : ((ParameterizedType) rt.from).getActualTypeArguments()) {
          var art = typeCache.get(pta);
          if (visited.add(art)) {
            stack.push(art);
          }
        }
      }
    }
  }

  private static Class<?> rawClass(Type t) {
    if (t instanceof Class) {
      return (Class<?>) t;
    } else if (t instanceof ParameterizedType) {
      var raw = ((ParameterizedType) t).getRawType();
      return raw instanceof Class ? (Class<?>) raw : null;
    }
    return null;
  }

  private static boolean isJdkType(Class<?> c) {
    var pkg = c.getPackage();
    var p = pkg != null ? pkg.getName() : "";
    return p.startsWith("java.")
      || p.startsWith("javax.")
      || p.startsWith("jdk.")
      || p.startsWith("sun.")
      || p.startsWith("com.sun")
      || p.startsWith("jakarta.");
  }

  private static boolean isMetadataType(Class<?> raw) {
    return !(isPrimitiveOrWrapper(raw)
      || isVoid(raw)
      || isString(raw)
      || raw.isArray()
      || isCollection(raw)
      || isJdkType(raw)
      || raw == RvResponse.class);
  }

  /**
   * Returns every type that must be registered for GraalVM reflection: the
   * controller classes plus all application types in the resolved graph,
   * regardless of whether they are part of the TypeScript RPC schema. Types
   * reachable only from attachment parameters or {@code @RvGraal(rpc = false)}
   * includes are included here (but excluded from {@link #schemaTypes()}).
   */
  public List<Type> metadataTypes() {
    var out = new LinkedHashSet<Class<?>>();
    for (var rvc : controllers) {
      out.add(rvc.clazz);
    }
    for (var rvt : typeCache.idx.values()) {
      var raw = rawClass(rvt.from);
      if (raw != null && isMetadataType(raw)) {
        out.add(raw);
      }
    }
    var list = new ArrayList<Type>(out);
    list.sort(Comparator.comparing(t -> ((Class<?>) t).getTypeName()));
    return list;
  }

  public List<RvType> schemaTypes() {
    var seen = new HashSet<Class<?>>();
    var out = new ArrayList<RvType>();
    for (var rvt : typeCache.idx.values()) {
      var raw = rawClass(rvt.from);
      if (raw == null || !isMetadataType(raw) || !rvt.rpc || !seen.add(raw)) {
        continue;
      }
      out.add(rvt);
    }
    validateTsNames(out);
    out.sort(Comparator.comparing(t -> t.name));
    return out;
  }

  private void validateTsNames(List<RvType> types) {
    var byName = new HashMap<String, Class<?>>();
    for (var rvt : types) {
      var raw = rawClass(rvt.from);
      if (raw == null || rvt.name == null) {
        continue;
      }
      var prev = byName.putIfAbsent(rvt.name, raw);
      if (prev != null && prev != raw) {
        throw new IllegalStateException(format(
          "TypeScript interface name clash: [%s] and [%s] both map to interface [%s]",
          prev.getName(), raw.getName(), rvt.name
        ));
      }
    }
  }

}
