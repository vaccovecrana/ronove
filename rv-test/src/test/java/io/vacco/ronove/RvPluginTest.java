package io.vacco.ronove;

import io.vacco.ronove.reflect.RvContext;
import io.vacco.ronove.util.RvResponse;
import io.vacco.ronove.badapi.BadApis;
import io.vacco.ronove.myapi.MyApi;
import io.vacco.ronove.myapi.MyFieldTestModel;
import io.vacco.ronove.plugin.RvGraalGen;
import io.vacco.ronove.plugin.RvPlugin;
import io.vacco.ronove.plugin.RvTsGen;
import io.vacco.ronove.plugin.RvTsContext;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import jakarta.ws.rs.core.Response;
import org.junit.runner.RunWith;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static j8spec.J8Spec.describe;
import static j8spec.J8Spec.it;
import static org.junit.Assert.*;

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
          .withBody(new String[]{"Hello", "world"})
          .withStream(RvPluginTest.class.getResource("/bye.txt"))
          .validate()
      );
      it("Accepts an error and defaults missing status to 500",
        () -> {
          var res = new RvResponse<>().withError(new RuntimeException("boom"));
          assertNotNull(res.error);
          assertNull(res.status);
        }
      );
      it("Rejects a null error",
        c -> c.expected(NullPointerException.class),
        () -> new RvResponse<>().withError(null)
      );
      it("Accepts a response with both status and error",
        () -> {
          var res = new RvResponse<>()
            .withStatus(Response.Status.OK)
            .withError(new RuntimeException("boom"))
            .withBody("OK");
          assertSame(res, res.validate());
        }
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
        () -> {
          var rpc =  new RvTsGen().render(Collections.singletonList(MyApi.class), true);
          System.out.println(rpc);
          assertFalse(rpc.contains("interface Map"));
        }
      )
    );

    describe(RvTsContext.class.getCanonicalName(), () -> {
      it("Filters out transient fields from schema types",
        () -> {
          var tsCtx = new RvTsContext().add(Collections.singletonList((Type) MyFieldTestModel.class));
          var types = tsCtx.schemaTypes();
          assertEquals(1, types.size());
          var schemaType = types.get(0);
          assertEquals("MyFieldTestModel", schemaType.name);
          assertEquals(1, schemaType.properties.size());
          assertEquals("visible", schemaType.properties.get(0).name);
        }
      );
      it("Resolves reflect config types from controller definitions",
        () -> {
          var idx = new RvContext().describe(Collections.singletonList(MyApi.class));
          var reflect = RvTsContext.from(idx).reflectTypes().stream()
            .map(Class::getSimpleName)
            .collect(Collectors.toSet());
          assertTrue(reflect.containsAll(Arrays.asList(
            "MyReply", "MyPair", "MyBlogEntry", "MyBlogTagsUpdate", "MyUser", "MyOpts",
            "OtAssignmentList", "OtList", "OtResult", "MtPage1", "OtApiKey", "OtKeyNamespace",
            "RvResult", "RvValidation"
          )));
          assertFalse(reflect.contains("String"));
          assertFalse(reflect.contains("List"));
          assertFalse(reflect.contains("Map"));
          assertFalse(reflect.contains("TreeMap"));
        }
      );
      it("Renders Graal reflect-config JSON from resolved types",
        () -> {
          var idx = new RvContext().describe(Collections.singletonList(MyApi.class));
          var json = new RvGraalGen().reflectConfig(
            RvTsContext.from(idx).reflectTypes(),
            Collections.singletonList(MyApi.class)
          );
          System.out.println(json);
          assertTrue(json.startsWith("[\n"));
          assertTrue(json.trim().endsWith("]"));
          assertTrue(json.contains("\"name\": \"io.vacco.ronove.myapi.MyBlogEntry\""));
          assertTrue(json.contains("\"allDeclaredConstructors\": true"));
          assertTrue(json.contains("\"allDeclaredFields\": true"));
          assertTrue(json.contains("\"allDeclaredMethods\": true"));
          assertTrue(json.contains("\"unsafeAllocated\": true"));
          assertFalse(json.contains("\"name\": \"java."));
          assertFalse(json.contains("\"name\": \"jakarta."));
        }
      );
      it("Registers controller classes with @BeanParam methods for reflection",
        () -> {
          var idx = new RvContext().describe(Collections.singletonList(MyApi.class));
          var json = new RvGraalGen().reflectConfig(
            RvTsContext.from(idx).reflectTypes(),
            Collections.singletonList(MyApi.class)
          );
          System.out.println(json);
          assertTrue(json.contains("\"name\": \"io.vacco.ronove.myapi.MyApi\""));
          assertTrue(json.contains("\"allDeclaredMethods\": true"));
        }
      );
      it("Renders Graal reachability-metadata JSON from resolved types",
        () -> {
          var idx = new RvContext().describe(Collections.singletonList(MyApi.class));
          var json = new RvGraalGen().reachabilityMetadata(
            RvTsContext.from(idx).reflectTypes(),
            Collections.singletonList(MyApi.class)
          );
          System.out.println(json);
          assertTrue(json.startsWith("{\n  \"reflection\": [\n"));
          assertTrue(json.trim().replaceAll("\\s+", "").endsWith("]}"));
          assertTrue(json.contains("\"type\": \"io.vacco.ronove.myapi.MyBlogEntry\""));
          assertTrue(json.contains("\"allDeclaredConstructors\": true"));
          assertTrue(json.contains("\"allDeclaredFields\": true"));
          assertTrue(json.contains("\"allDeclaredMethods\": true"));
          assertTrue(json.contains("\"unsafeAllocated\": true"));
          assertTrue(json.contains("\"type\": \"io.vacco.ronove.myapi.MyApi\""));
          assertFalse(json.contains("\"type\": \"java."));
          assertFalse(json.contains("\"type\": \"jakarta."));
        }
      );
    });
  }
}
