package io.vacco.ronove;

import io.vacco.oruzka.core.OzReply;

import javax.ws.rs.*;
import java.util.*;

public class RvExampleApi {

  @GET @Path("/v1/api/echo")
  public OzReply<Integer> getSomEchoFn(int req) {
    return OzReply.asOk(0);
  }

  @GET @Path("/v1/api/options")
  public OzReply<List<RvApiOpts>> getApiOpts() {
    return OzReply.asOk(Arrays.asList(RvApiOpts.values()));
  }

  @GET @Path("/v1/blog/options")
  public OzReply<List<RvBlogEntry>> getBlogsWithOption(RvApiOpts anOption) {
    RvBlogEntry e0 = new RvBlogEntry();
    e0.bid = 111L;
    e0.text = "This is some blog content text";
    e0.tags = new LinkedHashSet<>(Arrays.asList("food", "travel", "money"));

    RvBlogEntry e1 = new RvBlogEntry();
    e1.bid = 888L;
    e1.text = "And this is some other blog content text";
    e1.tags = new LinkedHashSet<>(Arrays.asList("cats", "dogs", "funny"));

    return OzReply.asOk(Arrays.asList(e0, e1));
  }

  @GET @Path("/v1/blog")
  public OzReply<RvBlogEntry> getBlogPost(long blogId) {
    RvBlogEntry e = new RvBlogEntry();
    e.bid = 999L;
    e.text = "This is some blog content text";
    e.tags = new LinkedHashSet<>(Arrays.asList("food", "travel", "money"));
    return OzReply.asOk(e);
  }

  @GET @Path("/v1/blog/tags")
  public OzReply<String[]> getBlogPostTags(List<Long> blogIds) {
    return OzReply.asOk(new String[] {"cooking", "cats", "latest"});
  }

  @PATCH @Path("/v1/blog/tags/update")
  public OzReply<Long[]> patchBlogTags(List<RvBlogTagsUpdate> blogUpdates) {
    return OzReply.asOk(new Long[] {111L, 222L, 333L});
  }

}
