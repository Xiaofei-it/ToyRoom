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

import java.util.HashMap;

/**
 * Created by Xiaofei on 16/7/17.
 */
public class DoubleKeyMap {
    private HashMap<Object, HashMap<Object, Object>> mMap;
    
    public DoubleKeyMap() {
        mMap = new HashMap<Object, HashMap<Object, Object>>();
    }
    
    public void put(Object key1, Object key2, Object value) {
        HashMap<Object, Object> map = mMap.get(key1);
        if (map == null) {
            map = new HashMap<Object, Object>();
            mMap.put(key1, map);
        }
        map.put(key2, value);
    }
    
    public Object get(Object key1, Object key2) {
        HashMap<Object, Object> map = mMap.get(key1);
        if (map == null) {
            return null;
        } else {
            return map.get(key2);
        }
    }

    public Object remove(Object key1, Object key2) {
        HashMap<Object, Object> map = mMap.get(key1);
        if (map == null) {
            return null;
        } else {
            Object result = map.remove(key2);
            if (map.isEmpty()) {
                mMap.remove(key1);
            }
            return result;
        }
    }
}
