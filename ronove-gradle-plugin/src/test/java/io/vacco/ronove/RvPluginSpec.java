package io.vacco.ronove;

import cz.habarta.typescript.generator.DefaultTypeProcessor;
import cz.habarta.typescript.generator.TypeProcessor;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;

import static java.util.Arrays.*;
import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class RvPluginSpec {

  private static final Logger log = LoggerFactory.getLogger(RvPluginSpec.class);

  static {
    describe(RvPlugin.class.getCanonicalName(), () -> {
      it("can method type parameters to their Typescript equivalents", () -> {
        // TypeProcessor.Context ctx = new TypeProcessor.Context
        // DefaultTypeProcessor p = new DefaultTypeProcessor();
        // p.processType(RvReq.class, );
      });
      it("can read service endpoint methods from annotated classes", () -> {
        stream(RvExampleApi.class.getMethods())
            .filter(m -> stream(m.getAnnotations()).anyMatch(a -> RvOp.class.isAssignableFrom(a.getClass())))
            .forEach(m -> {
              // TODO turn path into TS method name (remove slashes, add camelCase)
              // call the right request method depending on annotation metadata.
              log.warn("========================");
              log.warn(m.toString());
              RvOp r = m.getAnnotation(RvOp.class);
              log.warn(">>> {}", r.toString());
            });

      });
    });
  }
}
