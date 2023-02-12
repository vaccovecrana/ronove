package io.vacco.ronove.myapi;

import java.util.Set;

public class MyBlogTagsUpdate {
  public long blogId;
  public Set<String> tags;

  public MyBlogTagsUpdate withBlogId(long blogId) {
    this.blogId = blogId;
    return this;
  }

  public MyBlogTagsUpdate withTags(Set<String> tags) {
    this.tags = tags;
    return this;
  }
}
