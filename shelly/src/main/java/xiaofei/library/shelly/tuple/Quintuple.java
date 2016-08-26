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

package xiaofei.library.shelly.tuple;

/**
 * Created by Xiaofei on 16/7/1.
 */
public class Quintuple<T1, T2, T3, T4, T5> {
    public final T1 first;
    public final T2 second;
    public final T3 third;
    public final T4 fourth;
    public final T5 fifth;

    @Deprecated
    public Quintuple(T1 first, T2 second, T3 third, T4 fourth, T5 fifth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
    }

    public static <T1, T2, T3, T4, T5> Quintuple<T1, T2, T3, T4, T5> create(
            T1 first, T2 second, T3 third, T4 fourth, T5 fifth) {
        return new Quintuple<T1, T2, T3, T4, T5>(first, second, third, fourth, fifth);
    }
}
