package com.cylan.jiafeigou.n.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by hunt on 16-5-14.
 */
public class DeviceBean implements Parcelable {
    public String uuid;
    public String sn;
    public String alias;
    public String shareAccount;
    public int pid;
    public int isChooseFlag;
    public int hasShareCount;


    public DeviceBean() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceBean bean = (DeviceBean) o;

        return TextUtils.equals(uuid, bean.uuid);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }


    @Override
    public String toString() {
        return "DeviceBean{" +
                "uuid='" + uuid + '\'' +
                ", sn='" + sn + '\'' +
                ", alias='" + alias + '\'' +
                ", shareAccount='" + shareAccount + '\'' +
                ", pid=" + pid +
                ", isChooseFlag=" + isChooseFlag +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uuid);
        dest.writeString(this.sn);
        dest.writeString(this.alias);
        dest.writeString(this.shareAccount);
        dest.writeInt(this.pid);
        dest.writeInt(this.isChooseFlag);
        dest.writeInt(this.hasShareCount);
    }

    protected DeviceBean(Parcel in) {
        this.uuid = in.readString();
        this.sn = in.readString();
        this.alias = in.readString();
        this.shareAccount = in.readString();
        this.pid = in.readInt();
        this.isChooseFlag = in.readInt();
        this.hasShareCount = in.readInt();
    }

    public static final Creator<DeviceBean> CREATOR = new Creator<DeviceBean>() {
        @Override
        public DeviceBean createFromParcel(Parcel source) {
            return new DeviceBean(source);
        }

        @Override
        public DeviceBean[] newArray(int size) {
            return new DeviceBean[size];
        }
    };
}