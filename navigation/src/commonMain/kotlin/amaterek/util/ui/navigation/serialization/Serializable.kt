package amaterek.util.ui.navigation.serialization

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect interface Serializable

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
expect annotation class SkipForSerialization()