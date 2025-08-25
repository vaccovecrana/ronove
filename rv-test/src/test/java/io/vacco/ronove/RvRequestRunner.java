package io.vacco.ronove;

import com.github.mizosoft.methanol.*;
import com.google.gson.Gson;
import io.vacco.ronove.myapi.MyBlogTagsUpdate;
import jakarta.ws.rs.core.Response;
import org.slf4j.*;

import java.io.IOException;
import java.net.http.*;
import java.util.*;

import static org.junit.Assert.*;
import static com.github.mizosoft.methanol.MutableRequest.*;
import static io.vacco.ronove.myapi.MyApi.*;
import static java.lang.String.format;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class RvRequestRunner {

  private static final Logger log = LoggerFactory.getLogger(RvRequestRunner.class);
  private static final Gson g = new Gson();
  private static final Methanol client = Methanol
    .newBuilder()
    .baseUri("http://localhost:8080")
    .build();

  private static void checkResOk(HttpResponse<?> res) {
    log.info("|-------------------");
    System.out.printf("[%d, %s]%n%s%n", res.statusCode(), res.uri(), res.body());
    assertEquals(Response.Status.OK.getStatusCode(), res.statusCode());
    log.info("*-------------------\n");
  }

  public static void batch1() throws IOException, InterruptedException {
    log.info("==== Batch 1 ====");
    checkResOk(client.send(GET(format(v1ApiEchoFmt, 10000)), ofString()));
    checkResOk(client.send(GET(v1ApiOptions), ofString()));
    checkResOk(client.send(GET(format("%s?anOption=SOME&sort=desc", v1BlogOptions)), ofString()));
    checkResOk(client.send(GET(format("%s?anOption=OF", v1BlogOptions)), ofString()));
    checkResOk(client.send(GET(format("%s?blogId=9999", v1Blog)), ofString()));
  }

  public static void batch2() throws IOException, InterruptedException {
    log.info("==== Batch 2 ====");
    var res5Body = g.toJson(List.of(
      new MyBlogTagsUpdate()
        .withBlogId(9999)
        .withTags(Set.of("art", "comedy"))
    ));
    var res5 = client.send(
      MutableRequest.create(v1BlogTagsUpdate).method("PATCH",
        HttpRequest.BodyPublishers.ofString(res5Body)
      ).header("xToken", "ABCDEF012345"), ofString()
    );
    assertEquals(Response.Status.NO_CONTENT.getStatusCode(), res5.statusCode());

    var res6 = client.send(
      POST(v1Login, FormBodyPublisher.newBuilder()
        .query("username", "gopher")
        .query("password", "lol1234").build())
      , ofString()
    );
    assertEquals(Response.Status.FOUND.getStatusCode(), res6.statusCode());
  }

  public static void batch3() throws IOException, InterruptedException {
    log.info("==== Batch 3 ====");
    checkResOk(client.send(GET(format("%s?pairId=12000", v1Pair)), ofString()));
    checkResOk(client.send(GET(v1PairList), ofString()));
    checkResOk(client.send(GET(v1ApiPing), ofString()));
    checkResOk(client.send(GET(v1Yaml), ofString()));
    checkResOk(
      client.send(
        GET(v1EchoCookie)
          .header("Cookie", "name=value; name2=value2; name3=value3"),
        ofString()
      )
    );
  }

  public static void go() throws IOException, InterruptedException {
    batch1();
    batch2();
    batch3();
  }

}
