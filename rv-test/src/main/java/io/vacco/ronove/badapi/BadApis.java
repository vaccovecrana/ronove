package io.vacco.ronove.badapi;

import io.vacco.oruzka.core.OzReply;
import io.vacco.ronove.myapi.MyBlogEntry;
import jakarta.ws.rs.*;

public class BadApis {

  public static class BadApi00 {
    @GET @Path("/v1/echo/{id}")
    public OzReply<Integer> bad00(@PathParam("someId") int someId) {
      return OzReply.asOk(someId);
    }
  }

  public static class BadApi01 {
    @POST @Path("/v1/bad-01")
    public String bad01(@BeanParam() MyBlogEntry bean00,
                        @BeanParam() MyBlogEntry bean01) {
      return getClass().getSimpleName();
    }
  }

  public static class BadApi02 {
    @GET @Path("/v1/bad-02")
    public String bad02(@BeanParam MyBlogEntry bean00) {
      return getClass().getSimpleName();
    }
  }

  public static class BadApi03 {
    @GET @Path("/v1/bad-03")
    public String bad03(@FormParam("name00") String name00) {
      return getClass().getSimpleName();
    }
  }

  public static class BadApi04 {
    @POST @Path("/v1/bad-04")
    public String bad04(@BeanParam() MyBlogEntry bean00,
                        @FormParam("name00") String name00) {
      return getClass().getSimpleName();
    }
  }

  public static class BadApi05 {
    @GET @Path("/v1/bad-05")
    public String bad0501() {
      return getClass().getSimpleName();
    }
    @GET @Path("/v1/bad-05")
    public String bad0502() {
      return getClass().getSimpleName();
    }
  }
}
