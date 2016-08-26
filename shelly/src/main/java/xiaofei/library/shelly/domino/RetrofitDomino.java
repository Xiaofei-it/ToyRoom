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
import xiaofei.library.shelly.function.Function1;
import xiaofei.library.shelly.function.TargetAction0;
import xiaofei.library.shelly.function.TargetAction1;
import xiaofei.library.shelly.operator.ListIdentityOperator;
import xiaofei.library.shelly.tuple.Triple;
import xiaofei.library.shelly.util.Tile;

/**
 * Created by Xiaofei on 16/6/27.
 */
public class RetrofitDomino<T, R> extends Domino<T, Triple<Boolean, Response<R>, Throwable>> {

    public RetrofitDomino(Object label, Tile<T, Triple<Boolean, Response<R>, Throwable>> tile) {
        super(label, tile);
    }

    private RetrofitDomino(Domino<T, Triple<Boolean, Response<R>, Throwable>> domino) {
        //can use getPlayer(), but not here!
        this(domino.getLabel(), domino.getPlayer());
    }
    @Override
    public RetrofitDomino<T, R> uiThread() {
        return new RetrofitDomino<T, R>(super.uiThread());
    }

    @Override
    public RetrofitDomino<T, R> background() {
        return new RetrofitDomino<T, R>(super.background());
    }

    @Override
    public RetrofitDomino<T, R> backgroundQueue() {
        return new RetrofitDomino<T, R>(super.backgroundQueue());
    }

    public RetrofitDomino<T, R> onFailure(final Domino<? super Throwable, ?> domino) {
        return new RetrofitDomino<T, R>(
                perform(Shelly.<Triple<Boolean, Response<R>, Throwable>>createAnonymousDomino()
                        .reduce(new Function1<List<Triple<Boolean, Response<R>, Throwable>>, List<Throwable>>() {
                            @Override
                            public List<Throwable> call(List<Triple<Boolean, Response<R>, Throwable>> input) {
                                List<Throwable> result= new ArrayList<Throwable>();
                                for (Triple<Boolean, Response<R>, Throwable> triple : input) {
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

    public <S> RetrofitDomino<T, R> onFailure(final Class<? extends S> target, final TargetAction0<? super S> targetAction0) {
        return new RetrofitDomino<T, R>(
                reduce(new ListIdentityOperator<Triple<Boolean, Response<R>, Throwable>>())
                .perform(target, new TargetAction1<S, List<Triple<Boolean, Response<R>, Throwable>>>() {
                    @Override
                    public void call(S s, List<Triple<Boolean, Response<R>, Throwable>> input) {
                        boolean failure = false;
                        for (Triple<Boolean, Response<R>, Throwable> triple : input) {
                            if (!triple.first) {
                                failure = true;
                                break;
                            }
                        }
                        if (failure) {
                            targetAction0.call(s);
                        }
                    }
                }).flatMap(new ListIdentityOperator<Triple<Boolean, Response<R>, Throwable>>()));
    }

    public <S> RetrofitDomino<T, R> onFailure(final Class<? extends S> target, final TargetAction1<? super S, ? super Throwable> targetAction1) {
        return new RetrofitDomino<T, R>(perform(target, new TargetAction1<S, Triple<Boolean, Response<R>, Throwable>>() {
            @Override
            public void call(S s, Triple<Boolean, Response<R>, Throwable> input) {
                if (!input.first) {
                    targetAction1.call(s, input.third);
                }
            }
        }));
    }

    public RetrofitDomino<T, R> onFailure(final Action0 action0) {
        return new RetrofitDomino<T, R>(
                reduce(new ListIdentityOperator<Triple<Boolean, Response<R>, Throwable>>())
                .perform(new Action1<List<Triple<Boolean, Response<R>, Throwable>>>() {
                    @Override
                    public void call(List<Triple<Boolean, Response<R>, Throwable>> input) {
                        boolean failure = false;
                        for (Triple<Boolean, Response<R>, Throwable> triple : input) {
                            if (!triple.first) {
                                failure = true;
                                break;
                            }
                        }
                        if (failure) {
                            action0.call();
                        }
                    }
                }).flatMap(new ListIdentityOperator<Triple<Boolean, Response<R>, Throwable>>()));
    }

    public RetrofitDomino<T, R> onFailure(final Action1<? super Throwable> action1) {
        return new RetrofitDomino<T, R>(perform(new Action1<Triple<Boolean, Response<R>, Throwable>>() {
            @Override
            public void call(Triple<Boolean, Response<R>, Throwable> input) {
                if (!input.first) {
                    action1.call(input.third);
                }
            }
        }));
    }

    public RetrofitDomino<T, R> finallyDo(Action0 action0) {
        return new RetrofitDomino<T, R>(perform(action0));
    }

    public <S> RetrofitDomino<T, R> finallyDo(Class<? extends S> target, TargetAction0<? super S> targetAction0) {
        return new RetrofitDomino<T, R>(perform(target, targetAction0));
    }

    private Domino<T, Triple<Boolean, Response<R>, Throwable>> toDomino() {
        return new Domino<T, Triple<Boolean, Response<R>, Throwable>>(getLabel(), getPlayer());
    }

    public Domino<T, R> endTask() {
        return endTask(new Function1<List<Triple<Boolean, Response<R>, Throwable>>, List<R>>() {
            @Override
            public List<R> call(List<Triple<Boolean, Response<R>, Throwable>> input) {
                List<R> result = new ArrayList<R>();
                for (Triple<Boolean, Response<R>, Throwable> triple : input) {
                    if (responseSuccess(triple)) {
                        result.add(triple.second.body());
                    }
                }
                return result;
            }
        });
    }

    public <S> Domino<T, S> endTaskEmpty() {
        return toDomino().clear();
    }

    public <S> Domino<T, S> endTask(Function1<List<Triple<Boolean, Response<R>, Throwable>>, List<S>> reducer) {
        return toDomino().reduce(reducer).flatMap(new ListIdentityOperator<S>());
    }

    private static <R> boolean responseSuccess(Triple<Boolean, Response<R>, Throwable> triple) {
        return triple.first && triple.second.isSuccessful() && triple.second.body() != null;
    }

    public RetrofitDomino<T, R> onSuccessResult(final Action0 action0) {
        return new RetrofitDomino<T, R>(
                reduce(new ListIdentityOperator<Triple<Boolean, Response<R>, Throwable>>())
                .perform(new Action1<List<Triple<Boolean, Response<R>, Throwable>>>() {
                    @Override
                    public void call(List<Triple<Boolean, Response<R>, Throwable>> input) {
                        boolean hasResult = false;
                        for (Triple<Boolean, Response<R>, Throwable> triple : input) {
                            if (responseSuccess(triple)) {
                                hasResult = true;
                                break;
                            }
                        }
                        if (hasResult) {
                            action0.call();
                        }
                    }
                })
                .flatMap(new ListIdentityOperator<Triple<Boolean, Response<R>, Throwable>>()));
    }

    public RetrofitDomino<T, R> onSuccessResult(final Action1<R> action1) {
        return new RetrofitDomino<T, R>(
                perform(new Action1<Triple<Boolean, Response<R>, Throwable>>() {
                    @Override
                    public void call(Triple<Boolean, Response<R>, Throwable> input) {
                        if (responseSuccess(input)) {
                            action1.call(input.second.body());
                        }
                    }
                })
        );
    }

    public <S> RetrofitDomino<T, R> onSuccessResult(final Class<? extends S> target, final TargetAction0<? super S> targetAction0) {
        return new RetrofitDomino<T, R>(
                reduce(new ListIdentityOperator<Triple<Boolean, Response<R>, Throwable>>())
                .perform(target, new TargetAction1<S, List<Triple<Boolean, Response<R>, Throwable>>>() {
                    @Override
                    public void call(S s, List<Triple<Boolean, Response<R>, Throwable>> input) {
                        boolean success = false;
                        for (Triple<Boolean, Response<R>, Throwable> triple : input) {
                            if (responseSuccess(triple)) {
                                success = true;
                                break;
                            }
                        }
                        if (success) {
                            targetAction0.call(s);
                        }
                    }
                })
                .flatMap(new ListIdentityOperator<Triple<Boolean, Response<R>, Throwable>>()));
    }

    public <S> RetrofitDomino<T, R> onSuccessResult(final Class<? extends S> target, final TargetAction1<? super S, R> targetAction1) {
        return new RetrofitDomino<T, R>(
                perform(target, new TargetAction1<S, Triple<Boolean, Response<R>, Throwable>>() {
                    @Override
                    public void call(S s, Triple<Boolean, Response<R>, Throwable> input) {
                        if (responseSuccess(input)) {
                            targetAction1.call(s, input.second.body());
                        }
                    }
                })
        );
    }

    public RetrofitDomino<T, R> onSuccessResult(final Domino<R, ?> domino) {
        return new RetrofitDomino<T, R>(
                perform(Shelly.<Triple<Boolean, Response<R>, Throwable>>createAnonymousDomino()
                        .reduce(new Function1<List<Triple<Boolean, Response<R>, Throwable>>, List<R>>() {
                            @Override
                            public List<R> call(List<Triple<Boolean, Response<R>, Throwable>> input) {
                                List<R> result= new ArrayList<R>();
                                for (Triple<Boolean, Response<R>, Throwable> triple : input) {
                                    if (responseSuccess(triple)) {
                                        result.add(triple.second.body());
                                    }
                                }
                                return result;
                            }
                        })
                        .flatMap(new ListIdentityOperator<R>())
                        .perform(domino)
                ));
    }

    private static <R> boolean responseFailure(Triple<Boolean, Response<R>, Throwable> triple) {
        return triple.first && (
                triple.second.isSuccessful() && triple.second.body() == null ||
                        !triple.second.isSuccessful());
    }

    public RetrofitDomino<T, R> onResponseFailure(final Action0 action0) {
        return new RetrofitDomino<T, R>(
                reduce(new ListIdentityOperator<Triple<Boolean, Response<R>, Throwable>>())
                .perform(new Action1<List<Triple<Boolean, Response<R>, Throwable>>>() {
                    @Override
                    public void call(List<Triple<Boolean, Response<R>, Throwable>> input) {
                        boolean failure = false;
                        for (Triple<Boolean, Response<R>, Throwable> triple : input) {
                            if (responseFailure(triple)) {
                                failure = true;
                                break;
                            }
                        }
                        if (failure) {
                            action0.call();
                        }
                    }
                })
                .flatMap(new ListIdentityOperator<Triple<Boolean,Response<R>,Throwable>>()));
    }

    public RetrofitDomino<T, R> onResponseFailure(final Action1<Response<R>> action1) {
        return new RetrofitDomino<T, R>(
                perform(new Action1<Triple<Boolean, Response<R>, Throwable>>() {
                    @Override
                    public void call(Triple<Boolean, Response<R>, Throwable> input) {
                        if (responseFailure(input)) {
                            action1.call(input.second);
                        }
                    }
                })
        );
    }

    public <S> RetrofitDomino<T, R> onResponseFailure(final Class<? extends S> target, final TargetAction0<? super S> targetAction0) {
        return new RetrofitDomino<T, R>(
                reduce(new ListIdentityOperator<Triple<Boolean,Response<R>,Throwable>>())
                .perform(target, new TargetAction1<S, List<Triple<Boolean, Response<R>, Throwable>>>() {
                    @Override
                    public void call(S s, List<Triple<Boolean, Response<R>, Throwable>> input) {
                        boolean failure = false;
                        for (Triple<Boolean, Response<R>, Throwable> triple : input) {
                            if (responseFailure(triple)) {
                                failure = true;
                                break;
                            }
                        }
                        if (failure) {
                            targetAction0.call(s);
                        }
                    }
                }).flatMap(new ListIdentityOperator<Triple<Boolean,Response<R>,Throwable>>()));
    }

    public <S> RetrofitDomino<T, R> onResponseFailure(final Class<? extends S> target, final TargetAction1<? super S, Response<R>> targetAction1) {
        return new RetrofitDomino<T, R>(
                perform(target, new TargetAction1<S, Triple<Boolean, Response<R>, Throwable>>() {
                    @Override
                    public void call(S s, Triple<Boolean, Response<R>, Throwable> input) {
                        if (responseFailure(input)) {
                            targetAction1.call(s, input.second);
                        }
                    }
                })
        );
    }

    public RetrofitDomino<T, R> onResponseFailure(final Domino<Response<R>, ?> domino) {
        return new RetrofitDomino<T, R>(
                perform(Shelly.<Triple<Boolean, Response<R>, Throwable>>createAnonymousDomino()
                        .reduce(new Function1<List<Triple<Boolean, Response<R>, Throwable>>, List<Response<R>>>() {
                            @Override
                            public List<Response<R>> call(List<Triple<Boolean, Response<R>, Throwable>> input) {
                                List<Response<R>> result= new ArrayList<Response<R>>();
                                for (Triple<Boolean, Response<R>, Throwable> triple : input) {
                                    if (responseFailure(triple)) {
                                        result.add(triple.second);
                                    }
                                }
                                return result;
                            }
                        })
                        .flatMap(new ListIdentityOperator<Response<R>>())
                        .perform(domino)
                ));
    }
}
