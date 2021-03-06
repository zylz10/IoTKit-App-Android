package com.cylan.jiafeigou.cache.db.view;

import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.base.view.JFGSourceManager;

import java.util.List;

import rx.Observable;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public interface IDPTaskDispatcher {
    /**
     * 对所有未经确认的记录进行同步
     */
    void perform();

    /**
     * @param entity 需要操作的 dp 实体
     */
    Observable<IDPTaskResult> perform(IDPEntity entity);

    Observable<IDPTaskResult> perform(List<? extends IDPEntity> entities);

    void setDBHelper(IDBHelper helper);

    void setSourceManager(JFGSourceManager manager);

    void setTaskFactory(IDPTaskFactory taskFactory);

    void setPropertyParser(IPropertyParser parser);

    void setAppCmd(AppCmd appCmd);
}
