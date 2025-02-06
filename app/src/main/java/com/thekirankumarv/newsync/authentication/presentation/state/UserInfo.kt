package com.thekirankumarv.newsync.authentication.presentation.state

open class ProfileInfo(
    open val id: String = "",
    open val name: String = "",
    open val email: String = "",
    open val profilePic: String = ""
) {
    data class ManualUser(
        override val id: String = "",
        override val name: String = "",
        override val email: String = "",
        override val profilePic: String = ""
    ) : ProfileInfo(id, name, email, profilePic)

    data class GoogleUser(
        override val id: String = "",
        override val name: String = "",
        override val email: String = "",
        override val profilePic: String = ""
    ) : ProfileInfo(id, name, email, profilePic)

    data class FacebookUser(
        override val id: String = "",
        override val name: String = "",
        override val email: String = "",
        override val profilePic: String = ""
    ) : ProfileInfo(id, name, email, profilePic)
}



