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

/**
 * Created by Xiaofei on 16/6/20.
 */
public abstract class Task<T, R, S> {

    private TaskListener<R, S> mListener;

    public Task() {
    }

    public void setListener(TaskListener<R, S> listener) {
        mListener = listener;
    }

    protected abstract void onExecute(T input);

    public final void execute(T input) {
        onExecute(input);
    }

    public final void notifySuccess(R result) {
        if (mListener != null) {
            mListener.onSuccess(result);
        }
    }

    public final void notifyFailure(S error) {
        if (mListener != null) {
            mListener.onFailure(error);
        }
    }

    public interface TaskListener<R, S> {
        void onSuccess(R result);
        void onFailure(S error);
    }

}
