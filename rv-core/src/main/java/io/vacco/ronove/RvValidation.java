package io.vacco.ronove;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Describes a failed validation in a locale-agnostic way for browser
 * consumers. The {@code key} and {@code params} form a pre-agreed contract
 * between backend and frontend: the frontend bundles human-readable sentence
 * templates per locale, keyed by {@code key}, and interpolates the values in
 * {@code params} into them (e.g. {@code t(key, params)}).
 */
public class RvValidation {

  public String name;
  public String key;
  public Map<String, String> params;

  public static RvValidation of(String key) {
    var v = new RvValidation();
    v.key = Objects.requireNonNull(key);
    return v;
  }

  public RvValidation withName(String name) {
    this.name = name;
    return this;
  }

  public RvValidation withParams(Map<String, String> params) {
    this.params = params;
    return this;
  }

  public RvValidation withParam(String key, String value) {
    if (this.params == null) {
      this.params = new TreeMap<>();
    }
    this.params.put(key, value);
    return this;
  }

}