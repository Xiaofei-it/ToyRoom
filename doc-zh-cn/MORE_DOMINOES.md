# Domino种类

Domino类提供了许多基础函数。你也可以创建自己的Domino子类来扩展Domino。Shelly库中，已经有几个衍生类，将在下面展示。

## Task Domino

Task Domino提供函数来执行耗时操作并且根据结果执行各种操作。使用Task Domino可以使app的业务逻辑清晰并且易于理解。

下面是[Task Domino](https://github.com/Xiaofei-it/Shelly/blob/master/shelly/src/main/java/xiaofei/library/shelly/domino/TaskDomino.java)的例子：

```
// Create a domino labeled "LoadingBitmap" which takes a String as input,
// indicating is the path of a bitmap.
Shelly.<String>createDomino("LoadingBitmap")
        // The following actions will be performed in background.
        .background()
        // Execute a task which loads a bitmap according to the path.
        .beginTask(new Task<String, Bitmap, Exception>() {
            private Bitmap load(String path) throws IOException {
                if (path == null) {
                    throw new IOException();
                } else {
                    return null;
                }
            }

            @Override
            protected void onExecute(String input) {
                // We load the bitmap.
                // Remember to call Task.notifySuccess() or Task.notifyFailure() in the end.
                // Otherwise, the Domino gets stuck here.
                try {
                    Bitmap bitmap = load(input);
                    notifySuccess(bitmap);
                } catch (IOException e) {
                    notifyFailure(e);
                }
            }
        })

        // The following performs different actions according to the result or the failure
        // of the task.

        // If the execution of the above task succeeds, perform an action.
        .onSuccess(new Action0() {
            @Override
            public void call() {
                // Do something.
            }
        })
        // If the execution of the above task succeeds,
        // perform an action which takes a bitmap as input.
        .onSuccess(new Action1<Bitmap>() {
            @Override
            public void call(Bitmap input) {
                // Do something.
            }
        })
        // The following actions will be performed in the main thread, i.e. the UI thread.
        .uiThread()
        // If the execution of the above task succeeds,
        // perform an action on all registered instances of ImageView.
        .onSuccess(ImageView.class, new TargetAction1<ImageView, Bitmap>() {
            @Override
            public void call(ImageView imageView, Bitmap input) {
                // Do something.
            }
        })
        // The following actions will be performed in background.
        .background()
        // If the execution of the above task fails, perform an action.
        .onFailure(new Action0() {
            @Override
            public void call() {
                // Do something.
            }
        })
        // If the execution of the above task fails, print the stack trace fo the exception.
        .onFailure(new Action1<Exception>() {
            @Override
            public void call(Exception input) {
                input.printStackTrace();
            }
        })
        // If the execution of the above task fails,
        // perform an action on all registered instances of ImageView.
        .onFailure(ImageView.class, new TargetAction1<ImageView, Exception>() {
            @Override
            public void call(ImageView imageView, Exception input) {
                // Do something.
            }
        })
        .endTask()
        .commit();
```

你可能会发现在task被执行后，执行的结果或异常会被传入之后的action，但是原始输入丢失了。某些时候在后面的action中我们也许会用到原始输入。为了将原始输入也传入后面的action，你可以使用另一个函数执行task：

```
// Create a domino labeled "LoadingBitmap" which takes a String as input,
// indicating is the path of a bitmap.
Shelly.<String>createDomino("LoadingBitmap 2")
        // The following actions will be performed in background.
        .background()
        // Execute a task which loads a bitmap according to the path.
        .beginTaskKeepingInput(new Task<String, Bitmap, Exception>() {
            private Bitmap load(String path) throws IOException {
                if (path == null) {
                    throw new IOException();
                } else {
                    return null;
                }
            }
            @Override
            protected void onExecute(String input) {
                // We load the bitmap.
                // Remember to call Task.notifySuccess() or Task.notifyFailure() in the end.
                // Otherwise, the Domino gets stuck here.
                try {
                    Bitmap bitmap = load(input);
                    notifySuccess(bitmap);
                } catch (IOException e) {
                    notifyFailure(e);
                }
            }
        })
        // The following performs different actions according to the result or the failure
        // of the task.

        // If the execution of the above task succeeds, perform an action.
        .onSuccess(new Action0() {
            @Override
            public void call() {
                // Do something.
            }
        })
        // If the execution of the above task succeeds,
        // perform an action which takes a bitmap as input.
        .onSuccess(new Action1<Pair<String, Bitmap>>() {
            @Override
            public void call(Pair<String, Bitmap> input) {

            }
        })
        // The following actions will be performed in the main thread, i.e. the UI thread.
        .uiThread()
        // If the execution of the above task succeeds,
        // perform an action on all registered instances of ImageView.
        .onSuccess(ImageView.class, new TargetAction1<ImageView, Pair<String,Bitmap>>() {
            @Override
            public void call(ImageView imageView, Pair<String, Bitmap> input) {

            }
        })
        // The following actions will be performed in background.
        .background()
        // If the execution of the above task fails, perform an action.
        .onFailure(new Action0() {
            @Override
            public void call() {
                // Do something.
            }
        })
        // If the execution of the above task fails, print the stack trace fo the exception.
        .onFailure(new Action1<Exception>() {
            @Override
            public void call(Exception input) {
                input.printStackTrace();
            }
        })
        // If the execution of the above task fails,
        // perform an action on all registered instances of ImageView.
        .onFailure(ImageView.class, new TargetAction1<ImageView, Exception>() {
            @Override
            public void call(ImageView imageView, Exception input) {
                // Do something.
            }
        })
        .endTask()
        // Now the task ends, but the result remains. You can do more in the following.
        .perform(new Action1<Pair<String, Bitmap>>() {
            @Override
            public void call(Pair<String, Bitmap> input) {

            }
        })
        .commit();
```

注意`TaskDomino.endTask()`会把task的结果保留下来，你可以在｀endTask()`之后利用这个结果执行更多的action，参看上面的例子。

## Retrofit Domino

Retrofit Domino提供一种方便的模式用来发送HTTP请求并且根据请求的不同结果进行不同的回调操作。在app开发中使用Retrofit Domino非常有效，相比其他框架有许多优点。

假设我们要发送HTTP请求获取用户信息：

```
Shelly.<String>createDomino("GETTING_USER")
        .background()
        // Return a call for the Retrofit task.
        .beginRetrofitTask(new RetrofitTask<String, User>() {
            @Override
            protected Call<User> getCall(String s) {
                return network.getUser(s);
            }
        })
        .uiThread()
        // If the request succeed and we get the user information,
        // perform an action.
        .onSuccessResult(new Action0() {
            @Override
            public void call() {

            }
        })
        // If the request succeed and we get the user information,
        // perform an action on MyActivity.
        .onSuccessResult(MyActivity.class, new TargetAction1<MyActivity, User>() {
            @Override
            public void call(MyActivity mainActivity, User input) {

            }
        })
        // If the request succeed but we get an error from the server,
        // perform an action.
        .onResponseFailure(new Action0() {
            @Override
            public void call() {

            }
        })
        // If the request succeed but we get an error from the server,
        // perform an action on MyActivity.
        .onResponseFailure(MyActivity.class, new TargetAction1<MyActivity, Response<User>>() {
            @Override
            public void call(MyActivity myActivity, Response<User> input) {

            }
        })
        // If the request fails, perform an action.
        .onFailure(new Action1<Throwable>() {
            @Override
            public void call(Throwable input) {

            }
        })
        // If the request fails, perform an action on MyActivity.
        .onFailure(MyActivity.class, new TargetAction1<MyActivity, Throwable>() {
            @Override
            public void call(MyActivity myActivity, Throwable input) {

            }
        })
        .endTask()
        .commit();
```

参见[RetrofitDomino](https://github.com/Xiaofei-it/Shelly/blob/master/shelly/src/main/java/xiaofei/library/shelly/domino/RetrofitDomino.java)了解Retrofit Domino的更多API。

同样的，你也会发现在请求执行后，结果或异常被传入后续的action，但原始输入丢失了。有些时候在后面的action中我们可能会用到原始输入。为了将原始输入传入后面的action，你可以使用另一个函数发送请求：

```
Shelly.<String>createDomino("GETTING_USER")
        .background()
        // Return a call for the Retrofit task.
        .beginRetrofitTaskKeepingInput(new RetrofitTask<String, User>() {
            @Override
            protected Call<User> getCall(String s) {
                return network.getUser(s);
            }
        })
        .uiThread()
        // If the request succeed and we get the user information,
        // perform an action.
        .onSuccessResult(new Action0() {
            @Override
            public void call() {

            }
        })
        // If the request succeed and we get the user information,
        // perform an action on MyActivity.
        .onSuccessResult(MyActivity.class, new TargetAction2<MyActivity, String, User>() {
            @Override
            public void call(MyActivity myActivity, String input1, User input2) {

            }
        })
        // If the request succeed but we get an error from the server,
        // perform an action.
        .onResponseFailure(new Action0() {
            @Override
            public void call() {

            }
        })
        // If the request succeed but we get an error from the server,
        // perform an action on MyActivity.
        .onResponseFailure(MyActivity.class, new TargetAction2<MyActivity, String, Response<User>>() {
            @Override
            public void call(MyActivity myActivity, String input1, Response<User> input2) {

            }
        })
        // If the request fails, perform an action.
        .onFailure(new Action1<Throwable>() {
            @Override
            public void call(Throwable input) {

            }
        })
        // If the request fails, perform an action on MyActivity.
        .onFailure(MyActivity.class, new TargetAction1<MyActivity, Throwable>() {
            @Override
            public void call(MyActivity myActivity, Throwable input) {

            }
        })
        .endTask()
        .commit();
```

上面这个Domino是RetrofitDomino2。参看[RetrofitDomino2](https://github.com/Xiaofei-it/Shelly/blob/master/shelly/src/main/java/xiaofei/library/shelly/domino/RetrofitDomino2.java)获取RetrofitDomino2的更多API。
