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
- One navigation method `Navigator.navigateTo(destination)` to allow ViewModel/Presenter to decide how to navigate.
- Allowed redirects to parent navigator wit strategy (`IfNotInCurrentGraph`, `Deep`).
- Operators on `Destination` like `replace`, `replaceAll`, `popUpTo`, `withRrsult` and `redirectToParent`.
- Extensions for `Navigator` like `navigateBack`, `navigateBackWithResult`, `replace`, `replaceAll`, `popUpTo` etc...

## Usage

### Definingdestinations
```kotlin
// Destination without argument
data object MyDestination : ScreenDestination {

    @Composable
    override fun Content() {
        // ...
    }
}

// Destination with argument
data class MyDestinationWithArg(val argument: String) : ScreenDestination {

    @Composable
    override fun Content() {
        // ...
    }
}
```

### Create Navigator 
```kotlin
@Composable
fun Screen(parentNavigator: Navigator) {
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
fun Screen() {
    val navigator = LocalNavigator.current
    
    with (navigator) {
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
fun Screen() {
    val navigator = LocalNavigator.current
    
    with (navigator) {
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

### More examples
Please check sample project and instrumented tests in the repo.