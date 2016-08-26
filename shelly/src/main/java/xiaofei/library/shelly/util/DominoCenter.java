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

import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import xiaofei.library.shelly.domino.Domino;

/**
 * Created by Xiaofei on 16/5/26.
 */
public class DominoCenter {

    private static final String TAG = "DominoCenter";

    private static volatile DominoCenter sInstance = null;

    private final ConcurrentHashMap<Object, Domino<?, ?>> mDominoes;

    private DominoCenter() {
        mDominoes = new ConcurrentHashMap<Object, Domino<?, ?>>();
    }

    public static DominoCenter getInstance() {
        if (sInstance == null) {
            synchronized (DominoCenter.class) {
                if (sInstance == null) {
                    sInstance = new DominoCenter();
                }
            }
        }
        return sInstance;
    }

    public void commit(Domino<?, ?> domino) {
        Object label = domino.getLabel();
        if (label == null) {
            throw new UnsupportedOperationException("Domino label cannot be null. "
                    + "Please note that Domino created by Shelly.createDomino(Class<T>) can only "
                    + "be used temporarily and cannot be committed.");
        }
        if (mDominoes.put(label, domino) != null) {
            Log.w(TAG, "Domino name duplicate! Check whether you have commit a domino with the same label before.");
        }
    }

    public <T> void play(Object label, CopyOnWriteArrayList<T> input) {
        Domino<T, ?> domino = (Domino<T, ?>) mDominoes.get(label);
        if (domino == null) {
            throw new IllegalStateException("There is no domino labeled '" + label + "'.");
        }
        domino.play(input);
    }

    public <T, R> Domino<T, R> getDomino(Object label) {
        return (Domino<T, R>) mDominoes.get(label);
    }

}
