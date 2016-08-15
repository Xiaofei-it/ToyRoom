# More kinds of Dominoes

[Chinese Translation 中文翻译](https://github.com/Xiaofei-it/Shelly/blob/master/doc-zh-cn/MORE_DOMINOES.md)

The Domino class provides many basic methods. Also you can write derived Dominoes which extend the
class. In the Shelly library, there are already several kinds of derived Dominoes, which are shown
below.

## Task Domino

The Task Domino provides methods for executing a time-consuming task and performing various
kinds of actions according to the result or the failure of the task execution. The usage of a Task
Domino makes the source code concerning the business logic of your app clear and easy to understand.

The following is an example of
[Task Domino](https://github.com/Xiaofei-it/Shelly/blob/master/shelly/src/main/java/xiaofei/library/shelly/domino/TaskDomino.java):

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

You may find that after the execution of the task, the result or the exception will be passed to
the following actions, but the original input of the task has been lost. Sometimes we need to know
the original input in the following actions. To pass the original input to the following actions,
you can execute a task using another method:

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

Note that `TaskDomino.endTask()` will keep the result of the task, thus you can perform more actions
with the result after `endTask()`. See the above for example.

## Retrofit Domino

The Retrofit Domino provides a convenient pattern for sending an HTTP request and performing
various kinds of actions according to the result or the failure of the request. The
Retrofit Task is very useful in the development of an app, which takes many advantages over the other
architectures for sending HTTP requests.

Suppose that we want to use Retrofit to send an HTTP request to get the user information:

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


See [RetrofitDomino](https://github.com/Xiaofei-it/Shelly/blob/master/shelly/src/main/java/xiaofei/library/shelly/domino/RetrofitDomino.java)
for more APIs of the Retrofit Domino.

Also, you may find that after the execution of the request, the result or the exception will be
passed to the following actions, but the original input of the task has been lost. Sometimes we
need to know the original input in the following actions. To pass the original input to the following
actions, you can sending an HTTP request using another method:

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


The above Domino is RetrofitDomino2. See [RetrofitDomino2](https://github.com/Xiaofei-it/Shelly/blob/master/shelly/src/main/java/xiaofei/library/shelly/domino/RetrofitDomino2.java)
for more APIs of the RetrofitDomino2 class.
