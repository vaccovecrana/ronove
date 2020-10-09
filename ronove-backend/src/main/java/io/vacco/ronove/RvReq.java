package io.vacco.ronove;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class RvReq<T> {
  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
  public T data;
}
