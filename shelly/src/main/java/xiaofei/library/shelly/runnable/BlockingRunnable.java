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

import java.util.concurrent.CopyOnWriteArrayList;

import xiaofei.library.shelly.function.Function1;
import xiaofei.library.shelly.util.Player;

/**
 * Created by Xiaofei on 16/6/23.
 */
public class BlockingRunnable<T, R> implements Runnable {

    private Player<T> mPlayer;

    private int mIndex;

    private Function1<CopyOnWriteArrayList<T>, CopyOnWriteArrayList<R>> mFunction;

    public BlockingRunnable(Player<T> player, Function1<CopyOnWriteArrayList<T>, CopyOnWriteArrayList<R>> function, int index) {
        mPlayer = player;
        mFunction = function;
        mIndex = index;
    }

    protected final CopyOnWriteArrayList<T> getPreviousInput() {
        return (CopyOnWriteArrayList<T>) mPlayer.getInput(mIndex - 1);
    }

    @Override
    public final void run() {
        CopyOnWriteArrayList<R> input = mFunction.call(getPreviousInput());
        if (input == null) {
            throw new IllegalStateException();
        }
        mPlayer.appendAt(mIndex, (CopyOnWriteArrayList<Object>) input);
    }
}
