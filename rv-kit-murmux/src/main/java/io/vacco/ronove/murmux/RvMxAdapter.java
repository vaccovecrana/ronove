package io.vacco.ronove.murmux;

import io.vacco.murmux.http.*;
import io.vacco.murmux.middleware.MxRouter;
import io.vacco.ronove.*;
import java.util.*;
import java.util.function.BiConsumer;

import static io.vacco.murmux.http.MxExchanges.*;

public class RvMxAdapter<Api> extends RvAdapter<Api, MxHandler, MxExchange> {

  private final RvJsonInput   jIn;
  private final RvJsonOutput  jOut;
  private final MxRouter      router = new MxRouter();

  public RvMxAdapter(Api api, BiConsumer<MxExchange, Exception> errorHandler,
                     RvJsonInput jIn, RvJsonOutput jOut) {
    super(api, errorHandler);
    this.jIn = Objects.requireNonNull(jIn);
    this.jOut = Objects.requireNonNull(jOut);
  }

  @Override public String loadPath(RvParameter pp, MxExchange x) {
    return x.getPathParam(pp.name);
  }

  @Override public String loadQuery(RvParameter qp, MxExchange x) {
    return x.getQueryParam(qp.name);
  }

  @Override public String loadCookie(RvParameter cp, MxExchange x) {
    var c = x.cookies.get(cp.name);
    return c != null ? c.value : null;
  }

  @Override public String loadForm(RvParameter fp, MxExchange x) {
    return x.getFormParam(fp.name);
  }

  @Override public String loadHeader(RvParameter hp, MxExchange x) {
    return x.io.getRequestHeaders().getFirst(hp.name);
  }

  @Override public Object loadAttachment(RvParameter ap, RvAttachmentParam at, MxExchange x) {
    return x.getAttachment(at.value());
  }

  @Override public Object loadBean(RvParameter bp, MxExchange x) {
    return jIn.fromJson(x.io.getRequestBody(), bp.type);
  }

  @Override public MxHandler combine(List<RvHandler<MxExchange>> rvHandlers) {
    for (var rvh : rvHandlers) {
      var method = MxMethod.valueOf(rvh.descriptor.httpMethodTxt);
      router.add(method, rvh.descriptor.path.value(), xc -> rvh.consumer.accept(xc));
    }
    return router;
  }

  @Override public void commitResponse(RvDescriptor rvd, Object res, MxExchange x) {
    if (rvd.httpStatus != null) {
      x.withStatus(MxStatus.valueOf(rvd.httpStatus.value().getStatusCode()));
    } else {
      x.withStatus(MxStatus._200);
    }
    if (res != null) {
      var json = jOut.toJson(res);
      x.withBody(MxMime.json, json);
    }
    x.commit();
  }

  @Override public void commitResponse(RvResponse<?> res, MxExchange x) {
    x.withStatus(MxStatus.valueOf(res.status.getStatusCode()));
    if (res.redirectPath != null) {
      x.withRedirect(res.redirectPath);
    }
    for (var e : res.headers.entrySet()) {
      x.withHeader(e.getKey(), e.getValue().get(0));
    }
    if (res.bodyUrl != null) {
      x.withBody(res.bodyUrl);
      if (res.mediaType != null) {
        x.withHeader(HContentType, res.mediaType);
      }
    } else if (res.body != null) {
      x.withBody(MxMime.json, jOut.toJson(res.body));
    }
    x.commit();
  }
}
