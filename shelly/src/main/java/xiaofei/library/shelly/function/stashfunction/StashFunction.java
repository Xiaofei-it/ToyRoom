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

package xiaofei.library.shelly.function.stashfunction;

import xiaofei.library.shelly.function.Function;
import xiaofei.library.shelly.util.DoubleKeyMap;

/**
 * Created by Xiaofei on 16/7/18.
 */
public abstract class StashFunction implements Function {

    //Initialized by setStash(DoubleKeyMap), otherwise NPE will be thrown.
    private DoubleKeyMap mStash;

    public void setStash(DoubleKeyMap stash) {
        mStash = stash;
    }

    public void stash(Object key, Object value) {
        stash(key, key, value);
    }

    public void stash(Object key1, Object key2, Object value) {
        mStash.put(key1, key2, value);
    }

    public Object get(Object key) {
        return get(key, key);
    }

    public Object get(Object key1, Object key2) {
        return mStash.get(key1, key2);
    }

    public Object remove(Object key) {
        return remove(key, key);
    }

    public Object remove(Object key1, Object key2) {
        return mStash.remove(key1, key2);
    }

    //TODO First refactor scheduler, then add stash in lift!
}
