# ToyRoom

A [Shelly](https://github.com/Xiaofei-it/Shelly)-based library for business-logic-oriented programming. [Shelly](https://github.com/Xiaofei-it/Shelly) plays Domino in ToyRoom.

[Readme in Chinese 中文文档](README-ZH-CN.md)

## Features

1. Provides a novel pattern for business-logic-oriented programming.

2. Makes the source code of a business-logic-oriented app easy to understand and maintain, no matter
how the business logic is modified.

3. Convenient for sending HTTP requests and performing callback operations,
especially for sending multiple requests synchronously or sequentially.

4. Convenient for time-consuming tasks and performing callback operations.

5. Provides powerful APIs for data flow control and thread scheduling.

## Preview

The ToyRoom library is based on, or is actually a wrapper of,
the [Shelly](https://github.com/Xiaofei-it/Shelly) library, which is a library for business-logic-oriented
programming and provides a novel pattern which uses a method chain to illustrate how each component
varies with a business object.

With the help of the ToyRoom library, you can use a method chain, in which each method takes an action as an argument,
to create an object named "Domino" which, once invoked, performs each action according to the action
sequence in the method chain.

Before the introduction, let's see an example first.

Suppose that you want to print the names of all the files in a folder. Using the ToyRoom library, you
may write the following to fulfil the requirement:

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

The above code uses a method chain to print the names of all the files. A folder path is passed in.
`Function1` uses the path to get all the files and pass the file names to `Action1`. `Action1` is
performed to print the names.

Now let's see another example which is more complex.
Suppose that you want to use Retrofit to send an HTTP request, and

1. If the response is successful, invoke two particular methods of `MyActivity` and `SecondActivity`;

2. If the response is not successful, show a toast on the screen;

3. If something goes wrong when sending request and an exception is thrown, print the message of the error.

Using the ToyRoom library, you may write the following to fulfil the above requirement:

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

A URL is passed in and Retrofit is used to send an HTTP request. After that, different actions are
performed according to the results of the request.

In the above code, there are also something concerning the thread scheduling, such as `background()` and `uiThread()`.
`background()` means that the following actions are performed in background. And `uiThread()` means
that the following actions are performed in the main thread, i.e. the UI thread.

From the above example, you can see how `MainActivity` and `SecondActivity` change according
to the result or the failure of the HTTP request. We can see the changes of each component
from a single place.

Note that, actually the above code will not perform any actions unless the Domino is invoked!
What the code does is simply committing and storing the Domino for later use.
To make the Domino perform actions, you should invoke the Domino.
Only after the Domino is invoked will it perform actions.

These are just simple examples. Actually, the ToyRoom library is very powerful,
which will be introduced in the following sections.

For more examples, see the demo and test cases of the [Shelly](https://github.com/Xiaofei-it/Shelly)
library. Also, the "Usage" section in this document lists some documents which contain many examples.

## Philosophy

This section illustrates a simple explanation of the theory of the ToyRoom library.
See the [THEORY](doc/THEORY.md) for a detailed introduction to the philosophy.

In business-logic-oriented programming, a change of a particular business object may cause changes
of various components, and the complexity of business logic will increase coupling between components.
To decrease coupling we usually use listeners (observers) or the event bus, which is easy to use and
also effective. However, these techniques have several disadvantages, such as making code difficult
to maintain and leading to a potential risk of memory leaking.

To solve these problems, I compose the ToyRoom library.
The ToyRoom library is based on the [Shelly](https://github.com/Xiaofei-it/Shelly) library,
which provides a novel pattern which uses a method chain to illustrate how each
component varies with a business object. In the method chain, each method takes an action which
represents the change of a particular component. The chain of methods represents all of the changes
of all of the corresponding components. Thus you can see the change of the whole "world" in a single
file rather than searching the whole project for the corresponding classes.

By the ToyRoom library, you can use a method chain in which each method takes an action as an argument,
to create an object named "Domino" which, once invoked, performs each action according to the action
sequence in the method chain.

After the creation of a Domino, you can "invoke" it to perform each action in the action sequence in
the method chain.
When a business object is changed, you "invoke" the Domino and pass the business object to it.
Then it performs the actions in the action sequence one after the other.

See the [THEORY](doc/THEORY.md) for a detailed introduction to the philosophy. Also, it gives the definitions
of the technical terms with respect to the ToyRoom library, such as the Domino and the data flow.

## Comparison with RxJava

In the development of the ToyRoom library, I discovered the RxJava library. Then I researched and
learned from its philosophy and its implementation. Thus the style of the ToyRoom library bears a
rather resemblance to the one RxJava, but their philosophies, implementations and usages are quite different,
which is described in detail in the [THEORY](doc/THEORY.md).

## Downloading

To download the ToyRoom library:

```
compile 'me.ele.android:toyroom:0.1.2'
```

In case you fail to download the ToyRoom library, use the following instead:

```
compile 'xiaofei.library:shelly:0.2.8'
```

## Usage

This section illustrates a brief outline of the usage of the ToyRoom library. For the details of
the usage, please read the articles listed below:

* [BASIC USAGE](doc/USAGE.md), contains the basic usage, including component registration,
Domino creation and Domino invocation.

* [MORE DOMINOES](doc/MORE_DOMINOES.md), contains the usage of various kinds of Dominoes.

* [DOMINO COMBINATION](doc/DOMINO_COMBINATION.md), illustrates how to merge the outputs of two
Dominoes and combing two outputs of two Dominoes into one input for the following action.

* [UTILITIES](doc/UTILITIES.md), contains the usage of the utilities provided by the ToyRoom library.

* [METHODOLOGY](doc/METHODOLOGY.md), illustrates how to use the ToyRoom library in action.

The ToyRoom library provides several kinds of Dominoes, including the basic Domino, the Task Domino
and the Retrofit Domino.

The basic Domino provides the basic methods for performing various kinds of actions,
for data transformation and for thread scheduling.

The Task Domino provides methods for executing a time-consuming task and performing various
kinds of actions according to the result or the failure of the task execution. The usage of a Task
Domino makes the source code concerning the business logic of your app clear and easy to understand.

The Retrofit Domino provides a convenient pattern for sending an HTTP request and performing
various kinds of actions according to the result or the failure of the request. The
Retrofit Domino is very useful in the development of an app, which takes many advantages over the other
architectures for sending HTTP requests.

Also, the ToyRoom library provides methods for merging the outputs of two Dominoes and combing two
outputs of two Dominoes into one input for the following action, which is useful especially
when it comes to the Retrofit Domino.
These methods allow you to write a Domino which sends two HTTP requests at the same time
and uses the results of the two requests to perform actions. Also, you can write a Domino which
sends an HTTP request and after getting its result, sends another request. These features are inspired
by RxJava.

Moreover, the ToyRoom library provides some useful utilities, such as the stash to store and
get objects and the tuple class to combine several input together.

In summary, the ToyRoom library provides a novel pattern for developing a business-logic-oriented app,
which makes the source code concerning the business logic clear and easy to understand and makes
the app easy to maintain.

## License

Copyright (C) 2016 Xiaofei

HermesEventBus binaries and source code can be used according to the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
