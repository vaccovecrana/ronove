package io.vacco.ronove;

import com.google.gson.Gson;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.util.LinkedHashMap;

import static j8spec.J8Spec.describe;
import static j8spec.J8Spec.it;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class RvResultTest {

  public static class AgeOp extends RvResult {
    public int age;
  }

  static {

    describe(RvValidation.class.getCanonicalName(), () -> {
      it("Requires an i18n key",
        c -> c.expected(NullPointerException.class),
        () -> RvValidation.of(null)
      );
      it("Chains optional attributes",
        () -> {
          var params = new LinkedHashMap<String, String>();
          params.put("minAge", "18");
          params.put("maxAge", "39");
          var v = RvValidation.of("app.i18n.ageValidationFailed")
            .withName("registrationAge")
            .withParams(params);
          assertEquals("registrationAge", v.name);
          assertEquals("app.i18n.ageValidationFailed", v.key);
          assertEquals(params, v.params);
        }
      );
      it("Serializes to a browser friendly JSON object",
        () -> {
          var v = RvValidation.of("app.i18n.ageValidationFailed")
            .withName("registrationAge")
            .withParam("minAge", "18")
            .withParam("maxAge", "39");
          var json = new Gson().toJson(v);
          assertEquals(
            "{\"name\":\"registrationAge\",\"key\":\"app.i18n.ageValidationFailed\",\"params\":{\"maxAge\":\"39\",\"minAge\":\"18\"}}",
            json
          );
        }
      );
    });

    describe(RvResult.class.getCanonicalName(), () -> {
      it("Is ok when empty",
        () -> assertTrue(new RvResult().ok())
      );
      it("Is not ok when an error is set",
        () -> assertFalse(new RvResult().withError("boom").ok())
      );
      it("Is not ok when validations are present",
        () -> {
          var r = new RvResult();
          r.validations.add(RvValidation.of("app.i18n.ageValidationFailed"));
          assertFalse(r.ok());
        }
      );
      it("Skips validators once a result is no longer ok",
        () -> {
          var r = new RvResult().withError("boom");
          var skipped = new boolean[]{false};
          r.validate(x -> {
            skipped[0] = true;
            return x;
          });
          assertFalse(skipped[0]);
        }
      );
      it("Carries validations through to the serialized result",
        () -> {
          var params = new LinkedHashMap<String, String>();
          params.put("minAge", "18");
          params.put("maxAge", "39");
          var r = new AgeOp();
          r.age = 15;
          r.validations.add(RvValidation.of("app.i18n.ageValidationFailed")
            .withName("age")
            .withParams(params));
          var json = new Gson().toJson(r);
          assertTrue(json.contains("\"validations\":"));
          assertTrue(json.contains("\"app.i18n.ageValidationFailed\""));
          assertFalse(r.ok());
        }
      );
    });

  }

}