package moe.ouom.wekit.loader.hookapi

import android.content.Context
import moe.ouom.wekit.core.model.BaseHookItem

interface IMenu {
    val creator: BaseHookItem
    val menuName: String
    val onClick: (context: Context, footer: Any) -> Unit
    val isShow: () -> Boolean
}