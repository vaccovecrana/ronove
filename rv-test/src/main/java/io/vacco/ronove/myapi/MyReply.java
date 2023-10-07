package io.vacco.ronove.myapi;

public class MyReply<T> {

  public T data;

  public static <T> MyReply<T> ok(T data) {
    var r = new MyReply<T>();
    r.data = data;
    return r;
  }

}
