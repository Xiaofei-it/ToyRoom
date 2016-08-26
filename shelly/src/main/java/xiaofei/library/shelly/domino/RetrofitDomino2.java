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

package xiaofei.library.shelly.domino;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import xiaofei.library.shelly.Shelly;
import xiaofei.library.shelly.function.Action0;
import xiaofei.library.shelly.function.Action1;
import xiaofei.library.shelly.function.Action2;
import xiaofei.library.shelly.function.Function1;
import xiaofei.library.shelly.function.TargetAction0;
import xiaofei.library.shelly.function.TargetAction1;
import xiaofei.library.shelly.function.TargetAction2;
import xiaofei.library.shelly.operator.ListIdentityOperator;
import xiaofei.library.shelly.tuple.Pair;
import xiaofei.library.shelly.tuple.Triple;
import xiaofei.library.shelly.util.Tile;

/**
 * Created by Xiaofei on 16/6/28.
 */
public class RetrofitDomino2<T, R, S> extends Domino<T, Triple<Boolean, Pair<R, Response<S>>, Throwable>> {

    public RetrofitDomino2(Object label, Tile<T, Triple<Boolean, Pair<R, Response<S>>, Throwable>> tile) {
        super(label, tile);
    }

    private RetrofitDomino2(Domino<T, Triple<Boolean, Pair<R, Response<S>>, Throwable>> domino) {
        //can use getPlayer(), but not here!
        this(domino.getLabel(), domino.getPlayer());
    }
    @Override
    public RetrofitDomino2<T, R, S> uiThread() {
        return new RetrofitDomino2<T, R, S>(super.uiThread());
    }

    @Override
    public RetrofitDomino2<T, R, S> background() {
        return new RetrofitDomino2<T, R, S>(super.background());
    }

    @Override
    public RetrofitDomino2<T, R, S> backgroundQueue() {
        return new RetrofitDomino2<T, R, S>(super.backgroundQueue());
    }


    public RetrofitDomino2<T, R, S> onFailure(final Domino<? super Throwable, ?> domino) {
        return new RetrofitDomino2<T, R, S>(
                perform(Shelly.<Triple<Boolean, Pair<R, Response<S>>, Throwable>>createAnonymousDomino()
                        .reduce(new Function1<List<Triple<Boolean, Pair<R, Response<S>>, Throwable>>, List<Throwable>>() {
                            @Override
                            public List<Throwable> call(List<Triple<Boolean, Pair<R, Response<S>>, Throwable>> input) {
                                List<Throwable> result= new ArrayList<Throwable>();
                                for (Triple<Boolean, Pair<R, Response<S>>, Throwable> triple : input) {
                                    if (!triple.first) {
                                        result.add(triple.third);
                                    }
                                }
                                return result;
                            }
                        })
                        .flatMap(new ListIdentityOperator<Throwable>())
                        .perform(domino)
                ));
    }

    public <U> RetrofitDomino2<T, R, S> onFailure(final Class<? extends U> target, final TargetAction0<? super U> targetAction0) {
        return new RetrofitDomino2<T, R, S>(
                reduce(new ListIdentityOperator<Triple<Boolean, Pair<R, Response<S>>, Throwable>>())
                .perform(target, new TargetAction1<U, List<Triple<Boolean, Pair<R, Response<S>>, Throwable>>>() {
                    @Override
                    public void call(U s, List<Triple<Boolean, Pair<R, Response<S>>, Throwable>> input) {
                        boolean failure = false;
                        for (Triple<Boolean, Pair<R, Response<S>>, Throwable> triple : input) {
                            if (!triple.first) {
                                failure = true;
                                break;
                            }
                        }
                        if (failure) {
                            targetAction0.call(s);
                        }
                    }
                }).flatMap(new ListIdentityOperator<Triple<Boolean, Pair<R, Response<S>>, Throwable>>()));
    }

    public <U> RetrofitDomino2<T, R, S> onFailure(final Class<? extends U> target, final TargetAction1<? super U, ? super Throwable> targetAction1) {
        return new RetrofitDomino2<T, R, S>(perform(target, new TargetAction1<U, Triple<Boolean, Pair<R, Response<S>>, Throwable>>() {
            @Override
            public void call(U s, Triple<Boolean, Pair<R, Response<S>>, Throwable> input) {
                if (!input.first) {
                    targetAction1.call(s, input.third);
                }
            }
        }));
    }

    public RetrofitDomino2<T, R, S> onFailure(final Action0 action0) {
        return new RetrofitDomino2<T, R, S>(
                reduce(new ListIdentityOperator<Triple<Boolean, Pair<R, Response<S>>, Throwable>>())
                .perform(new Action1<List<Triple<Boolean, Pair<R, Response<S>>, Throwable>>>() {
                    @Override
                    public void call(List<Triple<Boolean, Pair<R, Response<S>>, Throwable>> input) {
                        boolean failure = false;
                        for (Triple<Boolean, Pair<R, Response<S>>, Throwable> triple : input) {
                            if (!triple.first) {
                                failure = true;
                                break;
                            }
                        }
                        if (failure) {
                            action0.call();
                        }
                    }
                }).flatMap(new ListIdentityOperator<Triple<Boolean, Pair<R, Response<S>>, Throwable>>()));
    }

    public RetrofitDomino2<T, R, S> onFailure(final Action1<? super Throwable> action1) {
        return new RetrofitDomino2<T, R, S>(perform(new Action1<Triple<Boolean, Pair<R, Response<S>>, Throwable>>() {
            @Override
            public void call(Triple<Boolean, Pair<R, Response<S>>, Throwable> input) {
                if (!input.first) {
                    action1.call(input.third);
                }
            }
        }));
    }

    public RetrofitDomino2<T, R, S> finallyDo(Action0 action0) {
        return new RetrofitDomino2<T, R, S>(perform(action0));
    }

    public <U> RetrofitDomino2<T, R, S> finallyDo(Class<? extends U> target, TargetAction0<? super U> targetAction0) {
        return new RetrofitDomino2<T, R, S>(perform(target, targetAction0));
    }

    private Domino<T, Triple<Boolean, Pair<R, Response<S>>, Throwable>> toDomino() {
        return new Domino<T, Triple<Boolean, Pair<R, Response<S>>, Throwable>>(getLabel(), getPlayer());
    }

    public Domino<T, S> endTask() {
        return endTask(new Function1<List<Triple<Boolean, Pair<R, Response<S>>, Throwable>>, List<S>>() {
            @Override
            public List<S> call(List<Triple<Boolean, Pair<R, Response<S>>, Throwable>> input) {
                List<S> result = new ArrayList<S>();
                for (Triple<Boolean, Pair<R, Response<S>>, Throwable> triple : input) {
                    if (responseSuccess(triple)) {
                        result.add(triple.second.second.body());
                    }
                }
                return result;
            }
        });
    }

    public <U> Domino<T, U> endTaskEmpty() {
        return toDomino().clear();
    }

    public <U> Domino<T, U> endTask(Function1<List<Triple<Boolean, Pair<R, Response<S>>, Throwable>>, List<U>> reducer) {
        return toDomino().reduce(reducer).flatMap(new ListIdentityOperator<U>());
    }

    private static <T, R, S> boolean responseSuccess(Triple<Boolean, Pair<R, Response<S>>, Throwable> triple) {
        return triple.first && triple.second.second.isSuccessful() && triple.second.second.body() != null;
    }

    public RetrofitDomino2<T, R, S> onSuccessResult(final Action0 action0) {
        return new RetrofitDomino2<T, R, S>(
                reduce(new ListIdentityOperator<Triple<Boolean, Pair<R, Response<S>>, Throwable>>())
                .perform(new Action1<List<Triple<Boolean, Pair<R, Response<S>>, Throwable>>>() {
                    @Override
                    public void call(List<Triple<Boolean, Pair<R, Response<S>>, Throwable>> input) {
                        boolean hasResult = false;
                        for (Triple<Boolean, Pair<R, Response<S>>, Throwable> triple : input) {
                            if (responseSuccess(triple)) {
                                hasResult = true;
                                break;
                            }
                        }
                        if (hasResult) {
                            action0.call();
                        }
                    }
                }).flatMap(new ListIdentityOperator<Triple<Boolean, Pair<R, Response<S>>, Throwable>>()));
    }

    public RetrofitDomino2<T, R, S> onSuccessResult(final Action2<R, S> action2) {
        return new RetrofitDomino2<T, R, S>(
                perform(new Action1<Triple<Boolean, Pair<R, Response<S>>, Throwable>>() {
                    @Override
                    public void call(Triple<Boolean, Pair<R, Response<S>>, Throwable> input) {
                        if (responseSuccess(input)) {
                            action2.call(input.second.first, input.second.second.body());
                        }
                    }
                })
        );
    }

    public <U> RetrofitDomino2<T, R, S> onSuccessResult(final Class<? extends U> target, final TargetAction0<? super U> targetAction0) {
        return new RetrofitDomino2<T, R, S>(
                reduce(new ListIdentityOperator<Triple<Boolean, Pair<R, Response<S>>, Throwable>>())
                .perform(target, new TargetAction1<U, List<Triple<Boolean, Pair<R, Response<S>>, Throwable>>>() {
                    @Override
                    public void call(U u, List<Triple<Boolean, Pair<R, Response<S>>, Throwable>> input) {
                        boolean success = false;
                        for (Triple<Boolean, Pair<R, Response<S>>, Throwable> triple : input) {
                            if (responseSuccess(triple)) {
                                success = true;
                                break;
                            }
                        }
                        if (success) {
                            targetAction0.call(u);
                        }
                    }
                }).flatMap(new ListIdentityOperator<Triple<Boolean, Pair<R, Response<S>>, Throwable>>()));
    }

    public <U> RetrofitDomino2<T, R, S> onSuccessResult(final Class<? extends U> target, final TargetAction2<? super U, R, S> targetAction2) {
        return new RetrofitDomino2<T, R, S>(
                perform(target, new TargetAction1<U, Triple<Boolean, Pair<R, Response<S>>, Throwable>>() {
                    @Override
                    public void call(U u, Triple<Boolean, Pair<R, Response<S>>, Throwable> input) {
                        if (responseSuccess(input)) {
                            targetAction2.call(u, input.second.first, input.second.second.body());
                        }
                    }
                })
        );
    }

    public RetrofitDomino2<T, R, S> onSuccessResult(final Domino<Pair<R, Response<S>>, ?> domino) {
        return new RetrofitDomino2<T, R, S>(
                perform(Shelly.<Triple<Boolean, Pair<R, Response<S>>, Throwable>>createAnonymousDomino()
                        .reduce(new Function1<List<Triple<Boolean, Pair<R, Response<S>>, Throwable>>, List<Pair<R, Response<S>>>>() {
                            @Override
                            public List<Pair<R, Response<S>>> call(List<Triple<Boolean, Pair<R, Response<S>>, Throwable>> input) {
                                List<Pair<R, Response<S>>> result= new ArrayList<Pair<R, Response<S>>>();
                                for (Triple<Boolean, Pair<R, Response<S>>, Throwable> triple : input) {
                                    if (responseSuccess(triple)) {
                                        result.add(triple.second);
                                    }
                                }
                                return result;
                            }
                        })
                        .flatMap(new ListIdentityOperator<Pair<R, Response<S>>>())
                        .perform(domino)
                ));
    }

    private static <T, R> boolean responseFailure(Triple<Boolean, Pair<T, Response<R>>, Throwable> triple) {
        return triple.first && (
                triple.second.second.isSuccessful() && triple.second.second.body() == null ||
                        !triple.second.second.isSuccessful());
    }

    public RetrofitDomino2<T, R, S> onResponseFailure(final Action0 action0) {
        return new RetrofitDomino2<T, R, S>(
                reduce(new ListIdentityOperator<Triple<Boolean, Pair<R, Response<S>>, Throwable>>())
                .perform(new Action1<List<Triple<Boolean, Pair<R, Response<S>>, Throwable>>>() {
                    @Override
                    public void call(List<Triple<Boolean, Pair<R, Response<S>>, Throwable>> input) {
                        boolean failure = false;
                        for (Triple<Boolean, Pair<R, Response<S>>, Throwable> triple : input) {
                            if (responseFailure(triple)) {
                                failure = true;
                                break;
                            }
                        }
                        if (failure) {
                            action0.call();
                        }
                    }
                }).flatMap(new ListIdentityOperator<Triple<Boolean, Pair<R, Response<S>>, Throwable>>()));
    }

    public RetrofitDomino2<T, R, S> onResponseFailure(final Action2<R, Response<S>> action2) {
        return new RetrofitDomino2<T, R, S>(
                perform(new Action1<Triple<Boolean, Pair<R, Response<S>>, Throwable>>() {
                    @Override
                    public void call(Triple<Boolean, Pair<R, Response<S>>, Throwable> input) {
                        if (responseFailure(input)) {
                            action2.call(input.second.first, input.second.second);
                        }
                    }
                })
        );
    }

    public <U> RetrofitDomino2<T, R, S> onResponseFailure(final Class<? extends U> target, final TargetAction0<? super U> targetAction0) {
        return new RetrofitDomino2<T, R, S>(
                reduce(new ListIdentityOperator<Triple<Boolean, Pair<R, Response<S>>, Throwable>>())
                .perform(target, new TargetAction1<U, List<Triple<Boolean, Pair<R, Response<S>>, Throwable>>>() {
                    @Override
                    public void call(U u, List<Triple<Boolean, Pair<R, Response<S>>, Throwable>> input) {
                        boolean failure = false;
                        for (Triple<Boolean, Pair<R, Response<S>>, Throwable> triple : input) {
                            if (responseFailure(triple)) {
                                failure = true;
                                break;
                            }
                        }
                        if (failure) {
                            targetAction0.call(u);
                        }
                    }
                }).flatMap(new ListIdentityOperator<Triple<Boolean, Pair<R, Response<S>>, Throwable>>()));
    }

    public <U> RetrofitDomino2<T, R, S> onResponseFailure(final Class<? extends U> target, final TargetAction2<? super U, R, Response<S>> targetAction2) {
        return new RetrofitDomino2<T, R, S>(
                perform(target, new TargetAction1<U, Triple<Boolean, Pair<R, Response<S>>, Throwable>>() {
                    @Override
                    public void call(U u, Triple<Boolean, Pair<R, Response<S>>, Throwable> input) {
                        if (responseFailure(input)) {
                            targetAction2.call(u, input.second.first, input.second.second);
                        }
                    }
                })
        );
    }

    public RetrofitDomino2<T, R, S> onResponseFailure(final Domino<Pair<R, Response<S>>, ?> domino) {
        return new RetrofitDomino2<T, R, S>(
                perform(Shelly.<Triple<Boolean, Pair<R, Response<S>>, Throwable>>createAnonymousDomino()
                        .reduce(new Function1<List<Triple<Boolean, Pair<R, Response<S>>, Throwable>>, List<Pair<R, Response<S>>>>() {
                            @Override
                            public List<Pair<R, Response<S>>> call(List<Triple<Boolean, Pair<R, Response<S>>, Throwable>> input) {
                                List<Pair<R, Response<S>>> result= new ArrayList<Pair<R, Response<S>>>();
                                for (Triple<Boolean, Pair<R, Response<S>>, Throwable> triple : input) {
                                    if (responseFailure(triple)) {
                                        result.add(triple.second);
                                    }
                                }
                                return result;
                            }
                        })
                        .flatMap(new ListIdentityOperator<Pair<R, Response<S>>>())
                        .perform(domino)
                ));
    }
}
