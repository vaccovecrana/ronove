package io.vacco.ronove.api;

import io.vacco.ronove.reflect.RvContext;
import io.vacco.ronove.reflect.RvMethod;
import io.vacco.ronove.reflect.RvParameter;
import io.vacco.ronove.reflect.RvTypes;
import io.vacco.ronove.util.RvResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Base class for implementing HTTP server adapters.
 *
 * @param <Hdl> the underlying HTTP request/response handler implementation.
 * @param <Xc>  the underlying HTTP request/response exchange implementation.
 */
public abstract class RvAdapter<Hdl, Xc> {

  public final BiConsumer<Xc, Exception> errorHandler;

  public RvAdapter(BiConsumer<Xc, Exception> errorHandler) {
    this.errorHandler = Objects.requireNonNull(errorHandler);
  }

  public abstract String loadPath(RvParameter pp, Xc xc);

  public abstract String loadQuery(RvParameter qp, Xc xc);

  public abstract String loadCookie(RvParameter cp, Xc xc);

  public abstract String loadForm(RvParameter fp, Xc xc);

  public abstract String loadHeader(RvParameter hp, Xc xc);

  public abstract Object loadAttachment(RvParameter ap, RvAttachmentParam at, Xc xc);

  public abstract Object loadBean(RvParameter bp, Xc xc);

  public abstract Hdl combine(List<RvHandler<Xc>> handlers);

  /**
   * Commit a response when controller method returns normally.
   *
   * @param rvd the source method descriptor.
   * @param res the raw response payload.
   * @param xc  the target exchange.
   * @throws Exception for any error.
   */
  public abstract void commitResponse(RvMethod rvd, Object res, Xc xc) throws Exception;

  /**
   * Commit a specific response payload. This response may include
   * for example, custom status codes returned by a controller, any
   * errors that occurred during controller execution, etc.
   *
   * @param res a custom generated response payload.
   * @param xc  the target exchange.
   * @throws Exception for any error.
   */
  public abstract void commitResponse(RvResponse<?> res, Xc xc) throws Exception;

  /**
   * Binds every handler method of the given controller instances into a single
   * combined handler. Each controller instance is invoked for its own methods.
   * Passing a single instance is the common case.
   *
   * @param controllers one or more controller instances.
   * @return the combined handler.
   */
  public Hdl build(Object... controllers) {
    Objects.requireNonNull(controllers);
    var classes = new ArrayList<Class<?>>(controllers.length);
    for (var c : controllers) {
      classes.add(Objects.requireNonNull(c, "controller instance").getClass());
    }
    var ctx = new RvContext();
    ctx.describe(classes);
    var handlers = new ArrayList<RvHandler<Xc>>();
    for (var i = 0; i < controllers.length; i++) {
      var instance = controllers[i];
      for (var rvd : ctx.controllers.get(i).methods.values()) {
        handlers.add(link(instance, rvd));
      }
    }
    return combine(handlers);
  }

  private Object valueOrDefault(RvParameter p, String rawValue) {
    if (rawValue != null) {
      return RvTypes
        .instance((Class<?>) p.type.from, rawValue)
        .orElse(null);
    } else if (p.defaultValue != null) {
      return RvTypes.instance(
        (Class<?>) p.type.from, p.defaultValue.value()
      ).orElse(null);
    }
    return null;
  }

  public RvHandler<Xc> link(Object instance, RvMethod rvd) {
    var params = new Object[rvd.allParams.size()];
    return new RvHandler<Xc>()
      .withDescriptor(rvd)
      .withConsumer((xc) -> {
        try {
          for (var pp : rvd.pathParams) {
            params[pp.position] = valueOrDefault(pp, loadPath(pp, xc));
          }
          for (var qp : rvd.queryParams) {
            params[qp.position] = valueOrDefault(qp, loadQuery(qp, xc));
          }
          for (var cp : rvd.cookieParams) {
            params[cp.position] = valueOrDefault(cp, loadCookie(cp, xc));
          }
          for (var fp : rvd.formParams) {
            params[fp.position] = valueOrDefault(fp, loadForm(fp, xc));
          }
          for (var hp : rvd.headerParams) {
            params[hp.position] = valueOrDefault(hp, loadHeader(hp, xc));
          }
          for (var ap : rvd.attachmentParams) {
            params[ap.position] = loadAttachment(ap, (RvAttachmentParam) ap.paramType, xc);
          }
          if (rvd.beanParam != null) {
            params[rvd.beanParam.position] = loadBean(rvd.beanParam, xc);
          }
          var out = rvd.javaMethod.invoke(instance, params);
          if (out instanceof RvResponse) {
            var res = ((RvResponse<?>) out).validate();
            if (rvd.produces != null) {
              res.withMediaType(rvd.produces.value()[0]);
            }
            commitResponse(res, xc);
          } else {
            commitResponse(rvd, out, xc);
          }
        } catch (Exception e) {
          errorHandler.accept(xc, e);
        }
      });
  }

}
