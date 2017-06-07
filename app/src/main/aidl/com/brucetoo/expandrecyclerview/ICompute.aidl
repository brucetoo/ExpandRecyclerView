// ICompute.aidl
package com.brucetoo.expandrecyclerview;
import com.brucetoo.expandrecyclerview.TestParcel;

// Declare any non-default types here with import statements
interface ICompute {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    TestParcel add(int a, int b);

}
