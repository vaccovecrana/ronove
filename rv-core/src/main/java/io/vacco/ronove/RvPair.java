package io.vacco.ronove;

public class RvPair<K, V> {

  public K key;
  public V val;

  public static <K, V> RvPair<K, V> of(K key, V val) {
    var p = new RvPair<K, V>();
    p.key = key;
    p.val = val;
    return p;
  }

}
