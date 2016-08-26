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

package xiaofei.library.shelly.task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Xiaofei on 16/6/27.
 */
public abstract class AbstractRetrofitTask<T, R> extends Task<T, Response<R>, Throwable> {

    protected abstract Call<R> getCall(T t);

    protected Callback<R> getCallback() {
        return new Callback<R>() {
            @Override
            public void onResponse(Call<R> call, Response<R> response) {
                notifySuccess(response);
            }

            @Override
            public void onFailure(Call<R> call, Throwable t) {
                notifyFailure(t);
            }
        };
    }

    protected abstract void call(Call<R> call);

    @Override
    protected void onExecute(T input) {
        call(getCall(input));
    }
}
