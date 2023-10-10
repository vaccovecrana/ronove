package io.vacco.ronove.plugin;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RvTsPrimitives {

  private static final String any = "any";
  private static final String tVoid = "void";
  private static final String tBoolean = "boolean";
  private static final String number = "number";
  private static final String string = "string";
  private static final String date = "Date";

  public static final Map<Class<?>, String> tsTypes = new HashMap<>();

  static {
    tsTypes.put(Void.class, tVoid);
    tsTypes.put(void.class, tVoid);
    tsTypes.put(Object.class, any);

    tsTypes.put(byte.class, number);
    tsTypes.put(Byte.class, number);
    tsTypes.put(short.class, number);
    tsTypes.put(Short.class, number);
    tsTypes.put(int.class, number);
    tsTypes.put(Integer.class, number);
    tsTypes.put(long.class, number);
    tsTypes.put(Long.class, number);
    tsTypes.put(float.class, number);
    tsTypes.put(Float.class, number);
    tsTypes.put(double.class, number);
    tsTypes.put(Double.class, number);

    tsTypes.put(boolean.class, tBoolean);
    tsTypes.put(Boolean.class, tBoolean);

    tsTypes.put(char.class, string);
    tsTypes.put(Character.class, string);

    tsTypes.put(String.class, string);
    tsTypes.put(BigDecimal.class, number);
    tsTypes.put(BigInteger.class, number);
    tsTypes.put(Date.class, date);
    tsTypes.put(UUID.class, string);
  }

}
