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

package xiaofei.library.shelly.operator;

import java.util.concurrent.CopyOnWriteArrayList;

import xiaofei.library.shelly.function.Function1;
import xiaofei.library.shelly.function.Function2;
import xiaofei.library.shelly.util.TargetCenter;

/**
 * Created by Xiaofei on 16/6/23.
 */
public class MapOperator2<T, R, U> implements CopyOnWriteArrayListFunction1<T, R> {

    private static final TargetCenter TARGET_CENTER = TargetCenter.getInstance();

    private Class<U> clazz;

    private Function2<? super U, ? super T, ? extends R> map;

    public MapOperator2(Class<U> clazz, Function2<? super U, ? super T, ? extends R> map) {
        this.clazz = clazz;
        this.map = map;
    }

    @Override
    public CopyOnWriteArrayList<R> call(CopyOnWriteArrayList<T> input) {
        CopyOnWriteArrayList<R> result = new CopyOnWriteArrayList<R>();
        CopyOnWriteArrayList<Object> objects = TARGET_CENTER.getObjects(clazz);
        for (Object o : objects) {
            for (T singleInput : input) {
                result.add(map.call((U) o, singleInput));
            }
        }
        return result;
    }
}
