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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Xiaofei on 16/6/21.
 */
public class ThrottleScheduler extends Scheduler {

    private static final ScheduledExecutorService sExecutorService = Executors.newScheduledThreadPool(10);

    private static final ConcurrentHashMap<Object, Boolean> sRunningMap = new ConcurrentHashMap<Object, Boolean>();

    private final Scheduler mScheduler;

    private final Object mLabel;

    private final long mDuration;

    private final TimeUnit mUnit;

    public ThrottleScheduler(Scheduler scheduler, Object label, long duration, TimeUnit unit) {
        mScheduler = scheduler;
        mLabel = label;
        mDuration = duration;
        mUnit = unit;
    }

    @Override
    public void call(Runnable runnable) {
        Boolean running = sRunningMap.get(mLabel);
        if (running == null || running) {
            mScheduler.call(runnable);
            sRunningMap.put(mLabel, false);
            sExecutorService.schedule(new ResumingRunnable(mLabel), mDuration, mUnit);
        } else {
            pause();
        }
    }

    private static class ResumingRunnable implements Runnable {

        private final Object mLabel;

        ResumingRunnable(Object label) {
            mLabel = label;
        }

        @Override
        public void run() {
            sRunningMap.put(mLabel, true);
        }
    }

}
