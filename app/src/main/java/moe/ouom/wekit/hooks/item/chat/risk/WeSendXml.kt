package moe.ouom.wekit.hooks.item.chat.risk

import android.annotation.SuppressLint
import android.content.Context
import android.widget.EditText
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.robv.android.xposed.XposedHelpers
import moe.ouom.wekit.core.model.BaseHookItem
import moe.ouom.wekit.core.model.BaseSwitchFunctionHookItem
import moe.ouom.wekit.hooks.core.annotation.HookItem
import moe.ouom.wekit.hooks.item.chat.msg.ShortcutMenu
import moe.ouom.wekit.hooks.sdk.api.WeMessageApi
import moe.ouom.wekit.hooks.sdk.ui.WeChatFooterApi
import moe.ouom.wekit.intf.IMenu
import moe.ouom.wekit.util.common.Toasts

@SuppressLint("DiscouragedApi")
@HookItem(path = "聊天与消息/发送 AppMsg(XML)", desc = "长按'发送'按钮，自动发送卡片消息")
class WeSendXml : BaseSwitchFunctionHookItem() {
    override fun entry(classLoader: ClassLoader) {
        ShortcutMenu.menus.add(object : IMenu {
            override val creator: BaseHookItem
                get() = this@WeSendXml
            override val menuName: String
                get() = "发送 AppMsg(XML)"
            override val onClick: (context: Context, footer: Any) -> Unit
                get() = { context, footer ->
                    MaterialAlertDialogBuilder(context).apply {
                        setTitle("发送 AppMsg(XML)")

                        val xml = EditText(context).apply {
                            hint = "请输入 XML"
                        }

                        setView(xml)
                        setPositiveButton("发送") { _, _ ->
                            val xmlContent = xml.text.toString()
                            WeMessageApi.INSTANCE?.sendXmlAppMsg(XposedHelpers.getAdditionalInstanceField(footer,
                                WeChatFooterApi.FIELD_TO_USER) as String, xmlContent)
                            Toasts.showToast(context, "发送成功")
                        }
                    }.show()
                }
        })
    }

    override fun onBeforeToggle(newState: Boolean, context: Context): Boolean {
        if (newState) {
            MaterialDialog(context)
                .title(text = "警告")
                .message(text = "此功能可能导致账号异常，确定要启用吗?")
                .positiveButton(text = "确定") { dialog ->
                    applyToggle(true)
                }
                .negativeButton(text = "取消") { dialog ->
                    dialog.dismiss()
                }
                .show()

            // 返回 false 阻止自动切换
            return false
        }

        // 禁用功能时直接允许
        return true
    }

}