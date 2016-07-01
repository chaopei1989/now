package com.nao.im.model.login;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nao.im.net.parser.IListParser;

import org.litepal.crud.DataSupport;

import java.util.List;

public class SocialUser extends DataSupport implements Parcelable {

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_NULL = 1;
    public static final int TYPE_QQ = 2;
    private int type;//从哪里登陆
    private String userName;//用户昵称
    private String iconUrl;//头像地址
    private int userId;//用户ID
    private int gender;
    private String openId;
    private String token;
    private long expiresTime;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SocialUser)) {
            return false;
        }
        SocialUser ob = (SocialUser)o;
        if (userId == ob.getUserId()) {
            return true;
        } else {
            return false;
        }
    }

    public SocialUser(){}

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserID(int userID) {
        this.userId = userID;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresTime() {
        return expiresTime;
    }

    public void setExpiresTime(long expiresTime) {
        this.expiresTime = expiresTime;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("SocialUser")
                .append("\nUserID=").append(userId)
                .append("\nUserName=").append(userName)
                .append("\nType=").append(type)
                .append("\nGender=").append(gender)
                .append("\nGender=").append(gender)
                .append("\nIconUrl=").append(iconUrl)
                .append("\nopenId=").append(openId)
                .append("\nToken=").append(token)
                .append("\nexpiresTime=").append(expiresTime);
        return sb.toString();
    }

    public SocialUser(Parcel source) {
        this.type = source.readInt();
        this.userName = source.readString();
        this.iconUrl = source.readString();
        this.userId = source.readInt();
        this.gender = source.readInt();
        this.openId = source.readString();
        this.token = source.readString();
        this.expiresTime = source.readLong();
    }

    public static final IListParser<SocialUser> LIST_PARSER = new IListParser<SocialUser>() {
        @Override
        public List<SocialUser> parseList(String json) {
            Gson gson = new Gson();
            return gson.fromJson(json,
                    new TypeToken<List<SocialUser>>(){}.getType());
        }
    };

    public static final Creator<SocialUser> CREATOR = new Creator<SocialUser>() {
        @Override
        public SocialUser createFromParcel(Parcel source) {
            return new SocialUser(source);
        }

        @Override
        public SocialUser[] newArray(int size) {
            return new SocialUser[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeString(this.userName);
        dest.writeString(this.iconUrl);
        dest.writeInt(this.userId);
        dest.writeInt(this.gender);
        dest.writeString(this.openId);
        dest.writeString(this.token);
        dest.writeLong(this.expiresTime);
    }
}
