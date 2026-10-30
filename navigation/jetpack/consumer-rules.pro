-keep,allowshrinking,allowoptimization,allowobfuscation class ** extends amaterek.util.ui.navigation.destination.ScreenDestination
-keepclassmembernames class ** extends amaterek.util.ui.navigation.destination.ScreenDestination {
    public static * INSTANCE;
}
