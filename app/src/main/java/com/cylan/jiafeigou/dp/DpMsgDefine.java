package com.cylan.jiafeigou.dp;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeSet;

/**
 * Created by cylan-hunt on 16-11-9.
 */


public class DpMsgDefine {
    @Message
    public static final class DPStandby extends DPSingle<DPStandby> {
        @Index(0)
        public boolean standby;
        @Index(1)
        public boolean warnEnable;
        @Index(2)
        public boolean led;
        @Index(3)
        public int autoRecord;

        public static DPStandby empty() {
            return new DPStandby();
        }

        public DPStandby() {
        }

        @Override
        public String toString() {
            return "DPStandby{" +
                    "standby=" + standby +
                    ", warnEnable=" + warnEnable +
                    ", led=" + led +
                    ", autoRecord=" + autoRecord +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte(this.standby ? (byte) 1 : (byte) 0);
            dest.writeByte(this.warnEnable ? (byte) 1 : (byte) 0);
            dest.writeByte(this.led ? (byte) 1 : (byte) 0);
            dest.writeInt(this.autoRecord);
        }

        protected DPStandby(Parcel in) {
            super(in);
            this.standby = in.readByte() != 0;
            this.warnEnable = in.readByte() != 0;
            this.led = in.readByte() != 0;
            this.autoRecord = in.readInt();
        }

        public static final Creator<DPStandby> CREATOR = new Creator<DPStandby>() {
            @Override
            public DPStandby createFromParcel(Parcel source) {
                return new DPStandby(source);
            }

            @Override
            public DPStandby[] newArray(int size) {
                return new DPStandby[size];
            }
        };
    }

    @Message
    public static final class DPNet extends DPSingle<DPNet> {
        /**
         * |NET_CONNECT | -1 | #绑定后的连接中 |
         * |NET_OFFLINE |  0 | #不在线 |
         * |NET_WIFI    |  1 | #WIFI网络 |
         * |NET_2G      |  2 | #2G网络 |
         * |NET_3G      |  3 | #3G网络 |
         * |NET_4G      |  4 | #4G网络  |
         * |NET_5G      |  5 | #5G网络  |
         */
        @Index(0)
        public int net = -1;
        @Index(1)
        public String ssid;


        @Override
        public String toString() {
            return "DPNet{" +
                    "net=" + net +
                    ", ssid='" + ssid + '\'' +
                    '}';
        }

        public static String getNormalString(DPNet net) {
            String result = null;
            switch (net.net) {
                case -1:
                    result = "绑定后的连接中";
                    break;
                case 0:
                    result = "设备离线中";
                    break;
                case 1:
                    result = TextUtils.isEmpty(net.ssid) ? "WiFi未开启" : net.ssid;
                    break;
                case 2:
                    result = "2G网络";
                    break;
                case 3:
                    result = "3G网络";
                    break;
                case 4:
                    result = "4G网络";
                    break;
                case 5:
                    result = "5G网络";
                    break;
            }
            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.net);
            dest.writeString(this.ssid);
        }

        public DPNet() {
        }

        public DPNet(int net, String ssid) {
            this.net = net;
            this.ssid = ssid;
        }

        protected DPNet(Parcel in) {
            super(in);
            this.net = in.readInt();
            this.ssid = in.readString();
        }

        public static final Creator<DPNet> CREATOR = new Creator<DPNet>() {
            @Override
            public DPNet createFromParcel(Parcel source) {
                return new DPNet(source);
            }

            @Override
            public DPNet[] newArray(int size) {
                return new DPNet[size];
            }
        };
    }

    @Message
    public static final class DPTimeZone extends DPSingle<DPTimeZone> {
        @Index(0)
        public String timezone;
        @Index(1)
        public int offset;

        @Override
        public String toString() {
            return "DPTimeZone{" +
                    "timezone='" + timezone + '\'' +
                    ", offset=" + offset +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.timezone);
            dest.writeInt(this.offset);
        }

        public DPTimeZone() {
            TimeZone timeZone = TimeZone.getDefault();
            this.timezone = timeZone.getID();
        }

        protected DPTimeZone(Parcel in) {
            super(in);
            this.timezone = in.readString();
            this.offset = in.readInt();
        }

        public static final Creator<DPTimeZone> CREATOR = new Creator<DPTimeZone>() {
            @Override
            public DPTimeZone createFromParcel(Parcel source) {
                return new DPTimeZone(source);
            }

            @Override
            public DPTimeZone[] newArray(int size) {
                return new DPTimeZone[size];
            }
        };
    }

    @Message
    public static final class DPBindLog extends DPSingle<DPBindLog> {

        @Index(0)
        public boolean isBind;
        @Index(1)
        public String account;
        @Index(2)
        public String oldAccount;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte(this.isBind ? (byte) 1 : (byte) 0);
            dest.writeString(this.account);
            dest.writeString(this.oldAccount);
        }

        public DPBindLog() {
        }

        protected DPBindLog(Parcel in) {
            super(in);
            this.isBind = in.readByte() != 0;
            this.account = in.readString();
            this.oldAccount = in.readString();
        }

        public static final Creator<DPBindLog> CREATOR = new Creator<DPBindLog>() {
            @Override
            public DPBindLog createFromParcel(Parcel source) {
                return new DPBindLog(source);
            }

            @Override
            public DPBindLog[] newArray(int size) {
                return new DPBindLog[size];
            }
        };

        @Override
        public String toString() {
            return "DPBindLog{" +
                    "isBind=" + isBind +
                    ", account='" + account + '\'' +
                    ", oldAccount='" + oldAccount + '\'' +
                    '}';
        }
    }

    //系统消息使用
    @Message
    public static final class DPSdcardSummary extends DPSingle<DPSdcardSummary> implements Parcelable {
        @Index(0)
        public boolean hasSdcard;
        @Index(1)
        public int errCode;

        public DPSdcardSummary() {
        }

        protected DPSdcardSummary(Parcel in) {
            super(in);
            hasSdcard = in.readByte() != 0;
            errCode = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte((byte) (hasSdcard ? 1 : 0));
            dest.writeInt(errCode);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<DPSdcardSummary> CREATOR = new Creator<DPSdcardSummary>() {
            @Override
            public DPSdcardSummary createFromParcel(Parcel in) {
                return new DPSdcardSummary(in);
            }

            @Override
            public DPSdcardSummary[] newArray(int size) {
                return new DPSdcardSummary[size];
            }
        };

        @Override
        public String toString() {
            return "DPSdcardSummary{" +
                    "hasSdcard=" + hasSdcard +
                    ", errCode=" + errCode +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            DPSdcardSummary summary = (DPSdcardSummary) o;

            if (hasSdcard != summary.hasSdcard) return false;
            return errCode == summary.errCode;

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (hasSdcard ? 1 : 0);
            result = 31 * result + errCode;
            return result;
        }
    }

    @Message
    public static final class DPSdStatus extends DPSingle<DPSdStatus> implements Parcelable {
        @Index(0)
        public long total;
        @Index(1)
        public long used;
        @Index(2)
        public int err = -1;
        @Index(3)
        public boolean hasSdcard;

        public DPSdStatus() {
        }

        @Override
        public String toString() {
            return "DPSdStatus{" +
                    "total=" + total +
                    ", used=" + used +
                    ", err=" + err +
                    ", hasSdcard=" + hasSdcard +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeLong(this.total);
            dest.writeLong(this.used);
            dest.writeInt(this.err);
            dest.writeByte(this.hasSdcard ? (byte) 1 : (byte) 0);
        }

        protected DPSdStatus(Parcel in) {
            super(in);
            this.total = in.readLong();
            this.used = in.readLong();
            this.err = in.readInt();
            this.hasSdcard = in.readByte() != 0;
        }

        public static final Creator<DPSdStatus> CREATOR = new Creator<DPSdStatus>() {
            @Override
            public DPSdStatus createFromParcel(Parcel source) {
                return new DPSdStatus(source);
            }

            @Override
            public DPSdStatus[] newArray(int size) {
                return new DPSdStatus[size];
            }
        };
    }

    @Message
    public static final class DPAlarmInfo extends DPSingle<DPAlarmInfo> implements Parcelable {
        @Index(0)
        public int timeStart;
        @Index(1)
        public int timeEnd;
        /**
         * 每周的星期*， 从低位到高位代表周一到周日。如0b00000001代表周一，0b01000000代表周日
         */
        @Index(2)
        public int day;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.timeStart);
            dest.writeInt(this.timeEnd);
            dest.writeInt(this.day);
        }

        public DPAlarmInfo() {
            timeStart = 0;
            timeEnd = 5947;
            day = 127;//默认开启
        }

        protected DPAlarmInfo(Parcel in) {
            super(in);
            this.timeStart = in.readInt();
            this.timeEnd = in.readInt();
            this.day = in.readInt();
        }

        public static final Creator<DPAlarmInfo> CREATOR = new Creator<DPAlarmInfo>() {
            @Override
            public DPAlarmInfo createFromParcel(Parcel source) {
                return new DPAlarmInfo(source);
            }

            @Override
            public DPAlarmInfo[] newArray(int size) {
                return new DPAlarmInfo[size];
            }
        };

        @Override
        public String toString() {
            return "DPAlarmInfo{" +
                    "timeStart=" + timeStart +
                    ", timeEnd=" + timeEnd +
                    ", duration=" + day +
                    '}';
        }
    }

    @Message
    public static final class DPAlarm extends DPSingle<DPAlarm> implements Parcelable {//505 报警消息
        @Index(0)
        public int time;
        @Index(1)
        public int isRecording;
        @Index(2)
        public int fileIndex;
        @Index(3)
        public int type;
        @Index(4)
        public String tly;//全景设备陀螺仪。'0'俯视, '1' 平视。
        @Ignore
        public static DPAlarm empty = new DPAlarm();

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.time);
            dest.writeInt(this.isRecording);
            dest.writeInt(this.fileIndex);
            dest.writeInt(this.type);
        }

        public DPAlarm() {
        }

        @Override
        public String toString() {
            return "DPAlarm{" +
                    "startTime=" + time +
                    ", isRecording=" + isRecording +
                    ", fileIndex=" + fileIndex +
                    ", type=" + type +
                    '}';
        }

        protected DPAlarm(Parcel in) {
            super(in);
            this.time = in.readInt();
            this.isRecording = in.readInt();
            this.fileIndex = in.readInt();
            this.type = in.readInt();
        }

        public static final Creator<DPAlarm> CREATOR = new Creator<DPAlarm>() {
            @Override
            public DPAlarm createFromParcel(Parcel source) {
                return new DPAlarm(source);
            }

            @Override
            public DPAlarm[] newArray(int size) {
                return new DPAlarm[size];
            }
        };

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            DPAlarm dpAlarm = (DPAlarm) o;

            if (time != dpAlarm.time) return false;
            if (isRecording != dpAlarm.isRecording) return false;
            if (fileIndex != dpAlarm.fileIndex) return false;
            if (type != dpAlarm.type) return false;
            return tly != null ? tly.equals(dpAlarm.tly) : dpAlarm.tly == null;

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + time;
            result = 31 * result + isRecording;
            result = 31 * result + fileIndex;
            result = 31 * result + type;
            result = 31 * result + (tly != null ? tly.hashCode() : 0);
            return result;
        }
    }

    @Message//504
    public static final class DPNotificationInfo extends DPSingle<DPNotificationInfo> implements Parcelable {
        @Index(0)
        public int notification;
        @Index(1)
        public int duration;

        @Ignore
        public static DPNotificationInfo empty = new DPNotificationInfo();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DPNotificationInfo that = (DPNotificationInfo) o;

            if (notification != that.notification) return false;
            return duration == that.duration;

        }

        @Override
        public int hashCode() {
            int result = notification;
            result = 31 * result + duration;
            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.notification);
            dest.writeInt(this.duration);
        }

        @Override
        public String toString() {
            return "DPNotificationInfo{" +
                    "notification=" + notification +
                    ", duration=" + duration +
                    '}';
        }

        public DPNotificationInfo() {
        }

        protected DPNotificationInfo(Parcel in) {
            super(in);
            this.notification = in.readInt();
            this.duration = in.readInt();
        }

        public static final Creator<DPNotificationInfo> CREATOR = new Creator<DPNotificationInfo>() {
            @Override
            public DPNotificationInfo createFromParcel(Parcel source) {
                return new DPNotificationInfo(source);
            }

            @Override
            public DPNotificationInfo[] newArray(int size) {
                return new DPNotificationInfo[size];
            }
        };
    }

    @Message
    public static final class DPTimeLapse extends DPSingle<DPTimeLapse> implements Parcelable {
        @Index(0)
        public int timeStart;
        @Index(1)
        public int timePeriod;
        @Index(2)
        public int timeDuration;
        @Index(3)
        public int status;

        @Ignore
        public static DPTimeLapse empty = new DPTimeLapse();

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.timeStart);
            dest.writeInt(this.timePeriod);
            dest.writeInt(this.timeDuration);
            dest.writeInt(this.status);
        }

        public DPTimeLapse() {
        }

        protected DPTimeLapse(Parcel in) {
            super(in);
            this.timeStart = in.readInt();
            this.timePeriod = in.readInt();
            this.timeDuration = in.readInt();
            this.status = in.readInt();
        }

        public static final Creator<DPTimeLapse> CREATOR = new Creator<DPTimeLapse>() {
            @Override
            public DPTimeLapse createFromParcel(Parcel source) {
                return new DPTimeLapse(source);
            }

            @Override
            public DPTimeLapse[] newArray(int size) {
                return new DPTimeLapse[size];
            }
        };

        @Override
        public String toString() {
            return "DPTimeLapse{" +
                    "timeStart=" + timeStart +
                    ", timePeriod=" + timePeriod +
                    ", timeDuration=" + timeDuration +
                    ", status=" + status +
                    '}';
        }
    }

    @Message
    public static final class DPCamCoord extends DPSingle<DPCamCoord> implements Parcelable {
        @Index(0)
        public int x;
        @Index(1)
        public int y;
        @Index(2)
        public int r;

        @Ignore
        public static DPCamCoord empty = new DPCamCoord();

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.x);
            dest.writeInt(this.y);
            dest.writeInt(this.r);
        }

        public DPCamCoord() {
        }

        protected DPCamCoord(Parcel in) {
            super(in);
            this.x = in.readInt();
            this.y = in.readInt();
            this.r = in.readInt();
        }

        public static final Creator<DPCamCoord> CREATOR = new Creator<DPCamCoord>() {
            @Override
            public DPCamCoord createFromParcel(Parcel source) {
                return new DPCamCoord(source);
            }

            @Override
            public DPCamCoord[] newArray(int size) {
                return new DPCamCoord[size];
            }
        };

        @Override
        public String toString() {
            return "DPCamCoord{" +
                    "x=" + x +
                    ", y=" + y +
                    ", r=" + r +
                    '}';
        }
    }

    @Message
    public static final class DPBellCallRecord extends DPMulti<DPBellCallRecord> implements Parcelable {

        @Index(0)
        public int isOK;

        @Index(1)
        public int time;

        @Index(2)
        public int duration;

        @Index(3)
        public int type;

        @Ignore
        public static DPBellCallRecord empty = new DPBellCallRecord();

        public DPBellCallRecord() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.isOK);
            dest.writeInt(this.time);
            dest.writeInt(this.duration);
            dest.writeInt(this.type);
        }

        protected DPBellCallRecord(Parcel in) {
            super(in);
            this.isOK = in.readInt();
            this.time = in.readInt();
            this.duration = in.readInt();
            this.type = in.readInt();
        }

        public static final Creator<DPBellCallRecord> CREATOR = new Creator<DPBellCallRecord>() {
            @Override
            public DPBellCallRecord createFromParcel(Parcel source) {
                return new DPBellCallRecord(source);
            }

            @Override
            public DPBellCallRecord[] newArray(int size) {
                return new DPBellCallRecord[size];
            }
        };

        @Override
        public String toString() {
            return "DPBellCallRecord{" +
                    "state=" + isOK +
                    ", startTime=" + time +
                    ", duration=" + duration +
                    ", type=" + type +
                    '}';
        }
    }

    public static final class DPPrimary<T> extends DataPoint {
        @Index(0)
        public T value;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeValue(value);
        }

        public DPPrimary() {
        }

        public DPPrimary(T value) {
            this.value = value;
        }

        @Override
        public byte[] toBytes() {
            return DpUtils.pack(value);
        }

        protected DPPrimary(Parcel in) {
            super(in);
            this.value = (T) in.readValue(Object.class.getClassLoader());
        }

        public static final Creator<DPPrimary> CREATOR = new Creator<DPPrimary>() {
            @Override
            public DPPrimary createFromParcel(Parcel source) {
                return new DPPrimary(source);
            }

            @Override
            public DPPrimary[] newArray(int size) {
                return new DPPrimary[size];
            }
        };
    }

    public static final class DPSet<T> extends DataPoint {
        public TreeSet<T> value;
        private List<T> listValue = new ArrayList<>();

        @Override
        public int describeContents() {
            return 0;
        }

        public List<T> list() {
            listValue.clear();
            listValue.addAll(value);
            return listValue;
        }


        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeList(new ArrayList<>(value));
        }

        public DPSet() {
        }

        public DPSet(TreeSet<T> value) {
            this.value = value;
        }

        protected DPSet(Parcel in) {
            super(in);
            List<T> list = new ArrayList<>();
            in.readList(list, ArrayList.class.getClassLoader());
            this.value = new TreeSet<>(list);
        }

        public static final Creator<DPSet> CREATOR = new Creator<DPSet>() {
            @Override
            public DPSet createFromParcel(Parcel source) {
                return new DPSet(source);
            }

            @Override
            public DPSet[] newArray(int size) {
                return new DPSet[size];
            }
        };
    }

    public static abstract class DPSingle<T extends DataPoint> extends DataPoint {

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
        }

        public DPSingle() {
        }

        protected DPSingle(Parcel in) {
            super(in);
        }
    }

    public static abstract class DPMulti<T extends DataPoint> extends DataPoint {
        @Ignore
        public boolean isRead;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte(this.isRead ? (byte) 1 : (byte) 0);
        }

        public DPMulti() {
        }

        protected DPMulti(Parcel in) {
            super(in);
            this.isRead = in.readByte() != 0;
        }
    }

    @Message
    public static final class DPWonderItem extends DPMulti<DPWonderItem> implements Parcelable {

        public static final int TYPE_PIC = 0;
        public static final int TYPE_VIDEO = 1;
        public static final int TYPE_LOAD = 2;
        public static final int TYPE_NO_MORE = 3;
        private static DPWonderItem guideBean;
        private static DPWonderItem nomore;

        @Index(0)
        public String cid;
        @Index(1)
        public int time;
        @Index(2)
        public int msgType;
        @Index(3)
        public int regionType;
        @Index(4)
        public String fileName;
        @Index(5)
        public String place;

        @Override
        public int describeContents() {
            return 0;
        }


        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.cid);
            dest.writeInt(this.time);
            dest.writeInt(this.msgType);
            dest.writeInt(this.regionType);
            dest.writeString(this.fileName);
            dest.writeString(this.place);
        }

        public DPWonderItem() {
            dpMsgId = 602;
        }

        protected DPWonderItem(Parcel in) {
            this.cid = in.readString();
            this.time = in.readInt();
            this.msgType = in.readInt();
            this.regionType = in.readInt();
            this.fileName = in.readString();
            this.place = in.readString();
        }

        public static final Creator<DPWonderItem> CREATOR = new Creator<DPWonderItem>() {
            @Override
            public DPWonderItem createFromParcel(Parcel source) {
                return new DPWonderItem(source);
            }

            @Override
            public DPWonderItem[] newArray(int size) {
                return new DPWonderItem[size];
            }
        };

        private static DPWonderItem loadBean = new DPWonderItem();

        public static DPWonderItem getEmptyLoadTypeBean() {
            if (loadBean == null)
                loadBean = new DPWonderItem();
            loadBean.msgType = TYPE_LOAD;
            return loadBean;
        }

        public static DPWonderItem getNoMoreTypeBean() {
            if (nomore == null)
                nomore = new DPWonderItem();
            nomore.msgType = TYPE_NO_MORE;
            return nomore;
        }

        public static DPWonderItem getGuideBean() {
            if (guideBean == null) {
                guideBean = new DPWonderItem();
                guideBean.msgType = TYPE_VIDEO;
                guideBean.fileName = "http://yf.cylan.com.cn:82/Garfield/1045020208160b9706425470.mp4";
                guideBean.cid = "www.cylan.com";
            }
            guideBean.time = (int) (System.currentTimeMillis() / 1000);
            return guideBean;
        }
    }

    @Message
    public static final class DPMineMesg {
        @Index(0)
        public String cid;
        @Index(1)
        public boolean isDone;
        @Index(2)
        public String account;
        @Index(3)
        public String sn;

        public DPMineMesg() {
        }

        public DPMineMesg(String cid, boolean isDone, String account, String sn) {
            this.cid = cid;
            this.isDone = isDone;
            this.account = account;
            this.sn = sn;
        }
    }

    @Message
    public static final class DPSystemMesg {
        @Index(0)
        public String title;
        @Index(1)
        public String content;

        public DPSystemMesg() {
        }

        public DPSystemMesg(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }

    @Message
    public static final class DpSdcardFormatRsp extends DPSingle<DpSdcardFormatRsp> implements Parcelable {
        @Index(0)
        public int placeHolder;

        public DpSdcardFormatRsp() {
        }

        protected DpSdcardFormatRsp(Parcel in) {
            super(in);
            placeHolder = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(placeHolder);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<DpSdcardFormatRsp> CREATOR = new Creator<DpSdcardFormatRsp>() {
            @Override
            public DpSdcardFormatRsp createFromParcel(Parcel in) {
                return new DpSdcardFormatRsp(in);
            }

            @Override
            public DpSdcardFormatRsp[] newArray(int size) {
                return new DpSdcardFormatRsp[size];
            }
        };
    }

    @Message
    public static class DPCameraWarnAndWonder {
        @Index(0)
        public long ctime;

        public DPCameraWarnAndWonder() {
        }

        public DPCameraWarnAndWonder(long ctime) {
            this.ctime = ctime;
        }
    }

    @Message
    public static class DpHangMode extends DPSingle<DpHangMode> implements Parcelable {
        @Index(0)
        public String mode = "0";


        public int safeGetValue() {
            try {
                return Integer.parseInt(mode);
            } catch (Exception e) {
                return 0;
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.mode);
        }

        public DpHangMode() {
        }

        protected DpHangMode(Parcel in) {
            super(in);
            this.mode = in.readString();
        }

        public static final Creator<DpHangMode> CREATOR = new Creator<DpHangMode>() {
            @Override
            public DpHangMode createFromParcel(Parcel source) {
                return new DpHangMode(source);
            }

            @Override
            public DpHangMode[] newArray(int size) {
                return new DpHangMode[size];
            }
        };
    }

    @Deprecated
    public interface EMPTY {
        DPNet NET = new DPNet();
        DPStandby STANDBY = new DPStandby();
        DPTimeZone TIME_ZONE = new DPTimeZone();
        DPBindLog BIND_LOG = new DPBindLog();
        DPSdcardSummary SDCARD_SUMMARY = new DPSdcardSummary();
        DPSdStatus SD_STATUS = new DPSdStatus();
        DPAlarmInfo ALARM_INFO = new DPAlarmInfo();
        DPAlarm ALARM = new DPAlarm();
        DPNotificationInfo NOTIFICATION_INFO = new DPNotificationInfo();
        DPTimeLapse TIME_LAPSE = new DPTimeLapse();
        DPCamCoord CAM_COORD = new DPCamCoord();
        DPBellCallRecord BELL_CALL_RECORD = new DPBellCallRecord();
        DPPrimary<Integer> DP_INT = new DPPrimary<>(0);
        DPPrimary<Boolean> DP_BOOL = new DPPrimary<>(false);
        DPPrimary<Long> DP_LONG = new DPPrimary<>(0L);
        DPPrimary<String> DP_STRING = new DPPrimary<>("");
        DPSet DP_SET = new DPSet<>(new TreeSet<>());
        DPWonderItem WONDER_ITEM = new DPWonderItem();
        DPMineMesg MINE_MESG = new DPMineMesg();
        DPSystemMesg SYSTEM_MESG = new DPSystemMesg();
        DpSdcardFormatRsp SDCARD_FORMAT_RSP = new DpSdcardFormatRsp();
        DpHangMode dpHangMode = new DpHangMode();
    }
}
