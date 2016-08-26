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

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xiaofei.library.shelly.runnable.ScheduledRunnable;
import xiaofei.library.shelly.util.Config;

/**
 * Created by Xiaofei on 16/5/31.
 */
public class UiThreadScheduler extends Scheduler {

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    //下面这个改变比较重要！！！
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private void scheduleInternal(Runnable runnable) {
        boolean isMainThread = Looper.getMainLooper() == Looper.myLooper();
        if (isMainThread) {
            if (Config.DEBUG) {
                System.out.println("post ui0 " + Thread.currentThread().getName());
            }
            runnable.run();
        } else {
            if (Config.DEBUG) {
                System.out.println("post ui1 " + Thread.currentThread().getName());
            }
            sHandler.post(runnable);
        }
    }

    @Override
    public void call(Runnable runnable) {
        if (runnable instanceof ScheduledRunnable) {
            final ScheduledRunnable scheduledRunnable = (ScheduledRunnable) runnable;
            if (!scheduledRunnable.inputSet()) {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (Config.DEBUG) {
                            System.out.println("before wait in ui " + Thread.currentThread().getName());
                        }
                        scheduledRunnable.waitForInput();
                        if (Config.DEBUG) {
                            System.out.println("after wait in ui " + Thread.currentThread().getName());
                        }
                        sHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (Config.DEBUG) {
                                    System.out.println("post ui2 " + Thread.currentThread().getName());
                                }
                                scheduledRunnable.getRunnable().run();
                            }
                        });
                    }
                });
            } else {
                scheduleInternal(scheduledRunnable.getRunnable());
            }
        } else {
            scheduleInternal(runnable);
        }
    }
}
