package io.vacco.ronove;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;

public interface RvJsonInput {

  <T> T fromJson(Reader r, Type knownType);

  default <T> T fromJson(String s, Type knownType) {
    return fromJson(new StringReader(s), knownType);
  }

  default <T> T fromJson(InputStream in, Type knownType) {
    return fromJson(new BufferedReader(new InputStreamReader(in)), knownType);
  }

  default <T> T fromJson(URL url, Type knownType) throws IOException {
    return fromJson(url.openStream(), knownType);
  }

}
