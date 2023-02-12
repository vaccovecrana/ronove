package io.vacco.ronove;

import io.vacco.murmux.http.*;
import io.vacco.ronove.myapi.MyUser;

public class MxMySession implements MxHandler {
  @Override public void handle(MxExchange xc) {
    // Perform some sort of authentication in a parent handler,
    // place a derived object, and retrieve it here.
    // This is just a quick example.
    var ses = xc.getAttachment(MyUser.class);
    if (ses == null) {
      xc.putAttachment(
        new MyUser()
          .withNickName("gopher")
          .withAvatarUrl("https://avatar.me/gopher")
      );
    }
  }
}
