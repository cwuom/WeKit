package moe.ouom.wekit.hooks.item.chat.risk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.EditText
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import moe.ouom.wekit.core.model.BaseHookItem
import moe.ouom.wekit.core.model.BaseSwitchFunctionHookItem
import moe.ouom.wekit.hooks.core.annotation.HookItem
import moe.ouom.wekit.hooks.item.chat.msg.ShortcutMenu
import moe.ouom.wekit.hooks.sdk.api.WeMessageApi
import moe.ouom.wekit.hooks.sdk.ui.WeChatFooterApi
import moe.ouom.wekit.loader.hookapi.IMenu
import moe.ouom.wekit.util.common.Toasts
import moe.ouom.wekit.util.common.Utils
import moe.ouom.wekit.util.log.WeLogger

@SuppressLint("DiscouragedApi")
@HookItem(path = "聊天与消息/发送 AppMsg(XML)", desc = "长按'发送'按钮，自动发送卡片消息或快捷菜单")
class WeSendXml : BaseSwitchFunctionHookItem() {
    object HookImagePicker {
        private const val REQUEST_CODE = 0x4417
        private var callback: ((ByteArray?) -> Unit)? = null
        private val hookedClasses = HashSet<Class<*>>()

        fun pick(activity: Activity, callback: (ByteArray?) -> Unit) {
            this.callback = callback

            WeLogger.d("HookImagePicker.pick activity=${activity.javaClass.name}")

            hookOnActivityResultIfNeeded(activity.javaClass)

            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }

            activity.startActivityForResult(
                Intent.createChooser(intent, "选择图片"),
                REQUEST_CODE
            )
        }

        private fun hookOnActivityResultIfNeeded(clazz: Class<*>) {
            var current: Class<*>? = clazz

            while (current != null && Activity::class.java.isAssignableFrom(current)) {
                if (hookedClasses.contains(current)) {
                    current = current.superclass
                    continue
                }

                val declared = current.declaredMethods.any {
                    it.name == "onActivityResult" &&
                            it.parameterTypes.size == 3 &&
                            it.parameterTypes[0] == Int::class.javaPrimitiveType &&
                            it.parameterTypes[1] == Int::class.javaPrimitiveType &&
                            Intent::class.java.isAssignableFrom(it.parameterTypes[2])
                }

                if (declared) {
                    hookedClasses.add(current)

                    XposedBridge.hookAllMethods(current, "onActivityResult", object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val activity = param.thisObject as? Activity ?: return
                            val requestCode = param.args.getOrNull(0) as? Int ?: return
                            val resultCode = param.args.getOrNull(1) as? Int ?: return
                            val data = param.args.getOrNull(2) as? Intent

                            WeLogger.d(
                                "HookImagePicker.onActivityResult hit activity=${activity.javaClass.name}, " +
                                        "requestCode=$requestCode, resultCode=$resultCode, data=$data"
                            )

                            onActivityResult(activity, requestCode, resultCode, data)
                        }
                    })

                    WeLogger.d("HookImagePicker hooked onActivityResult in ${current.name}")
                    return
                }

                current = current.superclass
            }

            WeLogger.e("HookImagePicker: no declared onActivityResult found for ${clazz.name}")
        }

        fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode != REQUEST_CODE) return

            val cb = callback
            callback = null

            if (resultCode != Activity.RESULT_OK) {
                cb?.invoke(null)
                return
            }

            val uri = data?.data ?: run {
                cb?.invoke(null)
                return
            }

            val bytes = runCatching {
                activity.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }.onFailure {
                WeLogger.e("read image bytes failed: ${it.stackTraceToString()}")
            }.getOrNull()

            cb?.invoke(bytes)
        }
    }

    override fun entry(classLoader: ClassLoader) {
        ShortcutMenu.menus.add(object : IMenu {
            override val creator: BaseHookItem
                get() = this@WeSendXml
            override val menuName: String
                get() = "发送 AppMsg(XML)"
            override val isShow: () -> Boolean
                get() = { this@WeSendXml.configIsEnable() }
            override val onClick: (context: Context, footer: Any) -> Unit
                get() = { context, footer ->
                    MaterialAlertDialogBuilder(context).apply {
                        setTitle("发送 AppMsg(XML)")

                        val xml = EditText(context).apply {
                            hint = "请输入 XML"
                        }

                        setView(xml)
                        setPositiveButton("发送") { _, _ ->
                            Toasts.showToast(context, "请选择图片")
                            HookImagePicker.pick(Utils.getCurrentActivity()!!) { bytes ->
                                val xmlContent = xml.text.toString()
                                if (bytes == null) return@pick Toasts.showToast(context, "失败")
                                WeMessageApi.INSTANCE?.sendXmlAppMsg(XposedHelpers.getAdditionalInstanceField(footer,
                                    WeChatFooterApi.FIELD_TO_USER) as String, xmlContent, bytes)
                                Toasts.showToast(context, "发送成功")
                            }
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