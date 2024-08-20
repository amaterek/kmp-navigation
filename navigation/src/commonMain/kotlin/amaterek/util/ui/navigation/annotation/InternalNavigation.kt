package amaterek.util.ui.navigation.annotation

import kotlin.annotation.AnnotationRetention.BINARY

@Target(
    allowedTargets = [
        AnnotationTarget.CLASS,
        AnnotationTarget.PROPERTY,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.TYPEALIAS,
        AnnotationTarget.CONSTRUCTOR
    ]
)
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is Navigation Internal.",
)
@Retention(BINARY)
annotation class InternalNavigation