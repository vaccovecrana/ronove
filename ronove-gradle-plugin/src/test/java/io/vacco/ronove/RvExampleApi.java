package io.vacco.ronove;

import io.vacco.oruzka.core.OzReply;

public class RvExampleApi {

  @RvOp(path = "/v1/blog/tags", method = RvMethod.Get)
  public OzReply<String[]> getBlogPostTags(RvReq<Long> blogIdReq) {
    return OzReply.asOk(new String[] {"cooking", "cats", "latest"});
  }

}
