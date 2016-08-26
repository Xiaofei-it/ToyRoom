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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Xiaofei on 16/6/23.
 */
public class PlayerInputs {

    private final CopyOnWriteArrayList<Object> mInputs;

    private final int mFunctionNumber;

    private final AtomicInteger mFinishedNumber;

    public PlayerInputs(List<Object> list, int functionNumber) {
        if (list instanceof CopyOnWriteArrayList<?>) {
            mInputs = (CopyOnWriteArrayList<Object>) list;
        } else {
            mInputs = new CopyOnWriteArrayList<Object>(list);
        }
        mFunctionNumber = functionNumber;
        mFinishedNumber = new AtomicInteger();
    }

    public PlayerInputs(int functionNumber) {
        mInputs = new CopyOnWriteArrayList<Object>();
        mFunctionNumber = functionNumber;
        mFinishedNumber = new AtomicInteger();
    }

    public AtomicInteger getFinishedNumber() {
        return mFinishedNumber;
    }

    public int getFunctionNumber() {
        return mFunctionNumber;
    }

    public CopyOnWriteArrayList<Object> getInputs() {
        return mInputs;
    }
}
