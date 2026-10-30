[![Release](https://jitpack.io/v/amaterek/kmp-navigation.svg)](https://jitpack.io/#amaterek/kmp-navigation)

# Kotlin multiplatform navigation library

Kotlin 2.4.20, Compose Multiplatform 1.12.1

## Supported platforms

* Android
* JVM (desktop)
* iOS // Not supported by jitpack.io

## Setup

First add jitpack repository

```gradle
repositories {
    ...
    maven { url 'https://jitpack.io' }
}
```

Then add this to your dependencies

```gradle
dependencies {
    implementation 'com.github.amaterek.kmp-navigation:navigation:{version}'

    // For Jetpack (Navigation 2) implementation
    implementation 'com.github.amaterek.kmp-navigation:jetpack:{version}'

    // For Navigation 3 implementation
    implementation 'com.github.amaterek.kmp-navigation:nav3:{version}'

    // For Voyager implementation (deprecated, will be removed)
    implementation 'com.github.amaterek.kmp-navigation:voyager:{version}'

    // For animated dialogs
    implementation 'com.github.amaterek.kmp-navigation:dialog:{version}'
}
```

## Principles

- Multiplatform (supports Jetpack and Navigation 3 navigators with the same API, and the deprecated Voyager navigator)
- One navigation method `Navigator.navigateTo(destination)` to allow ViewModel/Presenter to decide
  how to navigate.
- Destinations a navigator can navigate to are defined by its graph (`navigationGraph { ... }`).
- Allowed redirects to parent navigator with strategy (`IfNotInCurrentGraph`, `Deep`).
- Operators on `Destination` like `replace`, `replaceAll`, `replaceIfPossible`, `popUpTo`, `withResult`
  and `redirectToParent`.
- Extensions for `Navigator`
  like `navigateBack`, `navigateBackWithResult`, `replace`, `replaceAll`, `popUpTo` etc...

## Usage

### Defining destinations

Destinations are serialized with [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization), so they must
be annotated with `@Serializable`. Serializers of properties with interface types are registered in the graph with
`serializersModule(...)`.

```kotlin
// Destination without argument
@Serializable
data object MyDestination : ScreenDestination {

    @Composable
    override fun Content() {
        // ...
    }
}

// Destination with argument
@Serializable
data class MyDestinationWithArg(val argument: String) : ScreenDestination {

    // Optionally override default screen transition, with a getter so it isn't serialized
    override val transition: ScreenTransition
        get() = SlideHorizontallyScreenTransition

    @Composable
    override fun Content() {
        // ...
    }
}

// Dialog destination
@Serializable
data object MyDialogDestination : DialogDestination {

    @Composable
    override fun Content() {
        // ...
    }

    enum class Result {
        Cancel,
        Ok,
    }
}
```

### Defining graph

```kotlin
val MyGraph = navigationGraph {
    screen<MyDestination>()
    screen<MyDestinationWithArg>()
    // Dialogs must be added with dialog(), which takes their dialog properties
    dialog<MyDialogDestination>(properties = DialogProperties(dismissOnClickOutside = false))
    // Adds the destinations of another graph
    include(FeatureGraph)
}
```

### Create Navigator

```kotlin
@Composable
fun Content(parentNavigator: Navigator) {
    JetpackNavigationHost(
        startDestination = MyDestination,
        graph = MyGraph,
        parent = parentNavigator,
        defaultTransition = FadeScreenTransition,
    )
    // or
    val jetpackNavigator = rememberJetpackNavigator(
        startDestination = MyDestination,
        graph = MyGraph,
        parent = parentNavigator,
        defaultTransition = FadeScreenTransition,
    )
    JetpackNavigationHost(jetpackNavigator)

    // or
    Nav3NavigationHost(
        startDestination = MyDestination,
        graph = MyGraph,
        parent = parentNavigator,
        defaultTransition = FadeScreenTransition,
    )
    // or
    val nav3Navigator = rememberNav3Navigator(
        startDestination = MyDestination,
        graph = MyGraph,
        parent = parentNavigator,
        defaultTransition = FadeScreenTransition,
    )
    Nav3NavigationHost(nav3Navigator)
}
```

The Voyager navigator (`VoyagerNavigationHost`, `rememberVoyagerNavigator`) is deprecated and will be removed: Jetpack
navigation and Navigation 3 are multiplatform now. Replacing it with `JetpackNavigationHost` or `Nav3NavigationHost`
doesn't change the destinations or the navigation.

### Navigation 3 scenes

The Navigation 3 navigator can show several destinations together, e.g. list and detail on wide windows. The graph
adds Navigation 3 entry metadata to destinations (the other navigators ignore it) and `Nav3NavigationHost` takes the
scene strategies that read it. Dialogs are shown above any scene and a destination not handled by the strategies is
shown alone.

```kotlin
val MyGraph = navigationGraph {
    screen<ItemListDestination>(metadata = ListDetailSceneStrategy.listPane())
    screen<ItemDetailDestination>(metadata = ListDetailSceneStrategy.detailPane())
}

Nav3NavigationHost(
    navigator = rememberNav3Navigator(ItemListDestination, MyGraph, parent = null),
    sceneStrategies = listOf(rememberListDetailSceneStrategy<Any>()),
)
```

### Navigation - destination approach

```kotlin
@Composable
fun Content() {
    val navigator = LocalNavigator.current

    with(navigator) {
        navigateTo(MyDestination)
        // Back stack is:
        //   MyDestination

        navigateTo(MyDestinationWithArg("Argument 1").replace())
        // Back stack is:
        //   MyDestinationWithArg("Argument 1")

        navigateTo(MyDestinationWithArg("Argument 2"))
        // Back stack is:
        //   MyDestinationWithArg("Argument 1")
        //   MyDestinationWithArg("Argument 2")

        navigateTo(PopUpToDestination(MyDestinationWithArg("Argument 1")).withResult("Result"))
        // Back stack is:
        //   MyDestinationWithArg("Argument 1") and its Navigator.resultFlow emits "Result"

        navigateTo(ParentDestination.redirectToParent(deep = 2))
        // Redirects ParentDestination to parent's parent

        navigateTo(ParentDestination.redirectToParentIfNotInGraph())
        // Redirects ParentDestination to parent only if ParentDestination isn't in current navigator graph

        navigateTo(PreviousDestination)
        // Redirects PreviousDestination to parent

        navigateTo(NoneDestination)
        // Doesn't navigate, e.g. for a destination that may not be needed

        // ETC ...
    }
}
```

### Navigation - navigator approach

```kotlin
@Composable
fun Content() {
    val navigator = LocalNavigator.current

    with(navigator) {
        navigateTo(MyDestination)
        // Back stack is:
        //   MyDestination

        replace(MyDestinationWithArg("Argument 1"))
        // Back stack is:
        //   MyDestinationWithArg("Argument 1")

        navigateTo(MyDestinationWithArg("Argument 2"))
        // Back stack is:
        //   MyDestinationWithArg("Argument 1")
        //   MyDestinationWithArg("Argument 2")

        popUpToWithResult(MyDestinationWithArg("Argument 1"), result = "Result")
        // Back stack is:
        //   MyDestinationWithArg("Argument 1") and its Navigator.resultFlow emits "Result"

        navigateBack()
        // Redirects PreviousDestination to parent

        // ETC ...
    }
}
```

### Handling results from other destinations (in the same back stack)

When a destination navigates with `PreviousDestination` or `PopUpTo` with `withResult(...)`, the result is
delivered once to the destination it navigates to.

```kotlin
@Composable
fun Content() {
    ObserveNavigationResult { result ->
        // Handle the result by type
        when (result) {
            is MyDialogDestination.Result -> handleResultFromMyDialogDestination(result)
            else -> Unit
        }
    }
    // or collect LocalNavigator.current.resultFlow
}
```

### Handling back

Navigation hosts handle system back (Android back button or gesture, iOS back swipe, Escape on desktop) with
[navigation event](https://developer.android.com/guide/navigation/navigation-event/handle-back): they navigate to
`PreviousDestination` while the navigator can go back, otherwise the back is handled by the parent host or the platform.
`JetpackNavigationHost(navigator, isPredictiveBackEnabled = false)` and
`Nav3NavigationHost(navigator, isPredictiveBackEnabled = false)` pop only when the back gesture completes, without
showing the previous destination during it (e.g. for tabs that aren't a real back stack). The Voyager host always does
that, as Voyager has no predictive back animation.

A destination can handle back itself with `NavigationBackHandler`, which takes precedence over the host:

```kotlin
@Composable
fun Content() {
    val navigator = LocalNavigator.current

    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        onBackCompleted = { navigator.navigateBackWithResult(MyResult.Cancelled) },
    )
}
```

### View models and lifecycle

Every destination in the back stack has its own `ViewModelStoreOwner` and `LifecycleOwner`, with all navigators on
all platforms, so `viewModel()` (also with `SavedStateHandle`) is scoped to the destination: its view models survive
until the destination is removed from the back stack, also while other destinations are displayed above it.

```kotlin
@Serializable
data object MyDestination : ScreenDestination {

    @Composable
    override fun Content() {
        val viewModel = viewModel { MyViewModel(createSavedStateHandle()) }
        // ...
    }
}
```

The lifecycle of a destination is RESUMED when it's on top, STARTED below a dialog and CREATED while another
destination is displayed above it. The Navigation 3 navigator destroys the lifecycle of a destination it doesn't
display and creates a new one when it's displayed again, its view models are kept.

### Animated dialogs

The `dialog` module adds dialogs that animate their content out before they are popped:

- `ClosingDialogDestination` closes in two steps: `close(result)` or `dismiss()` starts closing, and the content calls
  `onClosed()` when its exit animation is finished, which pops the dialog and delivers the result. Navigating back
  with `LocalNavigator` in the content closes the dialog the same way.
- `AnimatedDialogDestination` implements it with content over a scrim. The content enters with `transition.enter` and
  exits with `transition.popExit`, so screen transitions can be used. The scrim (`scrimColor`, `scrimTransition`),
  `contentAlignment`, `dismissOnBackPress` and `dismissOnClickOutside` can be overridden.

They are added to the graph with `animatedDialog()`, which uses full screen dialog properties without the platform's
animation and scrim.

A bottom sheet, for example:

```kotlin
@Serializable
data object MyBottomSheetDestination : AnimatedDialogDestination<MyBottomSheetDestination.Result>() {

    override val contentAlignment: Alignment
        get() = Alignment.BottomCenter

    // Content transition (fade by default)
    override val transition: ScreenTransition
        get() = SlideVerticallyScreenTransition

    // Optionally override the scrim
    override val scrimColor: Color
        get() = Color.Black.copy(alpha = 0.5f)

    @Composable
    override fun ClosingDialogScope<Result>.AnimatedDialogContent() {
        Button(onClick = { close(Result.Ok) }) {
            Text("Ok")
        }
    }

    // Result delivered on back, click outside or navigating back without result
    override fun dismissResult() = Result.Cancel

    enum class Result {
        Cancel,
        Ok,
    }
}

val MyGraph = navigationGraph {
    // ...
    animatedDialog<MyBottomSheetDestination>()
}
```

The closing state isn't saved, so a dialog that is closing when the Android activity is recreated stays open.

### More examples

Please check sample project and tests in the repo.

## Tests

The navigation UI tests are in `commonTest`, so they run with every navigator on all platforms:

- JVM: `./gradlew :navigation:jvmTest :navigation:dialog:jvmTest`
- iOS simulator: `./gradlew -Pbuild.ios=true :navigation:iosSimulatorArm64Test :navigation:dialog:iosSimulatorArm64Test`
- Android device or emulator: `./gradlew :navigation:connectedAndroidDeviceTest :navigation:dialog:connectedAndroidDeviceTest`

State restoration and the animated dialog's back and click outside are tested only on Android (`androidDeviceTest`):
Compose Multiplatform's `StateRestorationTester` isn't implemented on iOS.
