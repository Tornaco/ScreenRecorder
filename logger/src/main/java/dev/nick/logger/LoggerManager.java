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

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LoggerManager {

    final static HashMap<String, Logger> sLoggers = new HashMap<>();
    final static AtomicInteger sDebugLevel = new AtomicInteger(Log.WARN);

    static String tagPrefix;

    public static void setTagPrefix(String tagPrefix) {
        LoggerManager.tagPrefix = tagPrefix;
    }

    public static int getDebugLevel() {
        return sDebugLevel.get();
    }

    public static void setDebugLevel(int level) {
        sDebugLevel.set(level);
    }

    public static Logger getLogger(Class propertyClz) {
        return getLogger(propertyClz.getSimpleName());
    }

    public static Logger getLogger(String propName) {
        synchronized (sLoggers) {
            if (sLoggers.containsKey(propName)) return sLoggers.get(propName);
            Logger logger = new LoggerImpl(new LogTagBuilder() {
                @Override
                public String buildLogTag(String prop) {
                    return tagPrefix == null ? prop : tagPrefix + "-" + prop;
                }
            }, new CallingInfoBuilderImpl(), propName);

            logger.setDebugLevel(sDebugLevel.get());

            sLoggers.put(propName, logger);

            return logger;
        }
    }
}