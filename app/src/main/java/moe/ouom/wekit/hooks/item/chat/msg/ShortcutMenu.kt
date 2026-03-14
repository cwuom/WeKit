package moe.ouom.wekit.hooks.item.chat.msg

import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.robv.android.xposed.XposedHelpers
import moe.ouom.wekit.core.model.BaseSwitchFunctionHookItem
import moe.ouom.wekit.hooks.core.annotation.HookItem
import moe.ouom.wekit.hooks.sdk.protocol.WeApi
import moe.ouom.wekit.hooks.sdk.protocol.WePkgHelper
import moe.ouom.wekit.hooks.sdk.ui.WeChatFooterApi
import moe.ouom.wekit.ui.CommonContextWrapper
import moe.ouom.wekit.util.common.Toasts
import moe.ouom.wekit.util.log.WeLogger

@HookItem(path = "聊天与消息/快捷菜单", desc = "长按加号")
class ShortcutMenu: BaseSwitchFunctionHookItem() {
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
                            MaterialAlertDialogBuilder(context).apply {
                                setTitle("快捷菜单")
                                setItems(arrayOf("发送卡片")) { _, which ->
                                    when (which) {
                                        0 -> {
                                            MaterialAlertDialogBuilder(context).apply {
                                                setTitle("发送卡片")

                                                val layout = LinearLayout(context).apply {
                                                    orientation = LinearLayout.VERTICAL
                                                }

                                                val appid = EditText(context).apply {
                                                    hint = "APP ID (默认 哔哩哔哩)"
                                                    setText("wxcb8d4298c6a09bcb")
                                                }
                                                val pkgName = EditText(context).apply {
                                                    hint = "包名 (默认 哔哩哔哩)"
                                                    setText("tv.danmaku.bili")
                                                }
                                                val pkgSign = EditText(context).apply {
                                                    hint = "签名 (默认 哔哩哔哩)"
                                                    setText("419e4aba7985c9724ed378386730de4a")
                                                }

                                                layout.addView(appid)
                                                layout.addView(pkgName)
                                                layout.addView(pkgSign)

                                                val title = EditText(context).apply {
                                                    hint = "标题"
                                                }
                                                val desc = EditText(context).apply {
                                                    hint = "简介"
                                                }
                                                val url = EditText(context).apply {
                                                    hint = "链接"
                                                }
                                                val cdnThumb = EditText(context).apply {
                                                    hint = "cdnThumb"
                                                }

                                                layout.addView(title)
                                                layout.addView(desc)
                                                layout.addView(url)
                                                layout.addView(cdnThumb)

                                                setView(layout)

                                                setPositiveButton("发送") { _, _ ->
                                                    fun xmlEscape(s: String): String {
                                                        return s
                                                            .replace("&", "&amp;")
                                                            .replace("<", "&lt;")
                                                            .replace(">", "&gt;")
                                                            .replace("\"", "&quot;")
                                                            .replace("'", "&apos;")
                                                    }

                                                    val selfWxId = WeApi.getSelfWxId()
                                                    val toUser = XposedHelpers.getAdditionalInstanceField(
                                                        footer,
                                                        WeChatFooterApi.FIELD_TO_USER
                                                    ) as String

                                                    val appIdStr = appid.text.toString()
                                                    val titleStr = xmlEscape(title.text.toString())
                                                    val descStr = xmlEscape(desc.text.toString()).replace("\n", "&#x0A;")
                                                    val urlStr = xmlEscape(url.text.toString())
                                                    val cdnThumbStr = xmlEscape(cdnThumb.text.toString())
                                                    val pkgNameStr = pkgName.text.toString().replace("\\", "\\\\").replace("\"", "\\\"")
                                                    val pkgSignStr = pkgSign.text.toString().replace("\\", "\\\\").replace("\"", "\\\"")
                                                    val timeSec = System.currentTimeMillis() / 1000

                                                    val appMsgXml = """
<appmsg appid="$appIdStr" sdkver="0">
    <title>$titleStr</title>
    <des>$descStr</des>
    <username></username>
    <action>view</action>
    <type>4</type>
    <showtype>0</showtype>
    <content></content>
    <url>$urlStr</url>
    <lowurl></lowurl>
    <forwardflag>0</forwardflag>
    <dataurl></dataurl>
    <lowdataurl></lowdataurl>
    <contentattr>0</contentattr>
    <streamvideo>
        <streamvideourl></streamvideourl>
        <streamvideototaltime>0</streamvideototaltime>
        <streamvideotitle></streamvideotitle>
        <streamvideowording></streamvideowording>
        <streamvideoweburl></streamvideoweburl>
        <streamvideothumburl></streamvideothumburl>
        <streamvideoaduxinfo></streamvideoaduxinfo>
        <streamvideopublishid></streamvideopublishid>
    </streamvideo>
    <canvasPageItem>
        <canvasPageXml><![CDATA[]]></canvasPageXml>
    </canvasPageItem>
    <appattach>
        <attachid></attachid>
        <cdnthumburl>$cdnThumbStr</cdnthumburl>
        <cdnthumbmd5>419e4aba7985c9724ed378386730de4a</cdnthumbmd5>
        <cdnthumblength>10019</cdnthumblength>
        <cdnthumbheight>100</cdnthumbheight>
        <cdnthumbwidth>100</cdnthumbwidth>
        <cdnthumbaeskey>5a8b301b7e93e8133776a755f8710825</cdnthumbaeskey>
        <aeskey>5a8b301b7e93e8133776a755f8710825</aeskey>
        <encryver>1</encryver>
        <fileext></fileext>
        <islargefilemsg>0</islargefilemsg>
        <filename></filename>
    </appattach>
    <extinfo></extinfo>
    <androidsource>2</androidsource>
    <thumburl></thumburl>
    <mediatagname></mediatagname>
    <messageaction><![CDATA[]]></messageaction>
    <messageext><![CDATA[]]></messageext>
    <emoticongift><packageflag>0</packageflag><packageid></packageid></emoticongift>
    <emoticonshared><packageflag>0</packageflag><packageid></packageid></emoticonshared>
    <designershared>
        <designeruin>0</designeruin>
        <designername>null</designername>
        <designerrediretcturl><![CDATA[null]]></designerrediretcturl>
    </designershared>
    <emotionpageshared>
        <tid>0</tid>
        <title>null</title>
        <desc>null</desc>
        <iconUrl><![CDATA[null]]></iconUrl>
        <secondUrl>null</secondUrl>
        <pageType>0</pageType>
        <setKey>null</setKey>
    </emotionpageshared>
    <webviewshared>
        <shareUrlOriginal></shareUrlOriginal>
        <shareUrlOpen></shareUrlOpen>
        <jsAppId></jsAppId>
        <publisherId></publisherId>
        <publisherReqId></publisherReqId>
    </webviewshared>
    <template_id></template_id>
    <md5></md5>
    <websearch><rec_category>0</rec_category><channelId>0</channelId></websearch>
    <weappinfo>
        <username></username>
        <appid></appid>
        <appservicetype>0</appservicetype>
        <secflagforsinglepagemode>0</secflagforsinglepagemode>
        <videopageinfo>
            <thumbwidth>100</thumbwidth>
            <thumbheight>100</thumbheight>
            <fromopensdk>0</fromopensdk>
        </videopageinfo>
    </weappinfo>
    <statextstr>GhQKEnd4Y2I4ZDQyOThjNmEwOWJjYg==</statextstr>
    <musicShareItem><musicDuration>0</musicDuration></musicShareItem>
    <directshare>0</directshare>
    <gameshare>
        <liteappext><liteappbizdata></liteappbizdata><priority>0</priority></liteappext>
        <appbrandext><litegameinfo></litegameinfo><priority>-1</priority></appbrandext>
        <gameshareid></gameshareid>
        <sharedata></sharedata>
        <isvideo>0</isvideo>
        <duration>-1</duration>
        <isexposed>0</isexposed>
        <readtext></readtext>
    </gameshare>
    <liteapp>
        <id>null</id>
        <path></path>
        <query></query>
        <istransparent>0</istransparent>
        <hideicon>0</hideicon>
        <forbidforward>0</forbidforward>
    </liteapp>
    <opensdk_share_is_modified>0</opensdk_share_is_modified>
</appmsg>
""".trimIndent()

                                                    val body = """
{
  "2": {
    "1": "$selfWxId",
    "2": "$appIdStr",
    "3": 0,
    "4": "$toUser",
    "5": 4,
    "6": ${org.json.JSONObject.quote(appMsgXml)},
    "7": $timeSec,
    "8": "${toUser}10973T1773476774120",
    "10": 2,
    "11": 0,
    "13": "",
    "14": "",
    "15": "",
    "16": 1,
    "17": "$pkgNameStr"
  },
  "4": 0,
  "5": "$pkgSignStr",
  "6": 0,
  "9": 0,
  "10": 825634038,
  "11": 0,
  "12": 0
}
""".trimIndent()

                                                    WePkgHelper.INSTANCE?.sendCgi(
                                                        "/cgi-bin/micromsg-bin/sendappmsg",
                                                        222,
                                                        0,
                                                        0,
                                                        body
                                                    )

                                                    Toasts.showToast(context, "已发送, 自己看不见")
                                                }
                                            }.show()
                                        }
                                    }
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