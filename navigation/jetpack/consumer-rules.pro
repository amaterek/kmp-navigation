-keep,allowshrinking,allowoptimization,allowobfuscation class ** extends amaterek.util.ui.navigation.destination.ScreenDestination
-keepclassmembernames class ** extends amaterek.util.ui.navigation.destination.ScreenDestination {
    public static * INSTANCE;
    public static * Companion;
}
-keep,allowshrinking,allowoptimization class **$Companion extends amaterek.util.ui.navigation.transition.ScreenTransitionProvider
-keep,allowshrinking,allowoptimization class **$Companion extends amaterek.util.ui.navigation.destination.DialogPropertiesProvider