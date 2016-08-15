# Usage

[Chinese Translation 中文翻译](https://github.com/Xiaofei-it/Shelly/blob/master/doc-zh-cn/USAGE.md)

This article illustrates the usage of the Shelly library. Here I focus on the basic usage including
component registration, Domino creation and Domino invocation. After reading these, you will have a
basic understanding of the Shelly library.

The Domino discussed in the following is the basic Domino, which provides the basic methods for
performing various kinds of actions, for data transformation and for thread scheduling.

Moreover, the Shelly library also provides many other useful Dominoes, including but not limited to:

1. Task Domino,
which provides methods for executing a time-consuming task and performing various
kinds of actions according to the result or the failure of the task execution. The usage of a Task
Domino makes source code concerning the business logic of your app clear and easy to understand.

2. Retrofit Domino,
which provides a convenient pattern for sending an HTTP request and performing
various kinds of actions according to the result or the failure of the request. The
Retrofit Task is very useful in the development of an app, which takes many advantages over the other
architectures for sending HTTP requests.

For the information about various kinds of Dominoes, please see [MORE DOMINOES](MORE_DOMINOES.md).

Also, the Shelly library provides methods for merging the results of two Dominoes and combing two
results of two Dominoes into one result, which is useful especially when it comes to the Retrofit
Domino. These methods allow you to write a Domino which sends two HTTP requests at the same time
and uses the results of the two requests to perform actions. Also, you can write a Domino which
sends an HTTP request and after getting its result, sends another request. These features are inspired
by RxJava. See [DOMINO COMBINATION](DOMINO_COMBINATION.md) for more information.

Moreover, the Shelly library provides some useful utilities, such as the stash to store and
get objects and the tuple class to combine several input together. Please see [UTILITIES](UTILITIES.md)
for more information.

The shelly library provides a novel pattern for developing a business-logic-oriented app, which makes
the souce code concerning the business logic clear and easy to understand and
makes the app easy to maintain. Please see [METHODOLOGY](METHODOLOGY.md) for the methodology.

## Component registration

Each component which changes according to the change of a business object should be registered first,
and should be unregistered whenever it is destroyed.

The following is an example of the registration and unregistration of an Activity:

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

## Domino creation

A domino should be created and committed before it takes effect. The following is an example illustrating
various APIs for creating and committing a Domino.
And more APIs can be found in the
[Domino](https://github.com/Xiaofei-it/Shelly/blob/master/shelly/src/main/java/xiaofei/library/shelly/domino/Domino.java) class.

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

Remember to commit the domino finally!

Each domino should be specified a unique label, which is an object, i.e. an Integer, a
String or something else.

Note that the above code will not perform any actions! What the code does is simply committing and
storing the Domino for later use. To make the Domino perform actions, you should invoke the Domino.
Only after the Domino is invoked will it perform actions.

## Domino invocation

To invoke a domino, do the following:

```
Shelly.playDomino("Example", "Single String"); // Pass a single String to the domino

Shelly.playDomino("Example", "First String", "Second String"); // Pass two Strings to the domino
```

## Anonymous Domino

As is shown above, a unique label is needed to label the Domino to be invoked,
thus you should specify a unique label when creating a Domino, otherwise the created Domino shall
not be committed.

However, a Domino which do not have a label ("Anonymous Domino") is also quite useful in that,
there exist some situation where you only need to create a Domino but not want to commit it.
For example, you can perform an action on an anonymous Domino:

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

Moreover, you can merge or combine two anonymous Dominoes.
See [DOMINO COMBINATION](DOMINO_COMBINATION.md) for more information.

## More kinds of Dominoes

The Domino class provides many basic methods. Also you can write derived Dominoes which extend the
class. In the Shelly library, there are already several kinds of derived Dominoes, which are shown
below.

The Task Domino provides methods for executing a time-consuming task and performing various
kinds of actions according to the result or the failure of the task execution. The usage of a Task
Domino makes the source code concerning the business logic of your app clear and easy to understand.

The Retrofit Domino provides a convenient pattern for sending an HTTP request and performing
various kinds of actions according to the result or the failure of the request. The
Retrofit Domino is very useful in the development of an app, which takes many advantages over the other
architectures for sending HTTP requests.

For the information about various kinds of Dominoes, please see [MORE DOMINOES](MORE_DOMINOES.md).

## Merging and combination of Dominoes

The Shelly library provides methods for merging the results of two Dominoes and combing two
results of two Dominoes into one result, which is useful especially when it comes to the Retrofit
Domino. These methods allow you to write a Domino which sends two HTTP requests at the same time
and uses the results of the two requests to perform actions. Also, you can write a Domino which
sends an HTTP request and after getting its result, sends another request. These features are inspired
by RxJava.

The Shelly library provides the methods for invoking Dominoes within a Domino.

See [DOMINO COMBINATION](DOMINO_COMBINATION.md) for more information.

## Tuple and stash

The Shelly library provides some useful utilities, such as the stash to store and
get objects and the tuple class to combine several input together. Please see [UTILITIES](UTILITIES.md)
for more information.

## Methodology

The shelly library provides a novel pattern for developing a business-logic-oriented app, which makes
the source code concerning the business logic clear and easy to understand and
makes the app easy to maintain. Please see [METHODOLOGY](METHODOLOGY.md) for the methodology.
