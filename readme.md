
# Router Annotations  [![Release](https://jitpack.io/v/talhahasanzia/router-annotations.svg)](https://jitpack.io/#talhahasanzia/router-annotations/0.2)  [![GitHub issues](https://img.shields.io/github/issues/talhahasanzia/router-annotations.svg)](https://github.com/talhahasanzia/router-annotations/issues)   [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
*Dont Repeat Yourself!*


A simple annotation that generates a "Router" class for providing a consistent way to navigate to Activity in Android.




### Advantages
- Just add an annotation of [Routeable](https://github.com/talhahasanzia/router-annotations/blob/master/annotation/src/main/java/com/talhahasanzia/annotation/Routeable.java) to your Activity class to get started.
- No need to write startActivity again and again.
- Consistent suffix 'Router' like MainActivity -> MainActivityRouter to identify it quickly.
- Just call route methods to navigate to acitivty with several options like - will current activity finish, intent extras etc.
- Call routeForResult() methods to start Activity for result.
- Fully customizable intents now supported (since v0.2+)! Pass IntentModifier object in overloaded methods customize intent object that library is using.



## Release
Available Version:  [0.2](https://github.com/talhahasanzia/router-annotations/releases/tag/0.2) on [jitpack.io](https://jitpack.io/#talhahasanzia/router-annotations/0.2)


## Library Source
[Jump to annotation processor source.](https://github.com/talhahasanzia/router-annotations/blob/master/processor/src/main/java/com/talhahasanzia/processor/RouteProcessor.java)

## Getting Started

### Adding the library

In your project level gradle, add:
```
    maven { url "http://jitpack.io" }
```

In your app level gradle **(4.0+)**, add:
```
    implementation 'com.github.talhahasanzia:router-annotations:0.2'
```
for gradle versions **below 4.0** use:
```
    compile 'com.github.talhahasanzia:router-annotations:0.2'
```
## Using in your project
- Consider you have MainActivity, and you want to navigate to SecondAcitivity, first add @Routeable annotation to SecondAcitvity simply like:
```
@Routeable
public class SecondActivity extends AppCompatActivity
 {
 ...
 }
  
 ```
- **Build your project**
- Now, you can simply call SecondActivityRouter methods from MainActivity (or from Anywhere in the project) like:

```
   // route to second activity
   SecondActivityRouter.route(MainActivity.this);
   
   // route to second activity, and finish this current one
   SecondActivityRouter.route(MainActivity.this, true);
   
   // route with some serialized data
   SecondActivityRouter.route(MainActivity.this, "myData", (Serializable) extraData);
   
   // route with some parcelable data
   SecondActivityRouter.route(MainActivity.this, "myData", myParcelable);

   // modify intent example
   SecondActivityRouter.route(this, intent -> {
       // incoming intent
       // modify it
       intent.putExtra("dummyFloat", 3.142799F);
       // return it.
       return intent;  // now the router will have your customized intent and will use this object in further calls
   });
  
```

- Feel free to report issues and contribute.
  


## Contributing

- Contributions are welcomed as long as they dont break the code. Please create an issue and have a discussion before pull request.
- There is still WIP so don't hesitate to report issues or pull requests.
- Also, if you created a skin based on this library you can create a pull request and we will add it in official release.


## Hosting

Thanks to jitpack.io! Hosted at: https://jitpack.io/#talhahasanzia/router-annotations/

## Authors

* **Talha** - *Initial work* - [@talhahasanzia](https://github.com/talhahasanzia)

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](https://github.com/talhahasanzia/router-annotations/blob/master/LICENSE) file for details.

*Sources from Android and Android APIs are subject to the licensing terms as per Android Open Source Project (AOSP).*


## Code Credits
- MyParcelabe class from this [blog](https://guides.codepath.com/android/using-parcelable)
