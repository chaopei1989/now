// IDefaultSharedPreference.aidl
package com.multiprocess.crossprocess;

// Declare any non-default types here with import statements

interface ICrossProcessSharedPreference {
    int getInt(String key, int defValue);
    void putInt(String key, int value);
    long getLong(String key, long defValue);
    void putLong(String key, long value);
    boolean getBoolean(String key, boolean defValue);
    void putBoolean(String key, boolean value);
    String getString(String key, String defValue);
    void putString(String key, String value);
}