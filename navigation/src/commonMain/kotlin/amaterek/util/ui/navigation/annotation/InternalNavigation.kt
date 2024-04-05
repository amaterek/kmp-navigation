package amaterek.util.ui.navigation.annotation

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
annotation class InternalNavigation