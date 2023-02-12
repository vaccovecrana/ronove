package io.vacco.ronove.myapi;

public class MyUser {

  public String nickName, avatarUrl;

  public MyUser withNickName(String nickName) {
    this.nickName = nickName;
    return this;
  }

  public MyUser withAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
    return this;
  }

  @Override public String toString() {
    return String.format("[nick: %s, avatar: %s]", nickName, avatarUrl);
  }
}
