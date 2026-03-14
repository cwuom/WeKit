package moe.ouom.wekit.hooks.item.chat.msg

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import moe.ouom.wekit.core.model.BaseSwitchFunctionHookItem
import moe.ouom.wekit.hooks.core.annotation.HookItem
import moe.ouom.wekit.ui.CommonContextWrapper
import moe.ouom.wekit.util.common.SyncUtils
import moe.ouom.wekit.util.common.Toasts
import moe.ouom.wekit.util.log.WeLogger
import java.io.File
import java.io.FileOutputStream


@HookItem(path = "聊天与消息/获取预览图数据", desc = "去别的软件随便分享内容，然后就会拦截")
class FuckThumbUrl: BaseSwitchFunctionHookItem() {
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
        XposedHelpers.findAndHookMethod(
            "com.tencent.mm.ui.transmit.SelectConversationUI",
            classLoader,
            "onCreate",
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!this@FuckThumbUrl.configIsEnable()) return

                    val activity = param.thisObject as Activity
                    val extras = activity.intent.extras ?: return
                    val nextStepIntent = extras.get("Select_Conv_NextStep") as? Intent ?: return
                    val nextStep = Bundle(nextStepIntent.extras ?: Bundle())

                    SyncUtils.postDelayed(1000) {
                        MaterialAlertDialogBuilder(CommonContextWrapper.createAppCompatContext(activity)).apply {
                            setTitle("获取预览图数据")
                            setMessage("是否拦截篡改预览图数据？")
                            setPositiveButton("是") { _, _ ->
                                HookImagePicker.pick(activity) { bytes ->
                                    WeLogger.d("picked bytes size = ${bytes?.size}")

                                    if (bytes != null) {
                                        nextStep.putByteArray("_wxobject_thumbdata", bytes)

                                        nextStepIntent.replaceExtras(nextStep)

                                        activity.intent.putExtra("Select_Conv_NextStep", nextStepIntent)

                                        WeLogger.d(
                                            "replace _wxobject_thumbdata done, current len=" +
                                                    "${nextStepIntent.extras?.getByteArray("_wxobject_thumbdata")?.size}"
                                        )

                                        Toasts.showToast(activity, "已替换预览图数据")
                                    } else {
                                        Toasts.showToast(activity, "未能读取到图片，请检查微信权限")
                                    }
                                }
                            }
                            setNegativeButton("否", null)
                        }.show()
                    }
                }
            }
        )
    }
}