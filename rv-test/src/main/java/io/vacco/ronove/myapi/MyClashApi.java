package io.vacco.ronove.myapi;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

public class MyClashApi {

  @GET
  @Path("/v1/clash/a")
  public io.vacco.ronove.myapi.MyClash a() {
    return new io.vacco.ronove.myapi.MyClash();
  }

  @GET
  @Path("/v1/clash/b")
  public io.vacco.ronove.myapi.clash.MyClash b() {
    return new io.vacco.ronove.myapi.clash.MyClash();
  }

}
