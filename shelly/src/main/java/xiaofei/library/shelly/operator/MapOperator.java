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

/**
 * Created by Xiaofei on 16/6/23.
 */
public class MapOperator<T, R> implements CopyOnWriteArrayListFunction1<T, R> {

    private Function1<? super T, ? extends R> map;

    public MapOperator(Function1<? super T, ? extends R> map) {
        this.map = map;
    }

    @Override
    public CopyOnWriteArrayList<R> call(CopyOnWriteArrayList<T> input) {
        CopyOnWriteArrayList<R> result = new CopyOnWriteArrayList<R>();
        for (T singleInput : input) {
            result.add(map.call(singleInput));
        }
        return result;
    }
}
