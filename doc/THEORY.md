# Theory

[Chinese Translation 中文翻译](https://github.com/Xiaofei-it/Shelly/blob/master/doc-zh-cn/THEORY.md)

This article discusses the theory of the Shelly library, including the philosophy and the definitions
of the technical terms with respect to the Shelly library.

## Philosophy

In business-logic-oriented programming, a change of a particular business object may cause changes
of various components, and the complexity of business logic will increase coupling between components.
To decrease coupling we usually use listeners (observers) or the event bus, which is easy to use and
also effective. However, these techniques have the following disadvantages:

1. The amount of listeners or events increases as the complexity of business logic does, which makes
the project difficult to maintain.

2. The usage of a particular listener will cause corresponding components to implement the interface
of the listener, which makes code confusing and complicated. What's worse, the abuse of listeners
leads to a potential risk of memory leaking.

3. The usage of the event bus will cause code to be difficult to debug, since it is difficult to
predict and control what happens after the posting of a particular event and you have to find the
usages of the Java class of the event in IDE to find all the components receiving the event.

To solve the above problems, I compose the Shelly library.

The Shelly library provides a novel pattern which uses a method chain to illustrate how each
component varies with a business object. In the method chain, each method takes an action, which
represents the change of a particular component, as an argument.
Thus the chain of methods represents the changes of all of the corresponding components.
Therefore you can see the change of the whole "world" in a single file rather than searching
the whole project for the corresponding classes.

Specifically, a method chain corresponds to a piece of business logic and a business object. It
illustrates that if this business object is changed, what happens to the whole app, according to
this piece of business logic.

When the method chain is created, the class of the business object should be specified and then each method
is appended to the chain. When the Domino is invoked (which will be mentioned below), each action,
which is the argument of each method of the method chain, obtains some objects as arguments and then is performed.

More attention should be paid to the input of each action. The first action of the method chain
takes the business objects, which are passed in when invoking the Domino, as arguments.
Then after it is performed, it passes the business objects to the
following action, which is also performed and then passes the business objects to the following
action. Thus the business objects are passed between actions until they are passed to an action for
performing data transformation, which takes the business objects as an argument and returns one or more new
objects. After the transformation, the new objects are passed to the following actions and
the old ones are discarded.

Now pay attention to the action. The action can be regarded as a method which takes the objects
passed to it as input and executes the statements inside it. Also the Shelly library provides
an EventBus-like feature, in that there exists some special actions which take the registered
components (which should be registered first, usually at the same time when they are
created) and the objects passed to the actions as input and executes the statements inside the actions.

The Shelly library provides many methods to compose a method chain, including a variety of methods
for performing different actions, methods for data transformation and methods for thread scheduling.
Also it, as is mentioned above, provides an EventBus-like feature for preforming actions on registered
components.

A method chain provides you with a global view of what happens after the change of a business object.
All of the methods of the method chain return the instances of the same class, which is named "Domino"
in the Shelly library, since it represents a series of actions to perform one after the other,
as the domino game does.

After the creation of a Domino, you can "invoke" it to perform the specified actions.
When a business object is changed, you "invoke" the corresponding Domino and pass the business object to it.
Then it performs the actions in the action sequence one after the other.

## Comparison with RxJava

In the development of the Shelly library, I discovered the RxJava library. Then I researched and
learned from its philosophy and its implementation. Thus the style of the Shelly library bears a
rather resemblance to the one RxJava, but their philosophies, implementations and usages are quite different,
which is described from the following aspects:

1. Implementations.
The `Domino` of the Shelly library bears a rather resemblance to the one of the `Observable` of RxJava.
However, since the usages of the Shelly library and RxJava are different, there exists quite a lot
of differences between them, especially in that the `Domino` takes two generic type arguments and has
a `Tile` function as one of its fields, which returns a `Player` after being called. After all,
it is these differences that make the Shelly library work.

2. Actions performed.
The `Domino` of the Shelly library has many methods similar to the ones of the `Observable` of RxJava.
Such methods are used for data flow control and thread scheduling. However, the `Domino` has many
`perform` methods used for performing various actions at a time, which is a feature RxJava does not support.
Specifically, the `perform` methods of the `Domino` is similar to the `subscribe` methods of the
`Observable`. You can append multiple `perform` methods to the method chain in the Shelly library in
order to cause the `Domino` to perform multiple actions at a time after being invoked, but
you can append only one `subscribe` method to the method chain in RxJava and thus can perform
only one action.

3. An EventBus-like feature.
The Shelly library has an EventBus-like feature which allows you to register components
and then cause the `Domino` to perform actions on the registered components, which RxJava does not
support.

4. Occasions for triggering.
The occasion when the `Domino` performs actions and the one when the `Observable`
subscribes a `Subscriber` are different. In the Shelly library, the `Domino` will not perform any
actions when it is committed. And only when it is invoked will it perform actions. Thus you have to
create the `Domino` at the beginning and once the `Domino` is created, it can be invoked at any time
and can also be invoked for many times.
In RxJava, however, everything will immediately take effect once the `Observable.subscribe()` method
is invoked. Moreover, each time you want to perform a particular operation, you have to create
an `Observable` and cause it to subscribe a `Subscriber`.

## Definitions

This section gives the definitions of the technical terms with respect to the Shelly library.

### On actions

An "action" refers to a sequence of Java statements, the effect of which includes but is not limited
to, performing certain operations on certain components, producing certain outputs, performing
data transformation, and performing thread scheduling. An action is represented by a Java
class or a Java interface, in which there exists a method which is named "call" and contains the
sequence of Java statements of the action. Such a method may be invoked to perform the corresponding
action.

"Performing an action" refers to executing the sequence of Java statements of the action.

### On Dominoes

A "Domino" is a Java object which, under certain circumstances, performs a sequence of actions.
For the sake of simplicity, a "Domino" may also refer to the Java class of a particular Domino
object.

"Creating a Domino" refers to the operation of building a Java instance of a particular Domino
class. A Domino is usually created by a Java method chain which starts with
`Shelly.createDomino(Object)` or `Shelly.createAnonymousDomino()` and is followed by various methods provided
by the Shelly library.

"Committing a Domino" refers to the operation of causing the Shelly library to hold a Java reference
of the particular Domino object for later use.

"Playing a Domino" or "Invoking a Domino" refers to the operation of causing the particular Domino to
perform a sequence of actions. To play a particular Domino, it is always necessary to pass a group
of objects to the Domino as its input. The group must contain one or more objects.

### On input, output and data flow

#### Input of an action

The "input of an action" is a group ("input group") of objects which is passed to the `call` method
of the action as arguments.
The following illustrates the relationship between the input and the performance of an action:

Suppose
the number of arguments the `call` method takes, excluding the arguments representing the components, is `a`,
the number of the objects contained in the input group is `b`,
and the number of occasions when the action is performed is `c`, i.e. the action is performed for `c` times.
Then

1. If b = 0 and a = 0, then c = 1.

2. If b = 0 and a > 0, then c = 0.

3. If b > 0 and a = 0, then c = 1.

4. If b > 0 and a > 0, then c = a * b.

#### Output of an action

Before giving the definition of the "output of an action", the `call` method should be paid more attention
on. As is mentioned above, actions includes but is not limited to, performing certain operations
on certain components, producing certain outputs, performing data transformation, and performing
thread scheduling. Among them, the actions performing data transformation are called the "lifting actions".

The "output of an action" is a group ("output group") of objects, produced in the following way:

1. If the action is not a "lifting action", then the output is exactly the same as the input.

2. If the action is a "lifting action", then the output is produced according to the effect of various
actions. And the number of the objects contained in an output group may be different from the number
of the objects contained in an input group.

#### Types

The "type of the input" is the Java type of elements in the input group.

The "type of the output" is the Java type of elements in the output group.

#### Data flow

Once invoked, a Domino performs a sequence of actions.

The "input of a Domino" is the input of the first action of the action sequence of the Domino.

The "output of a Domino" is the output of the last action of the action sequence of the Domino.

In an action sequence, the output of a previous action is passed to the next one.
Thus the output of a previous action is exactly the same as the input of the next one.

Therefore, the "data flow of a Domino" is the sequence of the input of each action of the Domino,
followed by the output of the Domino.
