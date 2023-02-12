package io.vacco.ronove.myapi;

import io.vacco.oruzka.core.*;
import io.vacco.ronove.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class MyApi {

  public static final String v1ApiPing = "/v1/api/ping";

  public static final String v1ApiEchoFmt = "/v1/api/echo/%d";
  public static final String v1ApiEcho = "/v1/api/echo/{someNumber}";
  public static final String v1ApiOptions = "/v1/api/options";

  public static final String v1Blog = "/v1/blog";
  public static final String v1BlogOptions = "/v1/blog/options";
  public static final String v1BlogTagsUpdate = "/v1/blog/tags/update";

  public static final String v1EchoCookie = "/v1/cookie-echo";
  public static final String v1Login = "/v1/login";

  public static final String v1Pair = "/v1/pair";
  public static final String v1PairList = "/v1/pair/list";

  @GET @Path(v1ApiPing)
  @Produces(MediaType.TEXT_PLAIN)
  public RvResponse<String> hello() {
    return new RvResponse<String>()
      .withStatus(Response.Status.OK)
      .withStream(MyApi.class.getResource("/pong.txt"));
  }

  @GET @Path(v1ApiEcho)
  public OzReply<Integer> getSomeEchoFn(@PathParam("someNumber") int someNumber,
                                        @RvAttachmentParam(MyUser.class) MyUser me) {
    System.out.printf("You are: [%s]%n", requireNonNull(me));
    return OzReply.asOk(someNumber);
  }

  @GET @Path(v1ApiOptions)
  public OzReply<List<MyOpts>> getApiOpts() {
    return OzReply.asOk(Arrays.asList(MyOpts.values()));
  }

  @GET @Path(v1BlogOptions)
  public RvResponse<OzReply<List<MyBlogEntry>>> getBlogsByOption(@QueryParam("anOption") MyOpts anOption,
                                                                 @QueryParam("sort") @DefaultValue("asc") String sort) {
    requireNonNull(anOption);

    var e0 = new MyBlogEntry();
    e0.bid = 111L;
    e0.text = "This is some blog content text";
    e0.tags = new LinkedHashSet<>(Arrays.asList("food", "travel", "money"));

    var e1 = new MyBlogEntry();
    e1.bid = 888L;
    e1.text = "And this is some other blog content text";
    e1.tags = new LinkedHashSet<>(Arrays.asList("cats", "dogs", "funny"));

    return new RvResponse<OzReply<List<MyBlogEntry>>>()
      .withStatus(Response.Status.OK)
      .withMediaType(MediaType.APPLICATION_JSON)
      .withBody(OzReply.asOk(Arrays.asList(e0, e1)));
  }

  @GET @Path(v1Blog)
  public OzReply<MyBlogEntry> getBlogPost(@QueryParam("blogId") long blogId) {
    var e = new MyBlogEntry();
    e.bid = blogId;
    e.text = "This is some blog content text";
    e.tags = new LinkedHashSet<>(Arrays.asList("food", "travel", "money"));
    return OzReply.asOk(e);
  }

  @PATCH @Path(v1BlogTagsUpdate)
  @Consumes(MediaType.APPLICATION_JSON)
  @RvStatus(Response.Status.NO_CONTENT)
  public void patchBlogTags(@BeanParam List<MyBlogTagsUpdate> blogUpdates,
                            @HeaderParam("xToken") String token) {
    requireNonNull(blogUpdates);
    requireNonNull(token);
    System.out.printf("====> [upd: %s, token: %s]%n", blogUpdates, token);
  }

  @GET @Path(v1EchoCookie)
  public String[] echoCookies(@CookieParam("name") String name,
                              @CookieParam("name2") String name2,
                              @CookieParam("name3") String name3) {
    return new String[] {
      requireNonNull(name),
      requireNonNull(name2),
      requireNonNull(name3)
    };
  }

  @POST @Path(v1Login)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public RvResponse<Void> login(@FormParam("username") String username,
                                @FormParam("password") String password) {
    requireNonNull(username);
    requireNonNull(password);
    System.out.printf("====> [user: %s, pass: %s]%n", username, password);
    return new RvResponse<Void>()
      .withStatus(Response.Status.FOUND)
      .withRedirectPath(String.format(v1ApiEchoFmt, 9999))
      .withHeader("superSecretToken", "mamamax1234");
  }

  @GET @Path(v1Pair)
  public MyPair<Integer, String> v1Pair(@QueryParam("pairId") Integer pairId) {
    var p = new MyPair<Integer, String>();
    p.key = requireNonNull(pairId);
    p.val = "Juno";
    return p;
  }

  @GET @Path(v1PairList)
  public List<MyPair<Integer, String>> v1PairList() {
    var p0 = new MyPair<Integer, String>();
    var p1 = new MyPair<Integer, String>();
    p0.key = 1;
    p0.val = "Juno";
    p1.key = 2;
    p1.val = "Golf";
    return List.of(p0, p1);
  }

}
