package io.vacco.ronove.undertow;

import io.undertow.server.*;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.*;
import io.vacco.ronove.*;
import jakarta.ws.rs.core.MediaType;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;

public class RvUtAdapter<Api> extends RvAdapter<Api, HttpHandler, HttpServerExchange> {

  private static final HttpString HContentType    = HttpString.tryFromString("Content-Type");
  private static final HttpString HContentLength  = HttpString.tryFromString("Content-Length");
  private static final HttpString HLocation       = HttpString.tryFromString("Location");

  private final Map<Class<?>, AttachmentKey<?>> attachmentKeyIdx = new HashMap<>();
  private final RoutingHandler routingHandler = new RoutingHandler();
  private final RvJsonInput  jIn;
  private final RvJsonOutput jOut;

  public RvUtAdapter(Api api, BiConsumer<HttpServerExchange, Exception> errorHandler,
                     RvJsonInput jIn, RvJsonOutput jOut, RvUtAttachmentKey<?> ... attachmentKeys) {
    super(api, errorHandler);
    this.jIn = Objects.requireNonNull(jIn);
    this.jOut = Objects.requireNonNull(jOut);
    for (var ak : attachmentKeys) {
      attachmentKeyIdx.put(ak.type, ak.key);
    }
  }

  @Override public String loadPath(RvParameter pp, HttpServerExchange x) {
    var pv = x.getQueryParameters().get(pp.name);
    return pv != null ? pv.getFirst() : null;
  }

  @Override public String loadQuery(RvParameter qp, HttpServerExchange x) {
    var qv = x.getQueryParameters().get(qp.name);
    return qv != null ? qv.getFirst() : null;
  }

  @Override public String loadCookie(RvParameter cp, HttpServerExchange x) {
    var qv = x.getRequestCookie(cp.name);
    return qv != null ? qv.getValue() : null;
  }

  @Override public String loadForm(RvParameter fp, HttpServerExchange x) {
    var formData = x.getAttachment(FormDataParser.FORM_DATA);
    if (formData != null) {
      return formData.get(fp.name).getFirst().getValue();
    }
    return null;
  }

  @Override public String loadHeader(RvParameter hp, HttpServerExchange x) {
    var hv = x.getRequestHeaders().get(hp.name);
    return hv != null ? hv.getFirst() : null;
  }

  @Override public Object loadAttachment(RvParameter ap, RvAttachmentParam at,
                                         HttpServerExchange x) {
    var ak = attachmentKeyIdx.get(at.value());
    if (ak != null) {
      return x.getAttachment(ak);
    }
    return null;
  }

  @Override public Object loadBean(RvParameter bp, HttpServerExchange x) {
    var rd = Channels.newReader(x.getRequestChannel(), StandardCharsets.UTF_8);
    return jIn.fromJson(rd, bp.type);
  }

  @Override public HttpHandler combine(List<RvHandler<HttpServerExchange>> rvHandlers) {
    for (var rvh : rvHandlers) {
      routingHandler.add(
        rvh.descriptor.httpMethodTxt, rvh.descriptor.path.value(),
        x -> rvh.consumer.accept(x)
      );
    }
    return routingHandler;
  }

  @Override public void commitResponse(RvDescriptor rvd, Object res,
                                       HttpServerExchange x) throws Exception {
    if (rvd.httpStatus != null) {
      x.setStatusCode(rvd.httpStatus.value().getStatusCode());
    }
    if (res != null) {
      var json = jOut.toJson(res);
      x.getResponseHeaders().add(HContentType, MediaType.APPLICATION_JSON);
      x.getResponseChannel().write(
        ByteBuffer.wrap(json.getBytes(StandardCharsets.UTF_8))
      );
    }
    x.endExchange();
  }

  @Override public void commitResponse(RvResponse<?> res, HttpServerExchange x) throws Exception {
    x.setStatusCode(res.status.getStatusCode());
    if (res.redirectPath != null) {
      x.getResponseHeaders().add(HLocation, res.redirectPath);
    }
    for (var e : res.headers.entrySet()) {
      x.getResponseHeaders().add(
        HttpString.tryFromString(e.getKey()),
        e.getValue().get(0)
      );
    }
    if (res.bodyUrl != null) {
      var conn = res.bodyUrl.openConnection();
      try (var is = conn.getInputStream()) {
        x.getResponseHeaders().add(HContentType, res.mediaType);
        x.getResponseHeaders().add(HContentLength, conn.getContentLength());
        is.transferTo(Channels.newOutputStream(x.getResponseChannel()));
      }
    } else if (res.body != null) {
      x.getResponseSender().send(jOut.toJson(res.body));
    }
    x.endExchange();
  }

}
