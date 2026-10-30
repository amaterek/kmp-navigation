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

    // For Voyager implementation
    implementation 'com.github.amaterek.kmp-navigation:voyager:{version}'
}
```

## Principles

- Multiplatform (supports Voyager and Jetpack navigators)
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
    VoyagerNavigationHost(
        startDestination = MyDestination, // or listOf(MyDestination, ...),
        graph = MyGraph,
        parent = parentNavigator,
        defaultTransition = FadeScreenTransition,
    )
    // or
    val voyagerNavigator = rememberVoyagerNavigator(
        startDestination = MyDestination, // or listOf(MyDestination, ...),
        graph = MyGraph,
        parent = parentNavigator,
        defaultTransition = FadeScreenTransition,
    )
    VoyagerNavigationHost(voyagerNavigator)
}
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
`JetpackNavigationHost(navigator, isPredictiveBackEnabled = false)` pops only when the back gesture completes, without
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

### More examples

Please check sample project and instrumented tests in the repo.
