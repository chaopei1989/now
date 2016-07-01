package com.nao.im.net.data;

import com.nao.im.model.login.SocialUser;

interface IUserDataManager {
    SocialUser getUserByUserId(int userId);

    void cache(in SocialUser data);

    void cacheList(in List<SocialUser> data);

}