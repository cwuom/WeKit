package moe.ouom.wekit.hooks.item.chat.msg

import android.content.Context
import android.widget.ImageButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import moe.ouom.wekit.core.model.BaseSwitchFunctionHookItem
import moe.ouom.wekit.hooks.core.annotation.HookItem
import moe.ouom.wekit.hooks.sdk.ui.WeChatFooterApi
import moe.ouom.wekit.intf.IMenu
import moe.ouom.wekit.ui.CommonContextWrapper
import moe.ouom.wekit.util.log.WeLogger

@HookItem(path = "聊天与消息/快捷菜单", desc = "长按加号")
class ShortcutMenu: BaseSwitchFunctionHookItem() {
    companion object {
        val menus = mutableListOf<IMenu>()
    }

    override fun entry(classLoader: ClassLoader) {
        WeChatFooterApi.listener.add { footer ->
            try {
                val fields = footer.javaClass.declaredFields
                for (field in fields) {
                    field.isAccessible = true
                    if (!ImageButton::class.java.isAssignableFrom(field.type)) {
                        // WeLogger.d(field.type.name)
                        continue
                    }

                    WeLogger.d("Found")

                    val button = field.get(footer) as? ImageButton ?: continue
                    val text = button.contentDescription?.toString()?.trim() ?: ""

                    if (text.contains("更多功能按钮")) {
                        WeLogger.i("定位到更多功能按钮 -> 字段名: ${field.name}")
                        button.setOnLongClickListener { button ->
                            val context = CommonContextWrapper.createAppCompatContext(button.context)
                            val menuNames = arrayListOf<String>()
                            val menuFunc = arrayListOf<(context: Context, footer: Any) -> Unit>()
                            for (menu in menus) {
                                menuNames.add(menu.menuName)
                                menuFunc.add(menu.onClick)
                            }
                            MaterialAlertDialogBuilder(context).apply {
                                setTitle("快捷菜单")
                                setItems(menuNames.toTypedArray()) { _, which ->
                                    menuFunc[which].invoke(context, footer)
                                }
                            }.show()
                            return@setOnLongClickListener true
                        }
                    }
                }
            } catch (e: Throwable) {
                WeLogger.e("查找按钮过程出错", e)
            }
        }
    }
}