# Shelly实战

本文阐述Shelly库的方法论，以及如何在实战中使用Shelly库。

Shelly库提供了一种全新的编程模式，将业务对象的变化对各个模块的影响通过方法链表示出来。在使用Shelly库时，你不应该按原来的方法构建你的工程。你应该使用一种看似略微丑陋但实际非常有用的模式来构建你的工程。

## 模式

本节阐述在工程中使用Shelly库的模式。

首先，你应该让所有UI组件只进行UI渲染的工作，不进行任何关于业务逻辑的操作。通过Domino实现业务逻辑，Domino根据业务逻辑调用UI组件的函数改变UI。

第二，你应该始终记住，一个Domino对应一条业务逻辑，所以为每条业务逻辑创建一个Domino。

第三，将相似的业务逻辑归为一组，为这个组创建一个Java类。这个Java类创建组内所有的Domino，其他什么都不做。这个Java类可以被看作“配置类”。

第四，在调用某个组内的Domino之前，让对应的配置类创建组内所有的Domino。

第五，当某个业务对象改变时，使用`Shelly.playDomino()`将对象传入并且调用对应的Domino来执行操作并且改变组件。

## 例子

本节给出一个例子来更好地理解上面地模式。

假设现在你要创建一个工程来做一个上传下载图片地app。

第一次打开app会让用户进行注册，以后每次都会让用户登录。登录后就可以使用这个app进行上传和下载图片，使用后可以退出。

我们将展示如何创建这个工程，并且展示关于Shelly库部分地模块。

### 创建Domino

首先，我们将业务逻辑分成两组，一组是关于用户信息的逻辑，比如注册、登录和退出。另一组是关于图片的业务逻辑，包括上传和下载图片。

其次，我们为每个业务逻辑组创建一个配置类，分别是`UserService`和`PictureService`。每个类提供一个方法用来创建Dominoes。

`UserService`如下：

```
public class UserService {

    public static final Object SIGN_UP = new Object();
    public static final Object SIGN_IN = new Object();
    public static final Object SIGN_OUT = new Object();

    public static void init() {

        // Use Retrofit to create the corresponding network interface.
        final UserNetwork userNetwork = ...;

        // Create the Domino for signing up.
        Shelly.<Pair<String, String>>createDomino(SIGN_UP)
                .background()
                .beginRetrofitTaskKeepingInput(new RetrofitTask<Pair<String,String>, String>() {
                    @Override
                    protected Call<String> getCall(Pair<String, String> input) {
                        return userNetwork.signUp(input.first, input.second);
                    }
                })
                .background()
                .onSuccessResult(new Action2<Pair<String, String>, String>() {
                    @Override
                    public void call(Pair<String, String> input1, String token) {
                        // Store the token.
                    }
                })
                .uiThread()
                .onSuccessResult(HomeActivity.class, new TargetAction2<HomeActivity, Pair<String, String>, String>() {
                    @Override
                    public void call(HomeActivity homeActivity, Pair<String, String> input1, String token) {
                        homeActivity.signUp(input1.first, token);
                    }
                })
                .onResponseFailure(HomeActivity.class, new TargetAction2<HomeActivity, Pair<String, String>, Response<String>>() {
                    @Override
                    public void call(HomeActivity homeActivity, Pair<String, String> input1, Response<String> input2) {
                        try {
                            Toast.makeText(homeActivity, input2.errorBody().string(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .background()
                .onFailure(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable input) {
                        Log.e("Eric Zhao", "ERROR", input);
                    }
                })
                .endTask()
                .commit();

        // Create the Domino for signing in.
        Shelly.<Pair<String, String>>createDomino(SIGN_IN)
                // ...
                .commit();

        // Create the Domino for signing out.
        Shelly.<Pair<String, String>>createDomino(SIGN_OUT)
                // ...
                .commit();
    }
}
```

在`init`函数中，我们创建和用户信息相关的所有Domino。另外`UserService`中包含了几个常量，比如`SIGN_IN`、`SIGN_UP`和`SIGN_OUT`。我们把这些常量当作Domino标签。

类似地，我们创建`PictureService`。为了简洁，源码省略。

注意，上面的代码不会执行任何操作！代码做的只是提交和存储Domino供以后使用。要使Domino执行操作，必须调用这个Domino。只有在调用Domino后，它才会执行操作。

### 在调用Domino之前的准备操作

在`Application`类的`onCreate()`中，添加如下代码：

```
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UserService.init();
        PictureService.init();
    }
}
```

在所有的`Activity`的`onCreate`和`onDestroy`分别添加`Shelly.register(this)`和`Shelly.unregister(this)`。

### 调用Domino

如果你想调用Domino，就写`Shelly.playDomino()`。

比如，你在注册按钮的`onClick`函数中写如下代码：

```
String userName = mUserNameEditText.getText().toString();
String password = mPasswordEditText.getText().toString();
Shelly.playDomino(UserService.SIGN_UP, Pair.create(userName, password));
```

注意我们为什么使用常量作为Domino标签，并且把这些标签放入对应的配置类中。用这种方式，我们可以轻易地找到某个特定的Domino在全工程中的哪些地方被调用，只需在IDE中对相应的Domino标签常量进行“Find Usage”操作。

## 总结

上面章节阐述了如何在实战中使用Shelly库。

你可能会发现有一个缺点：由于业务逻辑的复杂，相应的配置类会很长。业务逻辑越复杂，相应的配置类越长。

现在应该改变你传统的思维方式。你不应把配置类当作普通的类。你应该把它当作一个包含所有业务逻辑的配置文件。配置文件可能很长。这个类中包含一组业务逻辑，所以如果逻辑很复杂，类当然很长。所以即使类很长也没有关系。

我们把业务逻辑写在配置类中是有许多优点的：

1. 我们可以看出整个业务逻辑，看出一个业务对象改变后整个app发生了什么变化。

2. 无论业务逻辑怎么修改，我们只需要在单一的配置类中修改代码即可。

3. 因为UI组件只负责UI渲染，这样就变得易于创建和扩展。
