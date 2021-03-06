package com.cylan.jiafeigou.misc.pty;

/**
 * 这些属性是静态的,从产品经理那边过来.
 * Created by hds on 17-5-28.
 */

public interface IProperty {

    /**
     * 初始化
     */
    void initialize();

    /**
     * 这些属性tag 从properties.json中获取
     *
     * @param pidOrOs
     * @param tag
     * @return
     */
    boolean hasProperty(int pidOrOs, String tag);

    boolean hasProperty(int pidOrOs, String tag, boolean share);

    String property(int pidOrOs, String tag);

    String property(int pidOrOs, String tag, boolean share);

    /**
     * cam or bell
     *
     * @param serial
     * @return
     */
    boolean isSerial(final String serial, final int pidOrOs);

    int getOSType(String cid);
}
