# Shelly in Action

[Chinese Translation 中文翻译](https://github.com/Xiaofei-it/Shelly/blob/master/doc-zh-cn/METHODOLOGY.md)

This article illustrates the methodology of using the Shelly library and how to use the Shelly library
in action.

The Shelly library provides a novel pattern which uses a method chain to illustrate how each component
varies with a business object. When using the Shelly library, you should never compose your project
as before. Instead you should use a novel pattern, which appears to be a bit ugly but is actually
useful.

## Pattern

This section illustrates the pattern for using the Shelly library in your project.

First of all, you should make all the UI components perform only the job of rendering the UI rather than
performing actions concerning the business logic. And you should write Dominoes to invoke the methods
in UI components to change them according to the business logic.

Second, you should always remember that each Domino corresponds to a particular piece of
business logic. So create a Domino for each piece of business logic.

Third, put a set of "similar" pieces of business logic into a group. Create a Java class corresponding to
the group. The Java class does nothing but creating the Dominoes of the group. This Java class is
regarded as a "Configuration Class".

Fourth, before the invocation of a Domino of a particular group, cause the corresponding configuration
class to create all the Dominoes of the group.

Fifth, when a particular business object is changed, use `Shelly.playDomino()` to pass the object
to and invoke the corresponding Domino to perform the specified actions to change the components.


## Example

This section gives an example for a better understanding of the pattern.

Suppose that you will create a new project of an app for uploading and downloading pictures.

The first time you start the app, you should sign up.
The next time you start the app, you can sign in.
After that, you can upload or download pictures.
You can also sign out after finishing your job.

The following will illustrate not how to write the whole project but how to write the parts with
respect to the Shelly library.

### Domino creation

First, we divides all of the business logic into two groups. One contains the business logic concerning
the user information, such as signing up, signing in and signing out. The other one contains the
business logic concerning the pictures, such as uploading and downloading pictures.

Second, we create two configuration classes corresponding to the two groups of business logic: `UserService`
and `PictureService` respectively. Each class provides a method for creating Dominoes.

The following is `UserService`:

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

In the `init` method, we create the Dominoes for all the business logic concerning the user information.
Also, `UserService` contains some constants, such as `SIGN_IN`, `SIGN_UP` and `SIGN_OUT`. We regard
these constants as Domino labels.

Similarly, we create `PictureService`. The source code is not given here for simplicity.

Note that the above code will not perform any actions! What the code does is simply commit and
store the Domino for later use. To make the Domino perform actions, you should invoke the Domino.
Only after the Domino is invoked will it perform actions.

### Preparation for Domino invocation

In the `onCreate()` of the `Application` class, we write the following code:

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

In each `onCreate` and `onDestroy` of all the `Activity`s, add `Shelly.register(this)` and `Shelly.unregister(this)`.

### Domino invocation

Whenever you want to invoke a particular Domino, write `Shelly.playDomino()`.

For example, you can write the following in the `onClick` method for signing up:

```
String userName = mUserNameEditText.getText().toString();
String password = mPasswordEditText.getText().toString();
Shelly.playDomino(UserService.SIGN_UP, Pair.create(userName, password));
```

Note the reason why we use constants for Domino labels and put all of the labels in the corresponding
configuration class. In this way, we can easily find all of the Domino invocation of a particular
Domino in the whole project, simply by using the IDE to find all the usages of the corresponding
constant.

## Summary

The above illustrates how to use the Shelly library in action.

There exists one disadvantage: a particular configuration class corresponding to a group
of business logic may be very long because of the complexity of the business logic. The more complex,
the longer the class will be.

Now change your traditional opinion. You should not regard the configuration class as
a traditional class. Instead you should regard it as a configuration file containing all of the
corresponding business logic. A configuration file may be very long.
And the class contains a group of business logic, so if the business logic is complex,
the class is long for sure. So feel free if the class is extremely long.

There exists several advantages if we write all the business logic in configuration classes:

1. We can see the whole business logic, especially what happens to the whole app after
a particular business object changes.

2. Whenever the business logic is modified, we only need to modify the source code in a single
configuration class.

3. Because UI components are responsible for only the UI rendering, it is flexible to compose them.
