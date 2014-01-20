package com.appctek.anyroshambo.util;

import java.util.Map;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-20-01
 */
public class Pair<K,V> implements Map.Entry<K,V> {

    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V v) {
        final V old = this.value;
        this.value = v;
        return old;
    }
}
