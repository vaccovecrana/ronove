package io.vacco.ronove;

import com.google.gson.Gson;
import io.vacco.murmux.Murmux;
import io.vacco.ronove.murmux.RvMxAdapter;
import io.vacco.ronove.myapi.MyApi;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;

import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class RvMxAdapterTest {

  private static Murmux mx;
  private static final Logger log = LoggerFactory.getLogger(RvUtAdapterTest.class);

  static {
    beforeAll(() -> {
      var g = new Gson();
      var jIn = (RvJsonInput) g::fromJson;
      var jOut = (RvJsonOutput) g::toJson;
      var sesHdl = new MxMySession();
      var mxBookApi = new RvMxAdapter<>(new MyApi(), (xc, e) -> log.error("Err", e), jIn, jOut).build();
      mx = new Murmux().rootHandler(xc -> {
        log.info("[{}] {}", xc.method, xc.getPath());
        sesHdl.handle(xc);
        mxBookApi.handle(xc);
      }).listen(8080);
    });

    it("Can serve an API using Murmux", () -> {
      if (mx != null) {
        RvRequestRunner.go();
      }
    });

    it("Stops the server", () -> mx.stop());
  }

}
