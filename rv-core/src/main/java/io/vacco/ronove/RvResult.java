package io.vacco.ronove;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RvResult {

  public String error;

  public final List<RvValidation> validations = new ArrayList<>();

  @SuppressWarnings("unchecked")
  public <T extends RvResult> T withError(String error) {
    this.error = error;
    return (T) this;
  }

  public <T extends RvResult> T withError(Exception e) {
    return withError(
      e.getMessage() != null
        ? e.getMessage()
        : e.getClass().getCanonicalName()
    );
  }

  @SuppressWarnings("unchecked")
  public <T extends RvResult> T withValidations(List<RvValidation> validations) {
    if (validations != null) {
      this.validations.clear();
      this.validations.addAll(validations);
    }
    return (T) this;
  }

  public boolean ok() {
    return error == null && validations.isEmpty();
  }

  @SuppressWarnings("unchecked")
  public <T extends RvResult> T validate(Function<T, T> validator) {
    if (ok()) {
      return validator.apply((T) this);
    }
    return (T) this;
  }

  public void clear() {
    this.error = null;
    this.validations.clear();
  }

  @SuppressWarnings("unchecked")
  public <T extends RvResult> T setFrom(RvResult r0) {
    clear();
    this.error = r0.error;
    this.validations.addAll(r0.validations);
    return (T) this;
  }

}