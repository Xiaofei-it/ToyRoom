/**
 *
 * Copyright 2016 Xiaofei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package xiaofei.library.shelly.scheduler;

import xiaofei.library.concurrentutils.util.Action;

/**
 * Created by Xiaofei on 16/7/27.
 */
public abstract class Scheduler implements Action<Runnable> {

    private static final int STATE_RUNNING = 0;

    private static final int STATE_PAUSE = 1;

    private volatile int mState;

    public Scheduler() {
        mState = STATE_RUNNING;
    }

    public final void pause() {
        mState = STATE_PAUSE;
    }

    public final boolean isRunning() {
        return mState == STATE_RUNNING;
    }
}
