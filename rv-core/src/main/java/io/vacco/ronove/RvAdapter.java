package io.vacco.ronove;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Base class for implementing HTTP server adapters.
 *
 * @param <Api> a target API class to wrap.
 * @param <Hdl> the underlying HTTP request/response handler implementation.
 * @param <Xc>  the underlying HTTP request/response exchange implementation.
 */
public abstract class RvAdapter<Api, Hdl, Xc> {

  public final Api api;
  public final BiConsumer<Xc, Exception> errorHandler;

  public RvAdapter(Api api, BiConsumer<Xc, Exception> errorHandler) {
    this.errorHandler = Objects.requireNonNull(errorHandler);
    this.api = Objects.requireNonNull(api);
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
  public abstract void commitResponse(RvDescriptor rvd, Object res, Xc xc) throws Exception;

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

  public Hdl build() {
    var idx = new RvContext().describe(api.getClass());
    return combine(
      idx.values().stream()
        .map(this::link)
        .collect(Collectors.toList())
    );
  }

  private Object valueOrDefault(RvParameter p, String s) {
    if (s != null) {
      return RvPrimitives
        .instance((Class<?>) p.type, s)
        .orElse(null);
    } else if (p.defaultValue != null) {
      return RvPrimitives.instance(
        (Class<?>) p.type, p.defaultValue.value()
      ).orElse(null);
    }
    return null;
  }

  public RvHandler<Xc> link(RvDescriptor rvd) {
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
          for (var ap: rvd.attachmentParams) {
            params[ap.position] = loadAttachment(ap, (RvAttachmentParam) ap.paramType, xc);
          }
          if (rvd.beanParam != null) {
            params[rvd.beanParam.position] = loadBean(rvd.beanParam, xc);
          }
          var out = rvd.javaMethod.invoke(api, params);
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
