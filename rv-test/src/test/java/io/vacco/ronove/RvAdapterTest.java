package io.vacco.ronove;

import io.vacco.ronove.api.RvAdapter;
import io.vacco.ronove.api.RvAttachmentParam;
import io.vacco.ronove.api.RvHandler;
import io.vacco.ronove.myapi.MyApi;
import io.vacco.ronove.myapi.MyEdgeApi;
import io.vacco.ronove.reflect.RvMethod;
import io.vacco.ronove.reflect.RvParameter;
import io.vacco.ronove.util.RvResponse;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.stream.Collectors;

import static j8spec.J8Spec.describe;
import static j8spec.J8Spec.it;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class RvAdapterTest {

  private static class CapturingAdapter extends RvAdapter<List<RvHandler<Object>>, Object> {

    private CapturingAdapter() {
      super((xc, e) -> {});
    }

    @Override public String loadPath(RvParameter pp, Object xc) { return null; }
    @Override public String loadQuery(RvParameter qp, Object xc) { return null; }
    @Override public String loadCookie(RvParameter cp, Object xc) { return null; }
    @Override public String loadForm(RvParameter fp, Object xc) { return null; }
    @Override public String loadHeader(RvParameter hp, Object xc) { return null; }
    @Override public Object loadAttachment(RvParameter ap, RvAttachmentParam at, Object xc) { return null; }
    @Override public Object loadBean(RvParameter bp, Object xc) { return null; }
    @Override public List<RvHandler<Object>> combine(List<RvHandler<Object>> handlers) { return handlers; }
    @Override public void commitResponse(RvMethod rvd, Object res, Object xc) { }
    @Override public void commitResponse(RvResponse<?> res, Object xc) { }
  }

  static {
    describe(RvAdapter.class.getCanonicalName(), () -> {
      it("Binds handler methods from multiple controller classes",
        () -> {
          var handlers = new CapturingAdapter().build(new MyApi(), new MyEdgeApi());
          var paths = handlers.stream()
            .map(h -> h.descriptor.path.value())
            .collect(Collectors.toSet());
          assertTrue(paths.contains("/v1/api/ping"));
          assertTrue(paths.contains("/v1/edge/array"));
          assertTrue(paths.contains("/v1/edge/cyclic"));
        }
      );
      it("Binds a single controller class",
        () -> {
          var handlers = new CapturingAdapter().build(new MyApi());
          assertFalse(handlers.isEmpty());
          assertTrue(handlers.stream().allMatch(
            h -> h.descriptor.javaMethod.getDeclaringClass() == MyApi.class
          ));
        }
      );
      it("Rejects controllers mapping the same path",
        c -> c.expected(IllegalStateException.class),
        () -> new CapturingAdapter().build(new MyApi(), new MyApi())
      );
    });
  }
}
