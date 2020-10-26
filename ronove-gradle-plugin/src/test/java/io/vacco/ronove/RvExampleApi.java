package io.vacco.ronove;

import io.vacco.oruzka.core.OzReply;
import io.vacco.ronove.core.*;

import java.util.*;

public class RvExampleApi {

  @RvOp(path = "/v1/api/options", method = RvMethod.Get)
  public OzReply<List<RvApiOpts>> getApiOpts() {
    return OzReply.asOk(Arrays.asList(RvApiOpts.values()));
  }

  @RvOp(path = "/v1/blog/options", method = RvMethod.Get)
  public OzReply<List<RvBlogEntry>> getBlogsWithOption(RvReq<RvApiOpts> anOption) {
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

  @RvOp(path = "/v1/blog", method = RvMethod.Get)
  public OzReply<RvBlogEntry> getBlogPost(RvReq<Long> blogId) {
    RvBlogEntry e = new RvBlogEntry();
    e.bid = 999L;
    e.text = "This is some blog content text";
    e.tags = new LinkedHashSet<>(Arrays.asList("food", "travel", "money"));
    return OzReply.asOk(e);
  }

  @RvOp(path = "/v1/blog/tags", method = RvMethod.Get)
  public OzReply<String[]> getBlogPostTags(RvReq<List<Long>> blogIds) {
    return OzReply.asOk(new String[] {"cooking", "cats", "latest"});
  }

  @RvOp(path = "/v1/blog/tags/update", method = RvMethod.Patch)
  public OzReply<Long[]> patchBlogTags(RvReq<List<RvBlogTagsUpdate>> blogUpdates) {
    return OzReply.asOk(new Long[] {111L, 222L, 333L});
  }

}
