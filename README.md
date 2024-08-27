[![Release](https://jitpack.io/v/amaterek/kmp-navigation.svg)](https://jitpack.io/#amaterek/kmp-navigation)

# Kotlin multiplatform navigation library

Kotlin version: 1.9.22

## Supported platforms

* Android
* JVM
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

    // For Jetpack implementation
    implementation 'com.github.amaterek.kmp-navigation:jetpack:{version}'

    // For Voyager implementation
    implementation 'com.github.amaterek.kmp-navigation:voyager:{version}'
}
```

## Principles

- Multiplatform (supports Voyager and JetPack navigators)
- One navigation method `Navigator.navigateTo(destination)` to allow ViewModel/Presenter to decide
  how to navigate.
- Allowed redirects to parent navigator wit strategy (`IfNotInCurrentGraph`, `Deep`).
- Operators on `Destination` like `replace`, `replaceAll`, `popUpTo`, `withRrsult`
  and `redirectToParent`.
- Extensions for `Navigator`
  like `navigateBack`, `navigateBackWithResult`, `replace`, `replaceAll`, `popUpTo` etc...

## Usage

### Defining destinations

```kotlin
// Destination without argument
@Serialize
data object MyDestination : ScreenDestination {

    @Composable
    override fun Content() {
        // ...
    }
}

// Destination with argument
@Serialize
data class MyDestinationWithArg(val argument: String) : ScreenDestination,
    ScreenTransitionProvider {

    // Optionally override default screen transition
    override val transition: ScreenTransition
        get() = Companion.transition

    @Composable
    override fun Content() {
        // ...
    }

    // Optionally override default screen transition
    // NOTE: This is required for JetPackNavigator
    companion object : ScreenTransitionProvider {
        override val transition: ScreenTransition = SlideHorizontallyScreenTransition
    }
}

// Dialog destination
@Serialize
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

### Create Navigator

```kotlin
@Composable
fun Content(parentNavigator: Navigator) {
    JetpackNavigationHost(
        startDestination = StartDestination,
        graph = setOf(StartDestination::class, /* ... */),
        parent = parentNavigator,
        defaultTransition = FadeScreenTransition,
    )
    // or
    val jetpackNavigator = rememberJetpackNavigator(
        startDestination = StartDestination,
        graph = setOf(StartDestination::class, /* ... */),
        parent = parentNavigator,
        defaultTransition = FadeScreenTransition,
    )
    JetpackNavigationHost(jetpackNavigator)

    // or
    VoyagerNavigationHost(
        startDestination = StartDestination, // or listOf(StartDestination, ...),
        graph = setOf(StartDestination::class, /* ... */),
        parent = parentNavigator,
        defaultTransition = FadeScreenTransition,
    )
    // or
    val voyagerNavigator = rememberVoyagerNavigator(
        startDestination = StartDestination, // or listOf(StartDestination, ...),
        graph = setOf(StartDestination::class, /* ... */),
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
        navgateTo(MyDestination)
        // Back stack is: 
        //   MyDestination

        navgateTo(MyDestinationWithArg("Argument 1").replace())
        // Back stack is: 
        //   MyDestinationWithArg("Argument 1")

        navgateTo(MyDestinationWithArg("Argument 2"))
        // Back stack is: 
        //   MyDestinationWithArg("Argument 1") 
        //   MyDestinationWithArg("Argument 2")

        navgateTo(PopUpToDestination(MyDestinationWithArg("Argument 1")).withResult("Result"))
        // Back stack is:
        //   MyDestinationWithArg("Argument 1") and Navigator.resultFlow emits "Result"

        navgateTo(ParentDestination.redirectToParent(deep = 2))
        // Redirects ParentDestination to parent's parent

        navgateTo(ParentDestination.redirectToParentIfNotInGraph())
        // Redirects ParentDestination to parent only if ParentDestination isn't in current navigator graph

        navgateTo(PreviousDestination)
        // Redirects PreviousDestination to parent

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
        navgateTo(MyDestination)
        // Back stack is: 
        //   MyDestination

        replace(MyDestinationWithArg("Argument 1"))
        // Back stack is: 
        //   MyDestinationWithArg("Argument 1")

        navgateTo(MyDestinationWithArg("Argument 2"))
        // Back stack is: 
        //   MyDestinationWithArg("Argument 1") 
        //   MyDestinationWithArg("Argument 2")

        popUpToWithResult(MyDestinationWithArg("Argument 1"), result = "Result")
        // Back stack is:
        //   MyDestinationWithArg("Argument 1") and Navigator.resultFlow emits "Result"

        navigateBack()
        // Redirects PreviousDestination to parent

        // ETC ...
    }
}
```

### Handling results from other destinations (in  the same backstack)

```kotlin
@Composable
fun Content() {
    val navigator = LocalNavigator.current
    val navigationResultFlow by LocalNavigator.current.resultFlow

    LaunchedEffect(navigator) {
        // When destination used PreviousDestination or PopUpTo with operator 'withResult(...)'
        // the result will be emitted by the flow if this destination i at the to of the backstack.
        navigationResultFlow
            .onEach {
                // Handle the result by type
                when (it) {
                    is MyDialogDestination.Result -> handleResultFromMyDialogDestination(it)
                    else -> Unit
                }
            }
            .launchIn(this)
    }
}
```

### More examples

Please check sample project and instrumented tests in the repo.