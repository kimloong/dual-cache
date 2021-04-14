package com.github.kimloong.dualcache;

/**
 * Constants
 *
 * @author kimloong
 */
public class Operations {

    /**
     * evict operation
     */
    public static final byte EVICT = 1;

    /**
     * put operation
     */
    public static final byte PUT = 2;

    /**
     * putIfAbsent operation
     */
    public static final byte PUT_IF_ABSENT = 3;

    /**
     * clear operation
     */
    public static final byte CLEAR = 9;

    private Operations() {
        //EMPTY
    }
}
