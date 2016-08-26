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

import xiaofei.library.concurrentutils.ObjectCanary;
import xiaofei.library.concurrentutils.util.Action;
import xiaofei.library.concurrentutils.util.Condition;
import xiaofei.library.concurrentutils.util.Function;
import xiaofei.library.shelly.function.Function1;
import xiaofei.library.shelly.function.Function2;
import xiaofei.library.shelly.task.Task;
import xiaofei.library.shelly.tuple.Triple;


/**
 * Created by Xiaofei on 16/6/30.
 */
public class TaskFunction<T, R1, R2, U1, U2>
        implements Function1<T, Triple<Boolean, R2, U2>>, Task.TaskListener<R1, U1> {

    private Task<T, R1, U1> mTask;

    private Function2<T, R1, R2> mFunc1;

    private Function2<T, U1, U2> mFunc2;

    private T mInput;

    private volatile ObjectCanary<ResultWrapper<R2, U2>> mResultWrapper;

    public TaskFunction(Task<T, R1, U1> task, Function2<T, R1, R2> func1, Function2<T, U1, U2> func2) {
        task.setListener(this);
        mTask = task;
        mFunc1 = func1;
        mFunc2 = func2;
        mResultWrapper = new ObjectCanary<ResultWrapper<R2, U2>>();
    }

    @Override
    public void onFailure(final U1 error) {
        mResultWrapper.action(new Action<ResultWrapper<R2, U2>>() {
            @Override
            public void call(ResultWrapper<R2, U2> o) {
                o.setError(mFunc2.call(mInput, error));
            }
        });
    }

    @Override
    public void onSuccess(final R1 result) {
        mResultWrapper.action(new Action<ResultWrapper<R2, U2>>() {
            @Override
            public void call(ResultWrapper<R2, U2> o) {
                o.setResult(mFunc1.call(mInput, result));
            }
        });
    }

    @Override
    public Triple<Boolean, R2, U2> call(T input) {
        mInput = input;
        mResultWrapper.set(new ResultWrapper<R2, U2>());
        mTask.execute(input);
        mResultWrapper.wait(new Condition<ResultWrapper<R2, U2>>() {
            @Override
            public boolean satisfy(ResultWrapper<R2, U2> o) {
                return o.getFlag() != -1;
            }
        });
        return Triple.create(
                mResultWrapper.calculate(new Function<ResultWrapper<R2, U2>, Boolean>() {
                    @Override
                    public Boolean call(ResultWrapper<R2, U2> o) {
                        return o.getFlag() == 1;
                    }
                }),
                mResultWrapper.calculate(new Function<ResultWrapper<R2, U2>, R2>() {
                    @Override
                    public R2 call(ResultWrapper<R2, U2> o) {
                        return o.getResult();
                    }
                }),
                mResultWrapper.calculate(new Function<ResultWrapper<R2, U2>, U2>() {
                    @Override
                    public U2 call(ResultWrapper<R2, U2> o) {
                        return o.getError();
                    }
                }));
    }

    private static class ResultWrapper<T, R> {
        private volatile int mFlag;
        private volatile T mResult;
        private volatile R mError;

        ResultWrapper() {
            mFlag = -1;
            mResult = null;
            mError = null;
        }

        public void setError(R error) {
            mError = error;
            mFlag = 0;
        }

        public void setResult(T result) {
            mResult = result;
            mFlag = 1;
        }

        public R getError() {
            return mError;
        }

        public int getFlag() {
            return mFlag;
        }

        public T getResult() {
            return mResult;
        }
    }
}
