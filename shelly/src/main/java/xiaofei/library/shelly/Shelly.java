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

package xiaofei.library.shelly;

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

import xiaofei.library.shelly.domino.Domino;
import xiaofei.library.shelly.util.DominoCenter;
import xiaofei.library.shelly.util.TargetCenter;

/**
 * Created by Xiaofei on 16/5/26.
 */
public class Shelly {

    private static final TargetCenter TARGET_CENTER = TargetCenter.getInstance();

    private static final DominoCenter DOMINO_CENTER = DominoCenter.getInstance();

    public static void register(Object object) {
        TARGET_CENTER.register(object);
    }

    public static boolean isRegistered(Object object) {
        return TARGET_CENTER.isRegistered(object);
    }

    public static void unregister(Object object) {
        TARGET_CENTER.unregister(object);
    }

    public static <T> Domino<T, T> createDomino(Object label) {
        return new Domino<T, T>(label);
    }

    public static <T> Domino<T, T> createAnonymousDomino() {
        return createDomino(null);
    }

    @SafeVarargs
    public static <T> void playDomino(Object label, T... input) {
        CopyOnWriteArrayList<T> newInput = new CopyOnWriteArrayList<T>();
        if (input == null) {
            newInput.add(null);
        } else if (input.length > 0) {
            newInput.addAll(Arrays.asList(input));
        }
        playDominoInternal(label, newInput);
    }

    public static void playDomino(Object label) {
        playDominoInternal(label, new CopyOnWriteArrayList<Object>());
    }

    private static <T> void playDominoInternal(Object label, CopyOnWriteArrayList<T> input) {
        DOMINO_CENTER.play(label, input);
    }

    public static <T, R> Domino<T, R> getDominoByLabel(Object label) {
        return DOMINO_CENTER.getDomino(label);
    }
}
