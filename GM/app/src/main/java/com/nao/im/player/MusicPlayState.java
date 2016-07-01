/*
 * Copyright (c) 2015 [1076559197@qq.com | tchen0707@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nao.im.player;

public class MusicPlayState {

    public static final int MPS_UNEXPECTED = -2; // 播放列表为空

    public static final int MPS_LIST_EMPTY = -1; // 播放列表为空

    public static final int MPS_LIST_FULL = 0; // 播放列表有数据

    public static final int MPS_PREPARED = 1; // 准备就绪

    public static final int MPS_PLAYING = 2; // 播放中

    public static final int MPS_PAUSE = 3; // 暂停

    public static final int MPS_STOP = 4; // 停止

    public static String getName(int state) {
        switch (state) {
            case MPS_UNEXPECTED:
                return "MPS_UNEXPECTED";
            case MPS_LIST_EMPTY:
                return "MPS_LIST_EMPTY";
            case MPS_LIST_FULL:
                return "MPS_LIST_FULL";
            case MPS_PREPARED:
                return "MPS_PREPARED";
            case MPS_PLAYING:
                return "MPS_PLAYING";
            case MPS_PAUSE:
                return "MPS_PAUSE";
            case MPS_STOP:
                return "MPS_STOP";
            default:
                return "ERROR";
        }
    }

}