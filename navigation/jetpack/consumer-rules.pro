-keep,allowshrinking,allowoptimization,allowobfuscation class ** extends amaterek.util.ui.navigation.destination.ScreenDestination
-keep,allowshrinking,allowoptimization class ** extends amaterek.util.ui.navigation.destination.DialogPropertiesProvider
-keepclassmembernames class ** extends amaterek.util.ui.navigation.destination.ScreenDestination {
    public static * INSTANCE;
    public static * Companion;
}