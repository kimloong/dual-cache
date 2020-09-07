package com.github.kimloong.dualcache;

/**
 * Constants
 *
 * @author kimloong
 */
public class Constants {

    /**
     * evict operation
     */
    public static final String OPERATION_EVICT = "evict";

    /**
     * put operation
     */
    public static final String OPERATION_PUT = "put";

    /**
     * putIfAbsent operation
     */
    public static final String OPERATION_PUT_IF_ABSENT = "putIfAbsent";

    /**
     * clear operation
     */
    public static final String OPERATION_CLEAR = "clear";

    private Constants() {
        //EMPTY
    }
}
