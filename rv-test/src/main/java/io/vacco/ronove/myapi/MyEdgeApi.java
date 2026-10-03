package io.vacco.ronove.myapi;

import io.vacco.ronove.api.RvAttachmentParam;
import io.vacco.ronove.api.RvGraal;
import io.vacco.ronove.util.RvResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@RvGraal(include = {MyGraalOnly.class}, rpc = false)
public class MyEdgeApi {

  @GET
  @Path("/v1/edge/array")
  public MyArrayOnly arrayOnly() {
    return new MyArrayOnly();
  }

  @GET
  @Path("/v1/edge/cyclic")
  public MyCyclic cyclic() {
    return new MyCyclic();
  }

  @GET
  @Path("/v1/edge/server-only")
  public RvResponse<Void> serverOnly(@RvAttachmentParam(MyServerOnly.class) MyServerOnly session) {
    return new RvResponse<Void>().withStatus(Response.Status.NO_CONTENT);
  }

}
