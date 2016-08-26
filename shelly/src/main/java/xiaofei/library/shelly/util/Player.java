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

package xiaofei.library.shelly.util;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import xiaofei.library.concurrentutils.AugmentedListCanary;
import xiaofei.library.concurrentutils.util.Action;
import xiaofei.library.concurrentutils.util.Condition;
import xiaofei.library.shelly.function.Function;
import xiaofei.library.shelly.function.Function1;
import xiaofei.library.shelly.function.stashfunction.StashFunction;
import xiaofei.library.shelly.runnable.BlockingRunnable;
import xiaofei.library.shelly.runnable.ScheduledRunnable;
import xiaofei.library.shelly.scheduler.DefaultScheduler;
import xiaofei.library.shelly.scheduler.Scheduler;

/**
 * Created by Xiaofei on 16/5/31.
 *
 * Not thread-safe!!!
 */
public class Player<T> {

    private final AugmentedListCanary<PlayerInputs> mInputs;

    private final DoubleKeyMap mStash;

    private Scheduler mScheduler;

    public Player(List<T> input) {
        mInputs = new AugmentedListCanary<PlayerInputs>();
        mInputs.add(new PlayerInputs((List<Object>) input, 0));
        mStash = new DoubleKeyMap();
        mScheduler = new DefaultScheduler();
    }

    public void setScheduler(Scheduler scheduler) {
        mScheduler = scheduler;
    }

    public Scheduler getScheduler() {
        return mScheduler;
    }

    public void prepare(Function function) {
        if (function instanceof StashFunction) {
            ((StashFunction) function).setStash(mStash);
        }
    }

    protected Runnable getRunnable(final Tile<T, ?> tile) {
        return new Runnable() {
            private int mIndex = mInputs.size() - 1;
            @Override
            public void run() {
                tile.call((CopyOnWriteArrayList<T>) mInputs.get(mIndex).getInputs());
            }
        };
    }

    //This method is not thread-safe! But we always call this in a single thread.
    public final <R> Player<R> playRunnable(List<? extends Runnable> runnables) {
        synchronized (this) {
            if (mScheduler.isRunning()) {
                int size = mInputs.size();
                for (Runnable runnable : runnables) {
                    mScheduler.call(new ScheduledRunnable<T>(this, runnable, size));
                }
            }
            return (Player<R>) this;
        }
    }

    public final <R> Player<R> playFunction(List<? extends Function1<CopyOnWriteArrayList<T>, CopyOnWriteArrayList<R>>> functions) {
        synchronized (this) {
            if (mScheduler.isRunning()) {
                int index = mInputs.add(new PlayerInputs(functions.size())) - 1;
                for (Function1<CopyOnWriteArrayList<T>, CopyOnWriteArrayList<R>> function : functions) {
                    mScheduler.call(new ScheduledRunnable<T>(this, new BlockingRunnable<T, R>(this, function, index), index));
                }
            }
            return (Player<R>) this;
        }
    }

    //This method is not thread-safe! But we always call this in a single thread.
    public final void play(Tile<T, ?> tile) {
        synchronized (this) {
            if (mScheduler.isRunning()) {
                playRunnable(Collections.singletonList(getRunnable(tile)));
            }
        }
    }

    public void appendAt(int index, final CopyOnWriteArrayList<Object> object) {
        mInputs.action(index, new Action<PlayerInputs>() {
            @Override
            public void call(PlayerInputs o) {
                o.getInputs().addAll(object);
                o.getFinishedNumber().getAndIncrement();
            }
        });
    }

    public final CopyOnWriteArrayList<Object> waitForFinishing() {
        int index = mInputs.size() - 1;
        return mInputs.get(index, new Condition<PlayerInputs>() {
            @Override
            public boolean satisfy(PlayerInputs o) {
                return o.getFinishedNumber().get() == o.getFunctionNumber();
            }
        }).getInputs();
    }

    public CopyOnWriteArrayList<Object> getInput(int index) {
        return mInputs.get(index).getInputs();
    }

    public AugmentedListCanary<PlayerInputs> getInputs() {
        return mInputs;
    }

}
