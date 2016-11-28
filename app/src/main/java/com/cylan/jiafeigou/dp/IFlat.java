package com.cylan.jiafeigou.dp;

import com.cylan.jiafeigou.n.mvp.model.BaseBean;

import java.util.List;

/**
 * Created by cylan-hunt on 16-11-17.
 */

public interface IFlat {

    /**
     * 保存uuid
     *
     * @param account
     * @param uuid
     */
    void cache(String account, String uuid);

    void cache(String account, BaseBean dpDevice);

    /**
     * 缓存清空
     */
    void clean();

    /**
     * 获取当前账号下的uuidList
     *
     * @return
     */
    List<String> getUuidList(String account);


    DpMsgDefine.DpWrap removeMsg(String account, String uuid);

    DpMsgDefine.DpWrap getWrap(String account, String uuid);

    void cache(String account, String uuid, DpMsgDefine.DpMsg msg);

    List<DpMsgDefine.DpWrap> getAllDevices(String account);
}