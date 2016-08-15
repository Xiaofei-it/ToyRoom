# 组合Domino

Shelly库提供方法用来合并两个Domino的输出，也可以将两个Domino的输出组合成一个作为下一个action的输入。这非常有用，尤其在使用Retrofit Domino的时候。这些方法让你可以写一个Domino同时发两个HTTP请求并且根据结果的不同执行不同操作。你也可以写一个Domino发送连续请求，在前一个请求返回后发送后一个请求。

## 合并Domino

输出为同一个种类型的Domino可以被合并。如果你合并两个输出类型相同的Domino，你会得到一个新的Domino，这个Domino的输入是前两个的输出的并集。

下面的代码展示了一个Domino，这个Domino从所有的.jpg和.png文件中载入图片：

```
Shelly.<String>createDomino("Find *.jpg")
        .background()
        .map(new Function1<String, File>() {
            @Override
            public File call(String input) {
                return new File(input);
            }
        })
        .flatMap(new Function1<File, List<Bitmap>>() {
            @Override
            public List<Bitmap> call(File input) {
                // Find *.jpg in this folder
                return null;
            }
        })
        .commit();
Shelly.<String>createDomino("Find *.png")
        .background()
        .map(new Function1<String, File>() {
            @Override
            public File call(String input) {
                return new File(input);
            }
        })
        .flatMap(new Function1<File, List<Bitmap>>() {
            @Override
            public List<Bitmap> call(File input) {
                // Find *.png in this folder
                return null;
            }
        })
        .commit();
Shelly.<String>createDomino("Find *.png and *.jpg")
        .background()
        .merge(
            Shelly.<String, Bitmap>getDominoByLabel("Find *.png"),
            Shelly.<String, Bitmap>getDominoByLabel("Find *.jpg")
        )
        .uiThread()
        .perform(new Action1<Bitmap>() {
            @Override
            public void call(Bitmap input) {

            }
        })
        .commit();
```

另外，你可以使用匿名Domino来实现同样效果：

```
Shelly.<String>createDomino("Find *.png and *.jpg")
        .background()
        .merge(
            Shelly.<String>createAnonymousDomino()
                    .background()
                    .map(new Function1<String, File>() {
                        @Override
                        public File call(String input) {
                            return new File(input);
                        }
                    })
                    .flatMap(new Function1<File, List<Bitmap>>() {
                        @Override
                        public List<Bitmap> call(File input) {
                            // Find *.jpg in this folder
                            return null;
                        }
                    }),
            Shelly.<String>createAnonymousDomino()
                    .background()
                    .map(new Function1<String, File>() {
                        @Override
                        public File call(String input) {
                            return new File(input);
                        }
                    })
                    .flatMap(new Function1<File, List<Bitmap>>() {
                        @Override
                        public List<Bitmap> call(File input) {
                            // Find *.png in this folder
                            return null;
                        }
                    })
        )
        .uiThread()
        .perform(new Action1<Bitmap>() {
            @Override
            public void call(Bitmap input) {

            }
        })
        .commit();
```

## 组合Domino

你可以将两个Domino组合成一个。具体说，假设有Domino A和Domino B，你提供一个函数f，组合后得到Domino C。具体组合方式是：前两个Domino的输出传入f，f返回新的对象作为Domino C的输入。

下面是一个例子，这个例子展示了如何使用Shelly库同时发两个HTTP请求：

```
Shelly.<String>createDomino("Login")
        .combine(
            Shelly.<String>createAnonymousDomino()
                    .beginRetrofitTask(new RetrofitTask<String, User>() {
                        @Override
                        protected Call<User> getCall(String s) {
                            return network.getUser(s);
                        }
                    })
                    .endTask(),
            Shelly.<String>createAnonymousDomino()
                    .beginRetrofitTask(new RetrofitTask<String, Summary>() {
                        @Override
                        protected Call<Summary> getCall(String s) {
                            return network.getSummary(s);
                        }
                    })
                    .endTask(),
            new Function2<User, Summary, Internal>() {
                @Override
                public Internal call(User input1, Summary input2) {
                    return null;
                }
            }
        )
        .perform(new Action1<Internal>() {
            @Override
            public void call(Internal input) {

            }
        })
        .commit();
```

## 在Domino中调用其他Domino

Shelly库提供函数让你在一个Domino中调用其他Domino。

下面是一个例子，展示了如何使用Shelly库发送两个连续请求（在第一个请求返回结果后，发送第二个请求）：

```
Shelly.<String>createDomino("Login")
        .beginRetrofitTask(new RetrofitTask<String, User>() {
            @Override
            protected Call<User> getCall(String s) {
                return network.getUser(s);
            }
        })
        .onSuccessResult(
            Shelly.<User>createAnonymousDomino()
                    .beginRetrofitTask(new RetrofitTask<User, Summary>() {
                        @Override
                        protected Call<Summary> getCall(User user) {
                            return network.getSummary(user.getId());
                        }
                    })
                    .onSuccessResult(new Action1<Summary>() {
                        @Override
                        public void call(Summary input) {

                        }
                    })
                    .endTask()
        )
        .endTask()
        .commit();
```
