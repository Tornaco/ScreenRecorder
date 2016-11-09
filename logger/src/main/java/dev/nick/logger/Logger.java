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

public interface Logger {

    boolean isDebuggable(int level);

    void setDebugLevel(int level);

    void funcEnter();

    void funcExit();

    void verbose(Object o);

    void info(Object o);

    void debug(Object o);

    void warn(Object o);

    void error(Object o);

    void trace(String trackMsg, Throwable throwable);
}
