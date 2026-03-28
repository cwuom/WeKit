package moe.ouom.wekit.hooks.item.chat.risk

import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import de.robv.android.xposed.XposedHelpers
import moe.ouom.wekit.core.model.BaseHookItem
import moe.ouom.wekit.core.model.BaseSwitchFunctionHookItem
import moe.ouom.wekit.hooks.core.annotation.HookItem
import moe.ouom.wekit.hooks.item.chat.msg.ShortcutMenu
import moe.ouom.wekit.hooks.sdk.api.WeMessageApi
import moe.ouom.wekit.hooks.sdk.ui.WeChatFooterApi
import moe.ouom.wekit.loader.hookapi.IMenu
import moe.ouom.wekit.util.common.Toasts
import kotlin.text.iterator

@HookItem(path = "聊天与消息/发送超链接", desc = "快捷菜单 发送超链接")
class SendSuperLink: BaseSwitchFunctionHookItem() {
    override fun entry(classLoader: ClassLoader) {
        ShortcutMenu.Companion.menus.add(object : IMenu {
            override val creator: BaseHookItem
                get() = this@SendSuperLink
            override val menuName: String
                get() = "发送超链接"
            override val isShow: () -> Boolean
                get() = { this@SendSuperLink.configIsEnable() }
            override val onClick: (context: Context, footer: Any) -> Unit
                get() = { context, footer ->
                    MaterialDialog(context)
                        .title(text = "发送超链接")
                        .customView(view = LinearLayout(context).apply {
                            orientation = LinearLayout.VERTICAL

                            val url = EditText(context).apply {
                                hint = "请输入链接"
                            }
                            val text = EditText(context).apply {
                                hint = "请输入文本"
                            }

                            addView(url)
                            addView(text)

                            // 绑定到 dialog
                            tag = Pair(url, text)
                        })
                        .positiveButton(text = "发送") { dialog ->
                            val (url, text) = dialog.view.tag as Pair<EditText, EditText>

                            fun htmlEscape(input: String): String {
                                return buildString {
                                    for (c in input) {
                                        when (c) {
                                            '&' -> append("&amp;")
                                            '<' -> append("&lt;")
                                            '>' -> append("&gt;")
                                            '"' -> append("&quot;")
                                            '\'' -> append("&#39;")
                                            else -> append(c)
                                        }
                                    }
                                }
                            }

                            val sendText = htmlEscape("<a href=\"${url.text}\">${text.text}</a>")

                            val xmlContent = "<msg><appmsg appid=\"\" sdkver=\"0\"><title>$sendText</title><des></des><username></username><action>view</action><type>53</type>...</appmsg></msg>"

                            WeMessageApi.Companion.INSTANCE?.sendXmlAppMsg(
                                XposedHelpers.getAdditionalInstanceField(
                                    footer,
                                    WeChatFooterApi.Companion.FIELD_TO_USER
                                ) as String,
                                xmlContent
                            )

                            Toasts.showToast(context, "发送成功")
                        }
                        .negativeButton(text = "取消")
                        .show()
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