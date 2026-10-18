package moe.ktandth.songmeld.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import moe.ktandth.songmeld.R

/** Explicit, reviewed credits. A PR or idea does not automatically grant a developer role. */
internal data class AboutPerson(
    val name: String,
    val displayName: String? = null,
    @StringRes val contribution: Int,
    @DrawableRes val avatar: Int? = null,
    val githubUrl: String? = null,
    val email: String? = null,
)

internal object AboutCredits {
    val primaryDevelopers = listOf(
        AboutPerson(
            name = "ktandth",
            contribution = R.string.about_primary_detail,
            avatar = R.drawable.developer_ktandth,
            githubUrl = "https://github.com/nktdth",
            email = "nkt@ktandth.moe",
        ),
    )
    // Add people here only after their ongoing developer role has been confirmed.
    val secondaryDevelopers: List<AboutPerson> = emptyList()
    // Idea authors, bug reporters and PR authors are credited separately here.
    val contributors: List<AboutPerson> = emptyList()
}
