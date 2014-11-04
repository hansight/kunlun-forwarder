package com.hansight.kunlun.forwarder.collector.utils;


/**
 * A generic class for pairs.
 * <p/>
 * <p><b>This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own risk.
 * This code and its internal interfaces are subject to change or
 * deletion without notice.</b>
 */
public class Pair<A, B> {

    private A first;
    private B second;

    public Pair(A fst, B snd) {
        this.first = fst;
        this.second = snd;
    }

    public A first() {
        return first;
    }

    public B second() {
        return second;
    }

    public void setFirst(A first) {
        this.first = first;
    }

    public void setSecond(B second) {
        this.second = second;
    }

    public String toString() {
        return "Pair[" + first + "," + second + "]";
    }

    private static boolean equals(Object x, Object y) {
        return (x == null && y == null) || (x != null && x.equals(y));
    }

    public boolean equals(Object other) {
        return other instanceof Pair<?, ?> && equals(first, ((Pair<?, ?>) other).first)
                && equals(second, ((Pair<?, ?>) other).second);
    }

    public int hashCode() {
        if (first == null)
            return (second == null) ? 0 : second.hashCode() + 1;
        else if (second == null)
            return first.hashCode() + 2;
        else
            return first.hashCode() * 17 + second.hashCode();
    }

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<A, B>(a, b);
    }
}