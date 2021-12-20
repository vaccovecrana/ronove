package io.vacco.ronove;

import io.vacco.oruzka.core.*;
import io.vacco.ronove.core.RvStatus;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.*;

public class MyApi {

  @GET @Path("/v1/api/echo/{someNumber}")
  public OzReply<Integer> getSomeEchoFn(@PathParam("someNumber") int someNumber) {
    return OzReply.asOk(0);
  }

  @GET @Path("/v1/api/options")
  public OzReply<List<MyOpts>> getApiOpts() {
    return OzReply.asOk(Arrays.asList(MyOpts.values()));
  }

  @GET @Path("/v1/blog/options")
  public OzReply<List<MyBlogEntry>> getBlogsByOption(@QueryParam("anOption") MyOpts anOption,
                                                     @QueryParam("sortAsc") @DefaultValue("asc") String sort) {
    MyBlogEntry e0 = new MyBlogEntry();
    e0.bid = 111L;
    e0.text = "This is some blog content text";
    e0.tags = new LinkedHashSet<>(Arrays.asList("food", "travel", "money"));

    MyBlogEntry e1 = new MyBlogEntry();
    e1.bid = 888L;
    e1.text = "And this is some other blog content text";
    e1.tags = new LinkedHashSet<>(Arrays.asList("cats", "dogs", "funny"));

    return OzReply.asOk(Arrays.asList(e0, e1));
  }

  @GET @Path("/v1/blog")
  public OzReply<MyBlogEntry> getBlogPost(@QueryParam("blogId") long blogId) {
    MyBlogEntry e = new MyBlogEntry();
    e.bid = 999L;
    e.text = "This is some blog content text";
    e.tags = new LinkedHashSet<>(Arrays.asList("food", "travel", "money"));
    return OzReply.asOk(e);
  }

  @GET @Path("/v1/blog/tags")
  public OzReply<String[]> getBlogPostTags(@HeaderParam("blogIds") List<Long> blogIds) {
    return OzReply.asOk(new String[] {"cooking", "cats", "latest"});
  }

  @RvStatus(Response.Status.NO_CONTENT)
  @PATCH @Path("/v1/blog/tags/update")
  public OzReply<Object[]> patchBlogTags(@BeanParam List<MyBlogTagsUpdate> blogUpdates,
                                         @HeaderParam("xToken") String token) {
    Object[] res = new Object[2];
    res[0] = new Long[] {111L, 222L, 333L};
    res[1] = token;
    return OzReply.asOk(res);
  }

}
