package amaterek.util.ui.navigation.serialization

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual interface Serializable

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual typealias SkipForSerialization = IOsSkipForSerialization

@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class IOsSkipForSerialization