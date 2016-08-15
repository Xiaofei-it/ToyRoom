# Domino Combination

[Chinese Translation 中文翻译](https://github.com/Xiaofei-it/Shelly/blob/master/doc-zh-cn/UTILITIES.md)

The Shelly library provides methods for merging the outputs of two Dominoes and combing two
outputs of two Dominoes into one input for the following action,
which is useful especially when it comes to the Retrofit
Domino. These methods allow you to write a Domino which sends two HTTP requests at the same time
and uses the results of the two requests to perform actions. Also, you can write a Domino which
sends an HTTP request and after getting its result, sends another request.

## Merging of Dominoes

Dominoes whose output are of the same type can be merged.
 If you merge two Dominoes whose output are of the same type, then you get
a new Domino whose input is the union of their output.

The following shows a Domino which loads the bitmaps from all of the .jpg and .png files:

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

Also, you can write the following example using anonymous Dominoes:

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

## Combination of Dominoes

You can combine two Dominoes into one. Specifically, suppose there are two Dominoes named "Domino A"
and "Domino B", and you can provide a function named "f" and combine these two Dominoes into a new Domino
named "Domino C" in the following way: The outputs of the two Dominoes are passed into "f" and "f"
returns new objects as the input of Domino C.

The following is an example, which shows how to use the Shelly library to send two HTTP requests at
the same time:

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

## Domino invocation within a Domino

The Shelly library provides the methods for invoking Dominoes within a Domino.

The following is an example, which shows how to use the Shelly library to send a request and after
getting the result, send another request:

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
