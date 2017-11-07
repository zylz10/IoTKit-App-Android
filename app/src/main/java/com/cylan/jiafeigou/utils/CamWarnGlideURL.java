package com.cylan.jiafeigou.utils;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Created by yzd on 16-12-13.
 */

public class CamWarnGlideURL extends JFGGlideURL {
    private int time, index;
    private boolean isface = false;

    public CamWarnGlideURL(String cid, String fileName, int type) {
        super(cid, fileName, type);
    }

    public CamWarnGlideURL(String cid, String fileName, int time, int index, int type) {
        super(cid, fileName, type);
        this.index = index;
        this.time = time;
    }

    public CamWarnGlideURL(String cid, int regionType, String faceId) {
        super(cid, faceId, regionType);
        isface = true;
    }

    @Override
    public URL toURL() throws MalformedURLException {
        if (isface) {
            try {
                String urlV2;
//                if (stranger) {
////                urlV2 = String.format(Locale.getDefault(), "/7day/%s/%s/AI/%s/%s.jpg", vid, account, cid, faceId);
//                    urlV2 = BaseApplication.getAppComponent().getCmd().getSignedCloudUrl(this.regionType, faceId);
//                } else {
                Account account = DataSourceManager.getInstance().getAccount();
                String acc = account == null ? "" : account.getAccount();

                urlV2 = String.format(Locale.getDefault(), "/long/%s/%s/AI/%s/%s.jpg", vid, acc, cid, timestamp);
                urlV2 = BaseApplication.getAppComponent().getCmd().getSignedCloudUrl(this.regionType, urlV2);
//                }
                return new URL(urlV2);
            } catch (Exception e) {
                AppLogger.e(String.format("err:%s", e.getLocalizedMessage()));
            }
        }
        return super.toURL();
    }

    public int getTime() {
        return time;
    }

    public int getIndex() {
        return index;
    }
}
