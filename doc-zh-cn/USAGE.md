# 用法

本文阐述Shelly库的用法。这里关注基本用法，包括组建注册，Domino创建和Domino调用。读了这些之后，你会对Shelly库有基本了解。

下面讨论的Domino是基本Domino，这种Domino提供了许多基本函数用来执行不同的action、进行数据变换以及进行线程调度。

另外，Shelly库也提供许多其他的Domino，包括但不限于：

1. Task Domino，提供函数来执行耗时操作并且根据结果执行各种操作。使用Task Domino可以使app的业务逻辑清晰并且易于理解。

2. Retrofit Domino，提供一种方便的模式用来发送HTTP请求并且根据请求的不同结果进行不同的回调操作。在app开发中使用Retrofit Domino非常有效，相比其他框架有许多优点。

要了解更多种类的Domino，请参看[Domino种类](MORE_DOMINOES.md)。

另外，Shelly库提供函数用来合并和组合Domino的输出，这个非常有用，尤其在使用Retrofit Domino的时候。这些函数让你可以写一个Domino同时发送两个请求然后根据这两个请求的结果执行操作。你也可以写一个Domino发送连续请求，执行前一个请求得到结果后，发送另一个请求。这些特色是受RxJava启发。查看[组合Domino](DOMINO_COMBINATION.md)获取更多信息。

Shelly库也提供一些有用的工具类，包括存取对象的stash以及组合多个输入的tuple类。查看[工具类](UTILITIES.md)获取更多信息。

Shelly库为开发面向业务逻辑的app提供了一种全新的模式，使得业务逻辑代码清晰并且易于理解，也使得app更易维护。参看[Shelly实战](METHODOLOGY.md)了解如何在实战中使用Shelly库。

## 组建注册

每个随业务对象变化而变化的组建必须提前注册，并且在销毁时反注册。

下面的例子展示了如何注册和反注册Activity：

```
public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Shelly.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shelly.unregister(this);
    }

}
```

## 创建Domino

一个Domino在起作用之前应该先被创建，并且提交。下面是一个创建并提交Domino的例子。更多的API可以查看[Domino](https://github.com/Xiaofei-it/Shelly/blob/master/shelly/src/main/java/xiaofei/library/shelly/domino/Domino.java)类。

```
// Create a domino labeled "Example" which takes one or more Strings as input.
Shelly.<String>createDomino("Example")
        // Perform an action. The action is performed once.
        .perform(new Action0() {
            @Override
            public void call() {
                // Do something
            }
        })
        // Perform an action which takes the String as input.
        // If one String is passed to this action, the action is performed once.
        // If two Strings are passed to this action, the action is performed twice.
        .perform(new Action1<String>() {
            @Override
            public void call(String input) {
                // Do something
            }
        })
        // Perform another action which takes the String as input.
        // If one String is passed to this action, the action is performed once.
        // If two Strings are passed to this action, the action is performed twice.
        .perform(new Action1<String>() {
            @Override
            public void call(String input) {
                // Do something
            }
        })
        // The above actions is performed in the thread in which the domino is invoked.
        // Now the following actions will be performed in background.
        .background()
        // Transform the String into an integer.
        // If one String is passed to this action, one integer will be passed to the following actions.
        // If two Strings are passed to this action, two integers will be passed to the following actions.
        .map(new Function1<String, Integer>() {
            @Override
            public Integer call(String input) {
                return null;
            }
        })
        // The following actions will be performed one after the other in background.
        .backgroundQueue()
        // Use a filter to filter the integers.
        // Only the integers labeled "true" will be passed to the following actions.
        .filter(new Function1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer input) {
                return false;
            }
        })
        // Pass the integer into the function and the function takes an integer as input
        // and return a list of Strings. Each String will be passed to the actions following
        // this action.
        // If one integer is passed to this action, and the function returns two Strings,
        // then two Strings will be passed to the following actions.
        // If two integers are passed to this action, and the function takes an integer as input and
        // returns two Strings, then we get four Strings here,
        // then four Strings will be passed to the following actions.
        .flatMap(new Function1<Integer, List<String>>() {
            @Override
            public List<String> call(Integer input) {
                return null;
            }
        })
        // The following actions will be performed in the main thread, i.e. the UI thread.
        .uiThread()
        // Perform an action on all registered instances of MyActivity.
        .perform(MyActivity.class, new TargetAction0<MyActivity>() {
            @Override
            public void call(MyActivity myActivity) {
                // Do something
            }
        })
        // Pass all the Strings into the function and get a single double.
        // Now the following actions will receive only one single input, which is a double.
        .reduce(new Function1<List<String>, Double>() {
            @Override
            public Double call(List<String> input) {
                return null;
            }
        })
        // Perform an action on all registered instances of MyActivity.
        // If there are two instances, then:
        // If one String is passed to this action, the action is performed twice.
        // If two Strings are passed to this action, the action is performed four times.
        .perform(MyActivity.class, new TargetAction1<MyActivity, Double>() {
            @Override
            public void call(MyActivity myActivity, Double input) {
                // Do something
            }
        })
        // Commit the Domino for later use.
        .commit();
```

记住最后要提交Domino！

每个Domino必须指定一个唯一标签，这个标签是一个对象，比如说一个Integer、一个String或者其他。

再次注意上面的代码不会执行任何操作！这段代码只是提交并且存储了这个Domino供以后使用。要让Domino执行操作，必须调用Domino。只有在Domino被调用后，它才会执行操作。

## 调用Domino

要调用Domino，代码如下：

```
Shelly.playDomino("Example", "Single String"); // Pass a single String to the domino

Shelly.playDomino("Example", "First String", "Second String"); // Pass two Strings to the domino
```

## 匿名Domino

上面的例子中，被调用的Domino必须指定一个唯一标签，因此你在创建Domino时必须指定一个唯一标签，否则Domino无法提交。

然而，没有标签的Domino（“匿名Domino”）也非常有用。某些情形下，你可能只想要创建一个Domino而并不想提交，比如你可以在一个Domino上执行操作：

```
Shelly.<String>createDomino("Example 2")
        .perform(new Action1<String>() {
            @Override
            public void call(String input) {

            }
        })
        .perform(Shelly.<String>createAnonymousDomino()
                        .map(new Function1<String, Integer>() {
                            @Override
                            public Integer call(String input) {
                                return null;
                            }
                        })
                        .perform(new Action1<Integer>() {
                            @Override
                            public void call(Integer input) {

                            }
                        })
                        .perform(new Action0() {
                            @Override
                            public void call() {

                            }
                        })
        )
        .perform(new Action1<String>() {
            @Override
            public void call(String input) {

            }
        })
        .commit();
```

另外，你也可以合并或者组合两个匿名Domino。更多信息参看[组合Domino](DOMINO_COMBINATION.md)。

## 更多种类的Domino

Domino类提供了许多基本函数用来执行不同操作。Shelly库也提供许多其他的衍生Domino。

Task Domino提供函数来执行耗时操作并且根据结果执行各种操作。使用Task Domino可以使app的业务逻辑清晰并且易于理解。

Retrofit Domino提供一种方便的模式用来发送HTTP请求并且根据请求的不同结果进行不同的回调操作。在app开发中使用Retrofit Domino非常有效，相比其他框架有许多优点。

要了解更多种类的Domino，请参看[Domino种类](MORE_DOMINOES.md)。

## 合并或组合Domino

Shelly库提供函数用来合并和组合Domino的输出，这个非常有用，尤其在使用Retrofit Domino的时候。这些函数让你可以写一个Domino同时发送两个请求然后根据这两个请求的结果执行操作。你也可以写一个Domino发送连续请求，执行前一个请求得到结果后，发送另一个请求。这些特色是受RxJava启发。

Shelly库还提供函数用来在Domino中调用其他Domino。

查看[组合Domino](DOMINO_COMBINATION.md)获取更多信息。

## Tuple和stash

Shelly库也提供一些有用的工具类，包括存取对象的stash以及组合多个输入的tuple类。查看[工具类](UTILITIES.md)获取更多信息。

## 方法论

Shelly库为开发面向业务逻辑的app提供了一种全新的模式，使得业务逻辑代码清晰并且易于理解，也使得app更易维护。参看[Shelly实战](METHODOLOGY.md)了解如何在实战中使用Shelly库。
