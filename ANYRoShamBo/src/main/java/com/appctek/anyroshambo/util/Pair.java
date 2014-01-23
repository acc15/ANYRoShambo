package com.appctek.anyroshambo.util;

import java.util.Map;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public class Pair<K,V> implements Map.Entry<K,V> {
    public K key;
    public V value;

    public Pair<K,V> withKey(K key) {
        this.key = key;
        return this;
    }

    public Pair<K,V> withValue(V value) {
        this.value = value;
        return this;
    }

    public Pair() {
    }

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public static <K,V> Pair<K,V> valueOnly(V value) {
        return new Pair<K, V>().withValue(value);
    }

    public static <K,V> Pair<K,V> keyOnly(K key) {
        return new Pair<K, V>().withKey(key);
    }

    public static <K,V> Pair<K,V> makePair(K key, V value) {
        return new Pair<K, V>().withKey(key).withValue(value);
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V v) {
        final V prev = value;
        value = v;
        return prev;
    }
}
