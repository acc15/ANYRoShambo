package com.appctek.anyroshambo.util;

/**
* @author Vyacheslav Mayorov
* @since 2014-19-01
*/
public final class Pair<K,V> {
    public K first;
    public V second;

    public Pair() {
    }

    public Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }
}
