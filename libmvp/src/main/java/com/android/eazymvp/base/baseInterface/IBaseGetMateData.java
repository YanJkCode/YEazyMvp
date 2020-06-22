package com.android.eazymvp.base.baseInterface;

public interface IBaseGetMateData {
    /**
     * 获取metaData 的指定名数据
     *
     * @param name
     * @return
     */
    String getMataDataString(String name);

    /**
     * 获取metaData 的指定名数据
     *
     * @param name
     * @return
     */
    float getMataDataFloat(String name);

    /**
     * 获取metaData 的指定名数据
     *
     * @param name
     * @return
     */
    double getMataDataDouble(String name);

    /**
     * 获取metaData 的指定名数据
     *
     * @param name
     * @return
     */
    char getMataDataChar(String name);

    /**
     * 获取metaData 的指定名数据
     *
     * @param name
     * @return
     */
    byte getMataDataByte(String name);

    /**
     * 获取metaData 的指定名数据
     *
     * @param name
     * @return
     */
    int getMataDataInt(String name);

    /**
     * 获取metaData 的指定名数据
     *
     * @param name
     * @return
     */
    long getMataDataLong(String name);
}
