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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import xiaofei.library.shelly.domino.converter.DominoConverter;
import xiaofei.library.shelly.domino.converter.RetrofitDominoConverter;
import xiaofei.library.shelly.domino.converter.RetrofitDominoConverter2;
import xiaofei.library.shelly.function.Action0;
import xiaofei.library.shelly.function.Action1;
import xiaofei.library.shelly.function.Action2;
import xiaofei.library.shelly.function.Function1;
import xiaofei.library.shelly.function.Function2;
import xiaofei.library.shelly.function.TargetAction0;
import xiaofei.library.shelly.function.TargetAction1;
import xiaofei.library.shelly.operator.EmptyOperator;
import xiaofei.library.shelly.operator.FilterOperator;
import xiaofei.library.shelly.operator.FlatMapOperator;
import xiaofei.library.shelly.operator.ListIdentityOperator;
import xiaofei.library.shelly.operator.MapOperator;
import xiaofei.library.shelly.operator.MapOperator2;
import xiaofei.library.shelly.operator.ReducerOperator;
import xiaofei.library.shelly.operator.RightRefinementOperator;
import xiaofei.library.shelly.scheduler.BackgroundQueueScheduler;
import xiaofei.library.shelly.scheduler.BackgroundScheduler;
import xiaofei.library.shelly.scheduler.DefaultScheduler;
import xiaofei.library.shelly.scheduler.NewThreadScheduler;
import xiaofei.library.shelly.scheduler.Scheduler;
import xiaofei.library.shelly.util.Player;
import xiaofei.library.shelly.scheduler.ThrottleScheduler;
import xiaofei.library.shelly.scheduler.UiThreadScheduler;
import xiaofei.library.shelly.task.AbstractRetrofitTask;
import xiaofei.library.shelly.task.Task;
import xiaofei.library.shelly.tuple.Pair;
import xiaofei.library.shelly.tuple.Triple;
import xiaofei.library.shelly.util.DominoCenter;
import xiaofei.library.shelly.util.Tile;
import xiaofei.library.shelly.util.TargetCenter;
import xiaofei.library.shelly.util.TaskFunction;

/**
 * Created by Xiaofei on 16/5/26.
 */
public class Domino<T, R> {

    protected static final DominoCenter DOMINO_CENTER = DominoCenter.getInstance();

    protected static final TargetCenter TARGET_CENTER = TargetCenter.getInstance();

    private Tile<T, R> mTile;

    private Object mLabel;

    public Domino(Object label) {
        this(label, new Tile<T, R>() {
            @Override
            public Player<R> call(List<T> input) {
                return (Player<R>) new Player<T>(input);
            }
        });
    }

    public Domino(Object label, Tile<T, R> tile) {
        mLabel = label;
        mTile = tile;
    }

    public Object getLabel() {
        return mLabel;
    }

    public Tile<T, R> getPlayer() {
        return mTile;
    }

    public <S extends Domino<T, ?>> S convert(DominoConverter<T, R, S> converter) {
        return converter.call(this);
    }

    /**
     * T是原本输入的参数类型，R是将传给下面的参数类型！
     *
     */

    public <U> Domino<T, R> perform(final Class<? extends U> target, final TargetAction0<? super U> targetAction0) {
        return performInternal(new Action2<Player<R>, List<R>>() {
            @Override
            public void call(Player<R> player, List<R> input) {
                CopyOnWriteArrayList<Object> objects = TARGET_CENTER.getObjects(target);
                player.prepare(targetAction0);
                for (Object object : objects) {
                    targetAction0.call(target.cast(object));
                }
            }
        });
    }

    public <U> Domino<T, R> perform(final Class<? extends U> target, final TargetAction1<? super U, ? super R> targetAction1) {
        return performInternal(new Action2<Player<R>, List<R>>() {
            @Override
            public void call(Player<R> player, List<R> input) {
                CopyOnWriteArrayList<Object> objects = TARGET_CENTER.getObjects(target);
                player.prepare(targetAction1);
                for (Object object : objects) {
                    for (R singleInput : input) {
                        targetAction1.call(target.cast(object), singleInput);
                    }
                }
            }
        });
    }

    public Domino<T, R> perform(final Action0 action0) {
        return performInternal(new Action2<Player<R>, List<R>>() {
            @Override
            public void call(Player<R> player, List<R> input) {
                player.prepare(action0);
                action0.call();
            }
        });
    }

    public Domino<T, R> perform(final Action1<? super R> action1) {
        return performInternal(new Action2<Player<R>, List<R>>() {
                    @Override
                    public void call(Player<R> player, List<R> input) {
                        player.prepare(action1);
                        for (R singleInput : input) {
                            action1.call(singleInput);
                        }
                    }
                });
    }

    public Domino<T, R> perform(final Domino<? super R, ?> domino) {
        return performInternal(new Action2<Player<R>, List<R>>() {
                    @Override
                    public void call(Player<R> player, List<R> input) {
                        //TODO How about stash here?
                        ((Domino<R, ?>) domino).mTile.call(input);
                    }
                });
    }

    private Domino<T, R> performInternal(final Action2<Player<R>, List<R>> action2) {
        return new Domino<T, R>(mLabel, new Tile<T, R>() {
            @Override
            public Player<R> call(List<T> input) {
                final Player<R> player = mTile.call(input);
                player.play(new Tile<R, R>() {
                    @Override
                    public Player<R> call(List<R> input) {
                        action2.call(player, input);
                        return player;
                    }
                });
                return player;
            }
        });
    }

    public <U> Domino<T, U> dominoMap(final Domino<? super R, ? extends U> domino) {
        return merge((Domino<R, U>[]) new Domino[]{domino});
    }

    public <U> Domino<T, U> merge(Domino<? super R, ? extends U> domino1, Domino<? super R, ? extends U> domino2) {
        return merge((Domino<R, U>[]) new Domino[]{domino1, domino2});
    }

    public <U> Domino<T, U> merge(Domino<? super R, ? extends U> domino1, Domino<? super R, ? extends U> domino2, Domino<? super R, ? extends U> domino3) {
        return merge((Domino<R, U>[]) new Domino[]{domino1, domino2, domino3});
    }

    public <U> Domino<T, U> merge(final Domino<? super R, ? extends U>[] dominoes) {
        return new Domino<T, U>(mLabel, new Tile<T, U>() {
            @Override
            public Player<U> call(List<T> input) {
                final Player<R> player = mTile.call(input);
                List<Function1<CopyOnWriteArrayList<R>, CopyOnWriteArrayList<U>>> functions =
                        new ArrayList<Function1<CopyOnWriteArrayList<R>, CopyOnWriteArrayList<U>>>();
                for (final Domino<? super R, ? extends U> domino : dominoes) {
                    functions.add(new Function1<CopyOnWriteArrayList<R>, CopyOnWriteArrayList<U>>() {
                        @Override
                        public CopyOnWriteArrayList<U> call(CopyOnWriteArrayList<R> input) {
                            Player<U> player = ((Domino<R, U>) domino).mTile.call(input);
                            return (CopyOnWriteArrayList<U>) player.waitForFinishing();
                        }
                    });
                }
                return player.playFunction(functions);
            }
        });
    }

    public <S1, S2, V> Domino<T, V> combine(Domino<? super R, S1> domino1, Domino<? super R, S2> domino2,
                                            final Function2<? super S1, ? super S2, ? extends V> combiner) {
        /**
         * 想实现的效果是domino1和domino2分开运行，结果经过combiner结合，得到一堆新的结果
         * 为了实现方便，做如下变化：
         * 1、将domino1与domino2分开运行，返回结果变为一个list，这个单独作为一个结果：domino->reduce->merge
         * 2、将这个结果进行合并：reduce
         * 3、将这个结果展开：flatMap
         */
        return merge(
                domino1.reduce(new Function1<List<S1>, Pair<Integer, List<Object>>>() {
                    @Override
                    public Pair<Integer, List<Object>> call(List<S1> input) {
                        return Pair.create(1, (List<Object>) input);
                    }
                }),
                domino2.reduce(new Function1<List<S2>, Pair<Integer, List<Object>>>() {
                    @Override
                    public Pair<Integer, List<Object>> call(List<S2> input) {
                        return Pair.create(2, (List<Object>) input);
                    }
                }))
                .reduce(new Function1<List<Pair<Integer, List<Object>>>, List<V>>() {
                    @Override
                    public List<V> call(List<Pair<Integer, List<Object>>> input) {
                        if (input.size() != 2) {
                            throw new IllegalStateException("Unknown error! Please report this to Xiaofei.");
                        }
                        List<V> result = new ArrayList<V>();
                        List<Object> input1, input2;
                        if (input.get(0).first == 1) {
                            input1 = input.get(0).second;
                            input2 = input.get(1).second;
                        } else {
                            input1 = input.get(1).second;
                            input2 = input.get(0).second;
                        }
                        for (Object o1 : input1) {
                            for (Object o2 : input2) {
                                result.add(combiner.call((S1) o1, (S2) o2));
                            }
                        }
                        return result;
                    }
                })
                .flatMap(new ListIdentityOperator<V>());
    }

    public <S1, S2, U1, U2, V> Domino<T, V> combineTask(
            TaskDomino<? super R, S1, U1> taskDomino1,
            TaskDomino<? super R, S2, U2> taskDomino2,
            final Function2<? super S1, ? super S2, ? extends V> combiner) {
        return combine(
                taskDomino1.map(new Function1<Triple<Boolean,S1,U1>, S1>() {
                    @Override
                    public S1 call(Triple<Boolean, S1, U1> input) {
                        return input.first ? input.second : null;
                    }
                }),
                taskDomino2.map(new Function1<Triple<Boolean,S2,U2>, S2>() {
                    @Override
                    public S2 call(Triple<Boolean, S2, U2> input) {
                        return input.first ? input.second : null;
                    }
                }),
                combiner);
    }

    private Domino<T, R> schedule(final Scheduler scheduler) {
        return new Domino<T, R>(mLabel, new Tile<T, R>() {
            @Override
            public Player<R> call(List<T> input) {
                Player<R> player = mTile.call(input);
                player.setScheduler(scheduler);
                return player;
            }
        });
    }

    public Domino<T, R> background() {
        return schedule(new BackgroundScheduler());
    }

    /**
     * For unit test only.
     */
    Domino<T, R> newThread() {
        return schedule(new NewThreadScheduler());
    }

    /**
     * For unit test only.
     */
    Domino<T, R> defaultScheduler() {
        return schedule(new DefaultScheduler());
    }

    public Domino<T, R> uiThread() {
        return schedule(new UiThreadScheduler());
    }

    public Domino<T, R> backgroundQueue() {
        return schedule(new BackgroundQueueScheduler());
    }

    public Domino<T, R> throttle(final long windowDuration, final TimeUnit unit) {
        return new Domino<T, R>(mLabel, new Tile<T, R>() {
            @Override
            public Player<R> call(List<T> input) {
                Player<R> player = mTile.call(input);
                player.setScheduler(new ThrottleScheduler(player.getScheduler(), mLabel, windowDuration, unit));
                return player;
            }
        });
    }

    public <U> Domino<T, U> lift(final Function1<CopyOnWriteArrayList<R>, CopyOnWriteArrayList<U>> function) {
        return new Domino<T, U>(mLabel, new Tile<T, U>() {
            @Override
            public Player<U> call(final List<T> input) {
                final Player<R> player = mTile.call(input);
                return player.playFunction(Collections.singletonList(function));
            }
        });
    }

    public <U> Domino<T, U> map(Function1<? super R, ? extends U> map) {
        return lift(new MapOperator<R, U>(map));
    }

    public <U, S> Domino<T, U> map(Class<S> target, Function2<? super S, ? super R, ? extends U> map) {
        return lift(new MapOperator2<R, U, S>(target, map));
    }

    public <U> Domino<T, U> flatMap(Function1<? super R, List<U>> map) {
        return lift(new FlatMapOperator<R, U>(map));
    }

    public Domino<T, R> filter(Function1<? super R, Boolean> filter) {
        return lift(new FilterOperator<R>(filter));
    }

    //scheduler的函数是一个高阶函数
    public <U> Domino<T, U> reduce(final Function1<List<R>, ? extends U> reducer) {
        return lift(new ReducerOperator<R, U>(reducer));
    }

    public <U> Domino<T, U> clear() {
        return lift(new EmptyOperator<R, U>());
    }

    public <U, S> TaskDomino<T, U, S> beginTask(Task<R, U, S> task) {
        Domino<T, Triple<Boolean, U, S>> domino = map(
                new TaskFunction<R, U, U, S, S>(
                        task,
                        new RightRefinementOperator<R, U>(),
                        new RightRefinementOperator<R, S>()));
        return new TaskDomino<T, U, S>(domino.getLabel(), domino.getPlayer());
    }

    public <U1, U2, S1, S2> TaskDomino<T, U2, S2> beginTask(
            Task<R, U1, S1> task, Function2<R, U1, U2> func1, Function2<R, S1, S2> func2) {
        Domino<T, Triple<Boolean, U2, S2>> domino = map(new TaskFunction<R, U1, U2, S1, S2>(task, func1, func2));
        return new TaskDomino<T, U2, S2>(domino.getLabel(), domino.getPlayer());
    }

    public <U, S> TaskDomino<T, Pair<R, U>, S> beginTaskKeepingInput(Task<R, U, S> task) {
        Domino<T, Triple<Boolean, Pair<R, U>, S>> domino = map(
                new TaskFunction<R, U, Pair<R, U>, S, S>(
                        task,
                        new Function2<R, U, Pair<R, U>>() {
                            @Override
                            public Pair<R, U> call(R input1, U input2) {
                                return Pair.create(input1, input2);
                            }
                        },
                        new RightRefinementOperator<R, S>()));
        return new TaskDomino<T, Pair<R, U>, S>(domino.getLabel(), domino.getPlayer());
    }

    public <U> RetrofitDomino<T, U> beginRetrofitTask(AbstractRetrofitTask<R, U> task) {
        return beginTask(task).convert(new RetrofitDominoConverter<T, U>());
    }

    public <U> RetrofitDomino2<T, R, U> beginRetrofitTaskKeepingInput(AbstractRetrofitTask<R, U> task) {
        return beginTaskKeepingInput(task).convert(new RetrofitDominoConverter2<T, R, U>());
    }

    public void play(CopyOnWriteArrayList<T> input) {
        mTile.call(input);
    }

    public void commit() {
        DOMINO_CENTER.commit(this);
    }

}
//// TODO: 16/6/30 加上包裹类，这时候begintask会怎么样

// TODO 加上stash，整个传进来map；之后参数可以传入map
// TODO 加上onSuccessBeginTask，参考其他