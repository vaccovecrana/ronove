package io.vacco.ronove;

import io.vacco.ronove.codegen.RvContext;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;

import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class RvPluginSpec {

  private static final Logger log = LoggerFactory.getLogger(RvPluginSpec.class);

  static {
    describe(RvPlugin.class.getCanonicalName(), () -> {
      it("can read service endpoint methods from annotated classes", () -> {
        log.warn(new RvContext().map(RvExampleApi.class));
      });
    });
  }
}
