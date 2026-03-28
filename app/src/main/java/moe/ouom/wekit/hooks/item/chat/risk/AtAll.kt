package moe.ouom.wekit.hooks.item.chat.risk

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.robv.android.xposed.XposedHelpers
import moe.ouom.wekit.core.model.BaseHookItem
import moe.ouom.wekit.core.model.BaseSwitchFunctionHookItem
import moe.ouom.wekit.hooks.core.annotation.HookItem
import moe.ouom.wekit.hooks.item.chat.msg.ShortcutMenu
import moe.ouom.wekit.hooks.sdk.api.WeDatabaseApi
import moe.ouom.wekit.hooks.sdk.protocol.WeApi
import moe.ouom.wekit.hooks.sdk.protocol.WePkgHelper
import moe.ouom.wekit.hooks.sdk.ui.WeChatFooterApi
import moe.ouom.wekit.loader.hookapi.IMenu
import moe.ouom.wekit.util.common.Toasts
import org.json.JSONObject

@HookItem(path = "聊天与消息/艾特全体", desc = "快捷菜单")
class AtAll: BaseSwitchFunctionHookItem() {
    override fun entry(classLoader: ClassLoader) {
        ShortcutMenu.menus.add(object : IMenu{
            override val creator: BaseHookItem
                get() = this@AtAll
            override val menuName: String
                get() = "艾特全体"
            override val onClick: (context: Context, footer: Any) -> Unit
                get() = { context, footer ->
                    val toUser = XposedHelpers.getAdditionalInstanceField(footer, WeChatFooterApi.FIELD_TO_USER) as String
                    if (toUser.contains("chatroom")) {
                        MaterialAlertDialogBuilder(context).apply {
                            setTitle("艾特全体")
                            setMessage("确定要艾特全体吗?\n* 可能导致账号下线\n* 群人数太多可能秒下线\n* 群人数太多可能不生效\n* 封号风险+++")
                            setPositiveButton("确定") { _, _ ->
                                val userid = WeDatabaseApi.INSTANCE?.getGroupMembers(toUser)?.filter { it.username != WeApi.getSelfWxId() }?.map { it.username }
                                var msg = ""
                                userid?.forEach { _ ->
                                    msg = "$msg@ "
                                }
                                msg = "${msg}大家好我艾特了全体哈哈哈"
                                val msgsource = "<msgsource><atuserlist><![CDATA[${userid?.joinToString(",")}]]></atuserlist><pua>1</pua><alnode><cf>5</cf><inlenlist>73</inlenlist></alnode><eggIncluded>1</eggIncluded></msgsource>"
                                val toUser = XposedHelpers.getAdditionalInstanceField(footer, WeChatFooterApi.FIELD_TO_USER) as String
                                val reqBody = JSONObject("{\n" +
                                        "  \"1\": 1,\n" +
                                        "  \"2\": {\n" +
                                        "    \"1\": {\n" +
                                        "      \"1\": \"${toUser}\"\n" +
                                        "    },\n" +
                                        "    \"2\": \"@所有人\",\n" +
                                        "    \"3\": 1,\n" +
                                        "    \"4\": ${System.currentTimeMillis() / 1000},\n" +
                                        "    \"5\": -388413336,\n" +
                                        "    \"6\": \"\"\n" +
                                        "  }\n" +
                                        "}")
                                reqBody.getJSONObject("2").put("2", msg)
                                reqBody.getJSONObject("2").put("6", msgsource)
                                WePkgHelper.INSTANCE?.sendCgi("/cgi-bin/micromsg-bin/newsendmsg", 522, 0, 0, reqBody.toString())
                                Toasts.showToast(context, "发送成功自己看不见")
                            }
                        }.show()
                    } else {
                        Toasts.showToast(context, "仅限在群聊使用")
                    }
                }
            override val isShow: () -> Boolean
                get() = { this@AtAll.configIsEnable() }
        })
    }
}