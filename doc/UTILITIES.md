# Utilities

[Chinese Translation 中文翻译](https://github.com/Xiaofei-it/Shelly/blob/master/doc-zh-cn/UTILITIES.md)

## Tuple input

You may find that in the previous examples, the type of the input of all the Dominoes is a single type.
You can pass an integer to the Domino, or a double.

See an example:

```
Shelly.<Integer>createDomino("Print")
        .perform(new Action1<Integer>() {
            @Override
            public void call(Integer input) {
                System.out.println(input);
            }
        })
        .commit();
```

If you invoke the Domino by `Shelly.playDomino("Print", 3)`, the Shelly library prints "3".

If you invoke the Domino by `Shelly.playDomino("Print", 3, 5)`, the Shelly library prints "3" and "5".

What should you do if you want to pass two integer and a double to a particular Domino at the same time?
For example, if you want to create a Domino which take two integers and a double as input and
print the sum of the three numbers.

The Shelly library provides you with some tuple classes, in which you can put multiple objects.

To print the sum of two integers and a double, you write the following to create the Domino:

```
Shelly.<Triple<Integer, Integer, Double>>createDomino("Add")
        .map(new Function1<Triple<Integer,Integer,Double>, Double>() {
            @Override
            public Double call(Triple<Integer, Integer, Double> input) {
                return input.first + input.second + input.third;
            }
        })
        .perform(new Action1<Double>() {
            @Override
            public void call(Double input) {
                System.out.print(input);
            }
        })
        .commit();
```

Invoke the Domino with the following statement:

```
Shelly.playDomino("Add", Triple.create(1, 2, 3.0));
```

The Shelly library provides you with many tuple classes, which are contained in the package
`xiaofei.library.shelly.tuple`. You can create a tuple by `XXX.create(...)` rather than using the
constructor of a tuple class.

## Stash

Sometimes, in an action performed by a Domino, you want to save something for the following action to use.

In this case, use the stash actions or stash functions, which are contained in the package
`xiaofei.library.shelly.function.stashfunction`. These actions and functions are the same as their
corresponding actions and functions except that they provide additional methods for stashing, with
which you can stash data for later use.

The following is an example:

```
Shelly.<String>createDomino("Stash example")
        .perform(new StashAction1<String>() {
            @Override
            public void call(String input) {
                // Store a String with the key "1".
                stash(1, input + " test");
            }
        })
        .perform(new StashAction1<String>() {
            @Override
            public void call(String input) {
                // Get the String with the key "1".
                System.out.println(get(1));
            }
        })
        .commit();
```

Note that if you invoke the Domino by `Shelly.playDomino("Stash example", "INPUT")`, it will work well.
But if you invoke the Domino by `Shelly.playDomino("Stash example", "INPUT1", "INPUT2")`, something
may go wrong:
After the first action is performed, there is only one value ("INPUT2 test") in the stash.

Why does this happen? The Shelly library performs the actions one after the other. When performing
the first action, "INPUT1" is passed in and "INPUT1 test" is stashed. But after this, "INPUT2" is
passed in and "INPUT2 test" replaces the previous one.
Only after finishing performing the first action will the Shelly library perform the second action.

So, what should we do to avoid the value replacement?

You can use another method of the stash:

```
Shelly.<String>createDomino("Stash example 2")
        .perform(new StashAction1<String>() {
            @Override
            public void call(String input) {
                // Store the String with two keys,
                // the first of which is "First" and the second of which is input.
                stash("First", input, input + " First");
            }
        })
        .perform(new StashAction1<String>() {
            @Override
            public void call(String input) {
                // Store the String with two keys,
                // the first of which is "Second" and the second of which is input.
                stash("Second", input, input + "Second");
            }
        })
        .perform(new StashAction1<String>() {
            @Override
            public void call(String input) {
                // Get the String with two keys.
                System.out.println(get("First", input));
                System.out.println(get("Second", input));
            }
        })
        .commit();
```