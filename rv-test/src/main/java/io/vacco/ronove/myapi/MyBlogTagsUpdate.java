package io.vacco.ronove.myapi;

import java.util.List;
import java.util.Set;

public class MyBlogTagsUpdate {

  public long blogId;
  public Set<String> tags;

  public MyOpts updateOptions;
  public MyOpts[] moreOptions;
  public List<MyOpts> evenMoreOptions;

  public MyPair<Integer, MyUser> followUsers;

  public MyBlogTagsUpdate withBlogId(long blogId) {
    this.blogId = blogId;
    return this;
  }

  public MyBlogTagsUpdate withTags(Set<String> tags) {
    this.tags = tags;
    return this;
  }
}
