# ToyRoom

基于[Shelly](https://github.com/Xiaofei-it/Shelly)的面向业务逻辑的编程库。[Shelly](https://github.com/Xiaofei-it/Shelly) plays Domino in ToyRoom.

## 特色

1. 为面向业务逻辑的编程提供一种全新的编程模式。

2. 无论业务逻辑怎么变化，使用ToyRoom库编写的代码都易于理解和维护。

3. 能够方便地发送网络请求和处理回调，尤其是发送并发请求和连续请求。

4. 能够方便地执行耗时任务和处理回调。

5. 提供强大丰富的数据流控制的API和线程调度的API。

## 预览

ToyRoom库基于[Shelly](https://github.com/Xiaofei-it/Shelly)库。[Shelly](https://github.com/Xiaofei-it/Shelly)是一个面向业务逻辑的编程库，它提供了一种全新的编程模式，将业务对象的变化对各个模块的影响通过方法链表示出来。

使用ToyRoom库时，你可以使用一个方法链创建一个名为“Domino”的对象。方法链中的每个方法都以一个action作为参数。创建的Domino一旦被调用，就会根据方法链中的action序列执行每个action。

在介绍之前，我们先看一个例子。

假设现在你想打印文件夹里所有文件的名字。使用ToyRoom库，你可以写如下代码：

```
Shelly.<String>createDomino("Print file names")
        .background()
        .flatMap((Function1) (input) -> {
                File[] files = new File(input).listFiles();
                List<String> result = new ArrayList<String>();
                for (File file : files) {
                    result.add(file.getName());
                }
                return result;
        })
        .perform((Action1) (input) -> {
                System.out.println(input);
        })
        .commit();
```

上面的代码用方法链打印文件夹中的文件名。文件夹的路径被传入，`Function1`获取此路径下的所有文件并将文件名传给`Action1`，`Action1`将文件名打印出来。

我们看一个稍微复杂的例子。假设现在你想使用Retrofit发送HTTP请求，然后

1. 如果服务端的响应成功，那么调用`MyActivity`和`SecondActivity`中的两个函数；

2. 如果服务端的响应失败，那么在屏幕上显示一个toast；

3. 如果在发请求的时候出现错误或者异常，那么打印错误信息。

使用ToyRoom库，你可以写下面的代码：

```
Shelly.<String>createDomino("Sending request")
        .background()
        .beginRetrofitTask((RetrofitTask) (s) -> {
                return netInterface.test(s);
        })
        .uiThread()
        .onSuccessResult(MainActivity.class, (TargetAction1) (mainActivity, input) -> {
                mainActivity.show(input.string());
        })
        .onSuccessResult(SecondActivity.class, (TargetAction1) (secondActivity, input) -> {
                secondActivity.show(input.string());
        })
        .onResponseFailure(MainActivity.class, (TargetAction1) (mainActivity, input) -> {
                Toast.makeText(
                    mainActivity.getApplicationContext(),
                    input.errorBody().string(),
                    Toast.LENGTH_SHORT
                ).show();
        })
        .onFailure((Action1) (input) -> {
                Log.e("Eric Zhao", "Error", input);
        })
        .endTask()
        .commit();
```

一个URL被传入，Retrofit发送HTTP请求，之后根据不同结果执行相应的操作。

代码中有一些线程调度相关的东西，比如`background()`和`uiThread()`。`background()`是说下面的操作在后台执行。`uiThread()`是说下面的操作在主线程（UI线程）执行。

上面的例子中，你可以看出发送HTTP请求后`MainActivity`和`SecondActivity`是如何变化的。我们在一个地方就可以看到整个世界的变化。


注意，如果不调用Domino，上面这段代码实际上并不会执行任何操作！这段代码做的只是提交并存储Domino，供以后使用。想要让Domino执行操作，必须调用它。只有调用Domino后，它才会执行操作。

这些只是简单的例子。实际上，ToyRoom库是非常强大的，将在后面几节介绍。

## 思想

本节简单介绍ToyRoom库的理论。如果想要看完整版，请查看[理论部分](doc/THEORY.md)。

在面向业务逻辑的编程中，一个特定的业务对象的改变可能会引起各个组件的变化，业务逻辑的复杂性也会增加模块之间的耦合。为了降低耦合，我们通常使用listeners（observers）或者event bus，这些易于使用并且非常有效，但是有一些缺点，比如难以维护，也可能有内存泄漏的风险。

为了解决这些问题，我写了ToyRoom库。

ToyRoom库提供了一种全新的编程模式，将业务对象的变化对各个模块的影响通过方法链表示出来。在方法链中，每个方法有一个action参数，这个action执行相应的操作改变特定的组件。方法串起来后就代表了一系列的对各个组件的操作。这样你就能从这一个地方看到整个世界的变化。

使用ToyRoom库时，你可以使用一个方法链创建一个名为“Domino”的对象。方法链中的每个方法都以一个action作为参数。创建的Domino一旦被调用，就会根据方法链中的action序列执行每个action。

创建Domino后，你可以调用Domino执行action序列中的每个action。如果一个业务对象发生改变，你只需调用Domino，并且将这个对象传给它，然后它就会按action序列一个个执行action。

如果要看详细的思想，请看[理论部分](doc/THEORY.md)。这里也会给出关于ToyRoom库的许多技术术语的定义，比如Domino和数据流。

## 与RxJava比较

在开发ToyRoom库的过程中，我发现了RxJva。然后我研究并学习了它的思想和实现，因此ToyRoom库和RxJava风格在某种程度上相似，但它们的思想、实现和用法很不相同，具体细节在[理论部分](doc/THEORY.md)描述.

## 下载

### Gradle

```
compile 'me.ele.android:toyroom:0.1.1'
```

### Maven

```
<dependency>
  <groupId>me.ele.android</groupId>
  <artifactId>toyroom</artifactId>
  <version>0.1.1</version>
  <type>pom</type>
</dependency>
```

## 用法

本节对ToyRoom库的用法进行简单介绍。详细用法请查看下面列出的文章：

* [基本用法](doc/USAGE.md)，包含基本用法，包括组件注册、Domino创建和Domino调用。

* [DOMINO种类](doc/MORE_DOMINOES.md)，包含不同种类的Domino的用法。

* [组合Domino](doc/DOMINO_COMBINATION.md)，介绍如何合并或组合两个Domino输出。

* [工具类](doc/UTILITIES.md)，包含ToyRoom库提供的工具类的用法。

* [ToyRoom实战](doc/METHODOLOGY.md)，介绍如何在实战中使用ToyRoom库。

ToyRoom库提供了各种Domino，包括基本Domino、Task Domino和Retrofit Domino。

基本Domino提供各种函数，用来执行各种不同操作、进行数据变换以及进行线程调度。

Task Domino提供函数来执行耗时操作并且根据结果执行各种操作。使用Task Domino可以使app的业务逻辑清晰并且易于理解。

Retrofit Domino提供一种方便的模式用来发送HTTP请求并且根据请求的不同结果进行不同的回调操作。在app开发中使用Retrofit Domino非常有效，相比其他框架有许多优点。

另外，ToyRoom库提供许多方法用来合并或者组合多个Domino输出，这非常有用，尤其对于Retrofit Domino。这些函数可以让你同时发送多个HTTP请求，也可以发送连续请求。这个特色是受RxJava启发而做的。

ToyRoom库还提供了一些有用的工具类，比如用来存取对象的stash和将多个输入组合在一起的tuple。

综上，ToyRoom库为开发面向业务逻辑的app提供了一种全新的模式，可以使业务逻辑代码清晰并且易于理解，也使app易于维护。

## License

Copyright (C) 2016 Xiaofei

HermesEventBus binaries and source code can be used according to the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
