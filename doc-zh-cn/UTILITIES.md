# 工具类

## Tuple输入

你可能注意到之前的例子中，Domino的输入类型是单一类型，你可以给Domino传入一个Integer或者Double。

看这个例子：

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

如果通过`Shelly.playDomino("Print", 3)`调用Domino，那么打印“3”。

如果通过`Shelly.playDomino("Print", 3, 5)`调用Domino，那么打印“3”和“5”。

如果要同时给某个Domino传入两个integer和一个double，那么我们要做什么？比如，你要给一个Domino传入两个integer和一个double，打印这三个数的和。

Shelly库提供了tuple类，支持多输入。

要打印两个integer和一个double的和，你可以创建如下Domino：

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

使用下面的语句调用Domino：

```
Shelly.playDomino("Add", Triple.create(1, 2, 3.0));
```

Shelly库提供了很多tuple类，位于`xiaofei.library.shelly.tuple`包内。你可以通过`XXX.create(...)`创建相应的tuple，不要使用tuple的构造器。

## Stash

某些时候，在一个Domino的一个action中，你可能想保存一些东西供下面的action使用。

在这种情况下，可以使用stash action或者stash function，这些类位于`xiaofei.library.shelly.function.stashfunction`包内。这些action和function与他们对应的原始action和function有相同的功能，只是提供了额外的方法供存取数据。

下面是一个例子：

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

注意，如果你通过`Shelly.playDomino("Stash example", "INPUT")`调用Domino，一切正常。但是如果你通过`Shelly.playDomino("Stash example", "INPUT1", "INPUT2")`调用Domino，就会有问题：在第一个action被执行后，stash中只存在一个值：“INPUT2 test”。

这是为什么呢？Shelly库按顺序一个个执行action。执行第一个action的时候，“INPUT1”被传入，“INPUT1 test”被存储。之后，“INPUT2”被传入，“INPUT2 test”便替换了原来的“INPUT1 test”。只有执行完第一个action后，Shelly库才会执行第二个action。

那么，我们怎么做才可以不发生替换呢？

你可以使用stash的另一个函数：

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