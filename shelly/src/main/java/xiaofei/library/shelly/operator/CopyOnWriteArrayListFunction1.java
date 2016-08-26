package xiaofei.library.shelly.operator;

import java.util.concurrent.CopyOnWriteArrayList;

import xiaofei.library.shelly.function.Function1;

/**
 * Created by Xiaofei on 16/7/18.
 */
public interface CopyOnWriteArrayListFunction1<T, R> extends Function1<CopyOnWriteArrayList<T>, CopyOnWriteArrayList<R>> {
}
