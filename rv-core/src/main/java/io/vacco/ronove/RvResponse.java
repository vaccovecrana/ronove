package io.vacco.ronove;

import com.sun.net.httpserver.Headers;
import jakarta.ws.rs.core.*;
import java.net.URL;
import java.util.Objects;

public class RvResponse<T> {

  public final Headers    headers = new Headers();
  public String           mediaType;
  public Response.Status  status;
  public URL              bodyUrl;
  public T                body;
  public String           redirectPath;

  public RvResponse<T> withMediaType(String mediaType) {
    this.mediaType = Objects.requireNonNull(mediaType);
    return this;
  }

  public RvResponse<T> withStatus(Response.Status status) {
    this.status = Objects.requireNonNull(status);
    return this;
  }

  public RvResponse<T> withHeader(String key, String value) {
    headers.add(key, value);
    return this;
  }

  public RvResponse<T> withStream(URL bodyUrl) {
    this.bodyUrl = Objects.requireNonNull(bodyUrl);
    return this;
  }

  public RvResponse<T> withBody(T body) {
    this.body = Objects.requireNonNull(body);
    return this;
  }

  public RvResponse<T> withRedirectPath(String redirectPath) {
    this.redirectPath = Objects.requireNonNull(redirectPath);
    return this;
  }

  public RvResponse<T> validate() {
    if (status == null) {
      throw new IllegalStateException("Response missing HTTP status code.");
    }
    if (body != null && bodyUrl != null) {
      throw new IllegalStateException("Response specifies both body payload and stream.");
    }
    return this;
  }

}
