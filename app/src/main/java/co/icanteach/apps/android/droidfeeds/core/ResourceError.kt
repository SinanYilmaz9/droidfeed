package co.icanteach.apps.android.droidfeeds.core

import androidx.annotation.StringRes

class ResourceError(
    @StringRes
    val actionText: Int,
    @StringRes
    val errorTitle: Int,
    @StringRes
    val errorDesc: Int
)