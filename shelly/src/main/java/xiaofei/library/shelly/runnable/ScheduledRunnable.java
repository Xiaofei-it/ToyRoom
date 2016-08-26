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

package xiaofei.library.shelly.runnable;

import xiaofei.library.concurrentutils.util.Condition;
import xiaofei.library.shelly.util.Player;
import xiaofei.library.shelly.util.PlayerInputs;

/**
 * Created by Xiaofei on 16/6/23.
 */
public class ScheduledRunnable<R> implements Runnable {

    private Player<R> mPlayer;

    private Runnable mRunnable;

    private int mWaiting;

    public ScheduledRunnable(Player<R> player, Runnable runnable, int waiting) {
        mPlayer = player;
        mRunnable = runnable;
        mWaiting = waiting;
    }

    public Runnable getRunnable() {
        return mRunnable;
    }

    public void waitForInput() {
        int waitingIndex = mWaiting - 1;
        mPlayer.getInputs().wait(waitingIndex, new Condition<PlayerInputs>() {
            @Override
            public boolean satisfy(PlayerInputs o) {
                return o.getFinishedNumber().get() == o.getFunctionNumber();
            }
        });
    }

    public boolean inputSet() {
        return mPlayer.getInputs().satisfy(mWaiting - 1, new Condition<PlayerInputs>() {
            @Override
            public boolean satisfy(PlayerInputs o) {
                return o.getFinishedNumber().get() == o.getFunctionNumber();
            }
        });
    }

    @Override
    public void run() {
        waitForInput();
        mRunnable.run();
    }
}
