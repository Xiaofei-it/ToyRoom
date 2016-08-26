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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Xiaofei on 16/5/26.
 */
public class TargetCenter {

    private static volatile TargetCenter sInstance = null;

    private final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Object>> mObjects;

    private TargetCenter() {
        mObjects = new ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Object>>();
    }

    public static TargetCenter getInstance() {
        if (sInstance == null) {
            synchronized (TargetCenter.class) {
                if (sInstance == null) {
                    sInstance = new TargetCenter();
                }
            }
        }
        return sInstance;
    }

    public void register(Object object) {
        synchronized (mObjects) {
            for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
                CopyOnWriteArrayList<Object> objects = mObjects.get(clazz);
                if (objects == null) {
                    mObjects.putIfAbsent(clazz, new CopyOnWriteArrayList<Object>());
                    objects = mObjects.get(clazz);
                }
                objects.add(object);
            }
        }
    }

    public void unregister(Object object) {
        synchronized (mObjects) {
            for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
                CopyOnWriteArrayList<Object> objects = mObjects.get(clazz);
                if (objects == null) {
                    return;
                }
                int size = objects.size();
                for (int i = 0; i < size; ++i) {
                    if (objects.get(i) == object) {
                        objects.remove(i);
                        --i;
                        --size;
                    }
                }
                if (objects.isEmpty()) {
                    mObjects.remove(clazz);
                }
            }
        }
    }

    public boolean isRegistered(Object object) {
        Class<?> clazz = object.getClass();
        CopyOnWriteArrayList<Object> objects = mObjects.get(clazz);
        return objects != null && objects.contains(object);
    }

    public CopyOnWriteArrayList<Object> getObjects(Class<?> clazz) {
        return mObjects.get(clazz);
    }
}
