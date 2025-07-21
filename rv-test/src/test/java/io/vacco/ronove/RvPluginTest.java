package io.vacco.ronove;

import io.vacco.ronove.badapi.*;
import io.vacco.ronove.plugin.*;
import io.vacco.ronove.myapi.MyApi;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import jakarta.ws.rs.core.Response;
import org.junit.runner.RunWith;
import java.util.Collections;

import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class RvPluginTest {
  static {
    describe(RvResponse.class.getCanonicalName(), () -> {
      it("Always needs a status code",
        c -> c.expected(IllegalStateException.class),
        () -> new RvResponse<>().validate()
      );
      it("Cannot contain both body and stream response content",
        c -> c.expected(IllegalStateException.class),
        () -> new RvResponse<>()
          .withStatus(Response.Status.OK)
          .withBody(new String[] {"Hello", "world"})
          .withStream(RvPluginTest.class.getResource("/bye.txt"))
          .validate()
      );
    });

    describe(RvContext.class.getCanonicalName(), () -> {
      it("Rejects path controllers with mismatching param names",
        c -> c.expected(IllegalStateException.class),
        () -> new RvContext().describe(BadApis.BadApi00.class)
      );
      it("Rejects multiple bean parameters in a single method",
        c -> c.expected(IllegalStateException.class),
        () -> new RvContext().describe(BadApis.BadApi01.class)
      );
      it("Rejects bean parameters in non-body methods",
        c -> c.expected(IllegalStateException.class),
        () -> new RvContext().describe(BadApis.BadApi02.class)
      );
      it("Rejects form parameters in non-body methods",
        c -> c.expected(IllegalStateException.class),
        () -> new RvContext().describe(BadApis.BadApi03.class)
      );
      it("Rejects bean and form parameters in a single method",
        c -> c.expected(IllegalStateException.class),
        () -> new RvContext().describe(BadApis.BadApi04.class)
      );
      it("Rejects duplicate path mappings",
        c -> c.expected(IllegalStateException.class),
        () -> new RvContext().describe(BadApis.BadApi05.class)
      );
      it("Rejects empty path mappings",
        c -> c.expected(IllegalStateException.class),
        () -> new RvContext().describe(BadApis.BadApi06.class)
      );
      it("Ends validations", () -> {
        var api00 = new BadApis.BadApi00();
        var api01 = new BadApis.BadApi01();
        var api02 = new BadApis.BadApi02();
        var api03 = new BadApis.BadApi03();
        var api04 = new BadApis.BadApi04();
        var api05 = new BadApis.BadApi05();
        var api06 = new BadApis.BadApi06();

        api00.bad00(-1);
        api01.bad01(null, null);
        api02.bad02(null);
        api03.bad03("");
        api04.bad04(null, null);
        api05.bad0501();
        api05.bad0502();
        System.out.println(api06);
      });
    });

    describe(RvPlugin.class.getCanonicalName(),
        () -> it(
          "Can render Typescript bindings from annotated classes",
          () -> System.out.println(
            new RvTsGen().render(Collections.singletonList(MyApi.class))
          )
        )
    );
  }
}
