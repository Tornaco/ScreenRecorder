/*
 *  Copyright (c) 2015-2016 Nick Guo
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package dev.nick.logger;

import android.util.Log;

class LoggerImpl implements Logger {

    int debugLevel = Log.VERBOSE;

    LogTagBuilder mLogTagBuilder;
    CallingInfoBuilder mCallingInfoBuilder;

    String mLogTag;

    public LoggerImpl(LogTagBuilder tagBuilder, CallingInfoBuilder infoBuilder, String prop) {
        this.mLogTagBuilder = tagBuilder;
        this.mCallingInfoBuilder = infoBuilder;
        this.mLogTag = mLogTagBuilder.buildLogTag(prop);
    }

    @Override
    public boolean isDebuggable(int level) {
        return level >= debugLevel;
    }

    @Override
    public void setDebugLevel(int level) {
        debugLevel = level;
    }

    @Override
    public void funcEnter() {
        if (isDebuggable(Log.VERBOSE))
            Log.v(mLogTag, mCallingInfoBuilder.getCallingInfo() + "\tENTER");
    }

    @Override
    public void funcExit() {
        if (isDebuggable(Log.VERBOSE))
            Log.v(mLogTag, mCallingInfoBuilder.getCallingInfo() + "\tEXIT");
    }

    @Override
    public void info(Object o) {
        if (isDebuggable(Log.INFO))
            Log.i(mLogTag, mCallingInfoBuilder.getCallingInfo()
                    + "---"
                    + String.valueOf(o));
    }

    @Override
    public void debug(Object o) {
        if (isDebuggable(Log.DEBUG))
            Log.d(mLogTag, mCallingInfoBuilder.getCallingInfo()
                    + "---"
                    + String.valueOf(o));
    }

    @Override
    public void verbose(Object o) {
        if (isDebuggable(Log.VERBOSE))
            Log.v(mLogTag, mCallingInfoBuilder.getCallingInfo()
                    + "---"
                    + String.valueOf(o));
    }

    @Override
    public void warn(Object o) {
        if (isDebuggable(Log.WARN))
            Log.w(mLogTag, mCallingInfoBuilder.getCallingInfo()
                    + "---"
                    + String.valueOf(o));
    }

    @Override
    public void error(Object o) {
        if (isDebuggable(Log.ERROR))
            Log.e(mLogTag, mCallingInfoBuilder.getCallingInfo()
                    + "---"
                    + String.valueOf(o));
    }

    @Override
    public void trace(String traceMsg, Throwable throwable) {
        if (isDebuggable(Log.ASSERT))
            Log.w(mLogTag, traceMsg
                    + mCallingInfoBuilder.getCallingInfo()
                    + "---"
                    + Log.getStackTraceString(throwable));
    }
}