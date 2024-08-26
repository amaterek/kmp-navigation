@file:Suppress("MatchingDeclarationName")

package amaterek.util.ui.navigation.serialization

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual interface Serializable : Parcelable

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual typealias Serialize = Parcelize

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual typealias SkipForSerialization = Transient