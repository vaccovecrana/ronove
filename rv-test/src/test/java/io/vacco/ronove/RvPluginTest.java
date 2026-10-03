package io.vacco.ronove;

import io.vacco.ronove.reflect.RvContext;
import io.vacco.ronove.reflect.RvTypeCache;
import io.vacco.ronove.util.RvResponse;
import io.vacco.ronove.badapi.BadApis;
import io.vacco.ronove.myapi.MyApi;
import io.vacco.ronove.myapi.MyClashApi;
import io.vacco.ronove.myapi.MyEdgeApi;
import io.vacco.ronove.myapi.MyFieldTestModel;
import io.vacco.ronove.plugin.RvGraalGen;
import io.vacco.ronove.plugin.RvPlugin;
import io.vacco.ronove.plugin.RvTsGen;
import io.vacco.ronove.reflect.RvContext;
import io.vacco.ronove.reflect.RvTypeCache;
import io.vacco.ronove.util.RvResponse;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import jakarta.ws.rs.core.Response;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static j8spec.J8Spec.describe;
import static j8spec.J8Spec.it;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class RvPluginTest {

  private static Set<String> schemaNames(RvContext ctx) {
    return ctx.schemaTypes().stream()
      .map(t -> t.name)
      .collect(Collectors.toSet());
  }

  private static Set<String> metadataNames(RvContext ctx) {
    return ctx.metadataTypes().stream()
      .map(t -> ((Class<?>) t).getSimpleName())
      .collect(Collectors.toSet());
  }

  private static File tempDir() throws Exception {
    return Files.createTempDirectory("ronove-test").toFile();
  }

  private static File generatedDir() {
    var dir = new File("build/generated/ronove");
    dir.mkdirs();
    return dir;
  }

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
          var grg = new RvGraalGen();
          var tsg = new RvTsGen();
          var dir = generatedDir();

          var apiCtx = new RvContext();
          apiCtx.describe(MyApi.class);
          var rpcFile = new File(dir, "rpc.ts");
          var rcFile = new File(dir, "reflect-config.json");
          var rmFile = new File(dir, "reachability-metadata.json");
          tsg.render(apiCtx, true, rpcFile);
          grg.render(apiCtx, rcFile, rmFile);

          var edgeCtx = new RvContext();
          edgeCtx.describe(MyEdgeApi.class);
          var edgeRpcFile = new File(dir, "edge-rpc.ts");
          tsg.render(edgeCtx, true, edgeRpcFile);

          var rpcSrc = Files.readString(rpcFile.toPath());
          var rcSrc = Files.readString(rcFile.toPath());
          var rmSrc = Files.readString(rmFile.toPath());
          var edgeSrc = Files.readString(edgeRpcFile.toPath());
          System.out.println(rpcSrc);
          System.out.println(edgeSrc);
          System.out.println(rcSrc);
          System.out.println(rmSrc);
          assertFalse(rpcSrc.contains("interface Map"));
          assertTrue(edgeSrc.contains("export const arrayOnly"));
        }
      )
    );

    describe(RvTypeCache.class.getCanonicalName(), () -> {
      it("Filters out transient fields from schema types",
        () -> {
          var t = new RvTypeCache().get(MyFieldTestModel.class);
          assertEquals("MyFieldTestModel", t.name);
          assertEquals(1, t.properties.size());
          assertTrue(t.properties.containsKey("visible"));
          assertFalse(t.properties.containsKey("hidden"));
        }
      );
      it("Registers controllers and DTOs in Graal metadata",
        () -> {
          var ctx = new RvContext();
          ctx.describe(Collections.singletonList(MyApi.class));
          var dir = tempDir();
          var rcFile = new File(dir, "reflect-config.json");
          var rmFile = new File(dir, "reachability-metadata.json");
          new RvGraalGen().render(ctx, rcFile, rmFile);
          var rc = Files.readString(rcFile.toPath());
          var rm = Files.readString(rmFile.toPath());

          assertTrue(rc.startsWith("[\n"));
          assertTrue(rc.trim().endsWith("]"));
          assertTrue(rc.contains("\"name\": \"io.vacco.ronove.myapi.MyApi\""));
          assertTrue(rc.contains("\"name\": \"io.vacco.ronove.myapi.MyBlogEntry\""));
          assertTrue(rc.contains("\"name\": \"io.vacco.ronove.myapi.MyBlogTagsUpdate\""));
          assertTrue(rc.contains("\"name\": \"io.vacco.ronove.myapi.MyDbCar\""));
          assertTrue(rc.contains("\"name\": \"io.vacco.ronove.myapi.MyDbEngine\""));
          assertTrue(rc.contains("\"allDeclaredMethods\": true"));
          assertTrue(rc.contains("\"unsafeAllocated\": true"));
          assertFalse(rc.contains("\"name\": \"java."));
          assertFalse(rc.contains("\"name\": \"jakarta."));

          assertTrue(rm.startsWith("{\n  \"reflection\": [\n"));
          assertTrue(rm.trim().replaceAll("\\s+", "").endsWith("]}"));
          assertTrue(rm.contains("\"type\": \"io.vacco.ronove.myapi.MyApi\""));
          assertTrue(rm.contains("\"type\": \"io.vacco.ronove.myapi.MyBlogEntry\""));
          assertFalse(rm.contains("\"type\": \"java."));
          assertFalse(rm.contains("\"type\": \"jakarta."));
        }
      );
      it("Writes only the configured metadata file",
        () -> {
          var ctx = new RvContext();
          ctx.describe(Collections.singletonList(MyApi.class));
          var dir = tempDir();
          var rcFile = new File(dir, "reflect-config.json");
          var rmFile = new File(dir, "reachability-metadata.json");
          new RvGraalGen().render(ctx, null, rmFile);
          assertFalse(rcFile.exists());
          assertTrue(rmFile.exists());
        }
      );
      it("Keeps rpc=false @RvGraal includes out of TypeScript but in Graal metadata",
        () -> {
          var ctx = new RvContext();
          ctx.describe(Collections.singletonList(MyEdgeApi.class));
          assertFalse(schemaNames(ctx).contains("MyGraalOnly"));
          assertTrue(metadataNames(ctx).contains("MyGraalOnly"));
        }
      );
      it("Includes rpc=true @RvGraal includes in TypeScript and Graal metadata",
        () -> {
          var ctx = new RvContext();
          ctx.describe(Collections.singletonList(MyApi.class));
          assertTrue(schemaNames(ctx).contains("MyDbCar"));
          assertTrue(schemaNames(ctx).contains("MyDbWheel"));
          assertTrue(metadataNames(ctx).contains("MyDbCar"));
          assertTrue(metadataNames(ctx).contains("MyDbWheel"));
        }
      );
      it("Keeps attachment-only types out of TypeScript but in Graal metadata",
        () -> {
          var ctx = new RvContext();
          ctx.describe(Collections.singletonList(MyEdgeApi.class));
          assertFalse(schemaNames(ctx).contains("MyServerOnly"));
          assertTrue(metadataNames(ctx).contains("MyServerOnly"));
        }
      );
      it("Keeps attachment types that are also RPC DTOs in TypeScript",
        () -> {
          var ctx = new RvContext();
          ctx.describe(Collections.singletonList(MyApi.class));
          assertTrue(schemaNames(ctx).contains("MyUser"));
        }
      );
      it("Maps DTOs referenced only through an array field",
        () -> {
          var ctx = new RvContext();
          ctx.describe(Collections.singletonList(MyEdgeApi.class));
          assertTrue(schemaNames(ctx).contains("MyArrayItem"));
          assertTrue(metadataNames(ctx).contains("MyArrayItem"));
        }
      );
      it("Maps cyclic DTO graphs without recursing forever",
        () -> {
          var ctx = new RvContext();
          ctx.describe(Collections.singletonList(MyEdgeApi.class));
          assertTrue(schemaNames(ctx).contains("MyCyclic"));
          assertTrue(metadataNames(ctx).contains("MyCyclic"));
        }
      );
      it("Rejects distinct Java classes mapping to the same TypeScript interface",
        c -> c.expected(IllegalStateException.class),
        () -> {
          var ctx = new RvContext();
          ctx.describe(Collections.singletonList(MyClashApi.class));
          ctx.schemaTypes();
        }
      );
    });
  }
}
