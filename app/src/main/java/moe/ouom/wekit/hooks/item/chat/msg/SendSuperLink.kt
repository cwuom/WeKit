package moe.ouom.wekit.hooks.item.chat.msg

import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.robv.android.xposed.XposedHelpers
import moe.ouom.wekit.core.model.BaseHookItem
import moe.ouom.wekit.core.model.BaseSwitchFunctionHookItem
import moe.ouom.wekit.hooks.core.annotation.HookItem
import moe.ouom.wekit.hooks.sdk.api.WeMessageApi
import moe.ouom.wekit.hooks.sdk.ui.WeChatFooterApi
import moe.ouom.wekit.intf.IMenu
import moe.ouom.wekit.util.common.Toasts

@HookItem(path = "聊天与消息/发送超链接", desc = "快捷菜单 发送超链接")
class SendSuperLink: BaseSwitchFunctionHookItem() {
    override fun entry(classLoader: ClassLoader) {
        ShortcutMenu.menus.add(object : IMenu {
            override val creator: BaseHookItem
                get() = this@SendSuperLink
            override val menuName: String
                get() = "发送超链接"
            override val onClick: (context: Context, footer: Any) -> Unit
                get() = { context, footer ->
                    MaterialAlertDialogBuilder(context).apply {
                        setTitle("发送超链接")
                        val layout = LinearLayout(context).apply {
                            orientation = LinearLayout.VERTICAL
                        }

                        val url = EditText(context).apply {
                            hint = "请输入链接"
                        }
                        val text = EditText(context).apply {
                            hint = "请输入文本"
                        }

                        layout.addView(url)
                        layout.addView(text)
                        setView(layout)

                        setPositiveButton("发送") { _, _ ->
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
                            val xmlContent = "<msg><appmsg appid=\"\" sdkver=\"0\"><title>$sendText</title><des></des><username></username><action>view</action><type>53</type><showtype>0</showtype><content></content><url></url><lowurl></lowurl><forwardflag>0</forwardflag><dataurl></dataurl><lowdataurl></lowdataurl><contentattr>0</contentattr><streamvideo><streamvideourl></streamvideourl><streamvideototaltime>0</streamvideototaltime><streamvideotitle></streamvideotitle><streamvideowording></streamvideowording><streamvideoweburl></streamvideoweburl><streamvideothumburl></streamvideothumburl><streamvideoaduxinfo></streamvideoaduxinfo><streamvideopublishid></streamvideopublishid></streamvideo><canvasPageItem><canvasPageXml><![CDATA[]]></canvasPageXml></canvasPageItem><appattach><totallen>0</totallen><attachid></attachid><cdnattachurl></cdnattachurl><emoticonmd5></emoticonmd5><aeskey></aeskey><fileext></fileext><islargefilemsg>0</islargefilemsg><filename></filename></appattach><extinfo><solitaire_info><![CDATA[<solitaire><tt>0-5</tt><ex>0-0</ex><tl>0-0</tl><s>.</s><au>wxid_22rgiv7r7l5j22</au><hrt>1</hrt><loss>0</loss><content><s>1</s><i><u>wxid_22rgiv7r7l5j22</u><h>0</h><s>.</s><t>1773488608</t><r>10-7</r></i></content></solitaire>]]></solitaire_info></extinfo><androidsource>0</androidsource><thumburl></thumburl><mediatagname></mediatagname><messageaction><![CDATA[]]></messageaction><messageext><![CDATA[]]></messageext><emoticongift><packageflag>0</packageflag><packageid></packageid></emoticongift><emoticonshared><packageflag>0</packageflag><packageid></packageid></emoticonshared><designershared><designeruin>0</designeruin><designername>null</designername><designerrediretcturl><![CDATA[null]]></designerrediretcturl></designershared><emotionpageshared><tid>0</tid><title>null</title><desc>null</desc><iconUrl><![CDATA[null]]></iconUrl><secondUrl>null</secondUrl><pageType>0</pageType><setKey>null</setKey></emotionpageshared><webviewshared><shareUrlOriginal></shareUrlOriginal><shareUrlOpen></shareUrlOpen><jsAppId></jsAppId><publisherId></publisherId><publisherReqId></publisherReqId></webviewshared><template_id></template_id><md5></md5><websearch><rec_category>0</rec_category><channelId>0</channelId></websearch><weappinfo><username></username><appid></appid><appservicetype>0</appservicetype><secflagforsinglepagemode>0</secflagforsinglepagemode><videopageinfo><thumbwidth>0</thumbwidth><thumbheight>0</thumbheight><fromopensdk>0</fromopensdk></videopageinfo></weappinfo><statextstr></statextstr><musicShareItem><musicDuration>0</musicDuration></musicShareItem><finderLiveProductShare><finderLiveID><![CDATA[]]></finderLiveID><finderUsername><![CDATA[]]></finderUsername><finderObjectID><![CDATA[]]></finderObjectID><finderNonceID><![CDATA[]]></finderNonceID><liveStatus><![CDATA[]]></liveStatus><appId><![CDATA[]]></appId><pagePath><![CDATA[]]></pagePath><productId><![CDATA[]]></productId><coverUrl><![CDATA[]]></coverUrl><productTitle><![CDATA[]]></productTitle><marketPrice><![CDATA[0]]></marketPrice><sellingPrice><![CDATA[0]]></sellingPrice><platformHeadImg><![CDATA[]]></platformHeadImg><platformName><![CDATA[]]></platformName><shopWindowId><![CDATA[]]></shopWindowId><flashSalePrice><![CDATA[0]]></flashSalePrice><flashSaleEndTime><![CDATA[0]]></flashSaleEndTime><ecSource><![CDATA[]]></ecSource><sellingPriceWording><![CDATA[]]></sellingPriceWording><platformIconURL><![CDATA[]]></platformIconURL><firstProductTagURL><![CDATA[]]></firstProductTagURL><firstProductTagAspectRatioString><![CDATA[0.0]]></firstProductTagAspectRatioString><secondProductTagURL><![CDATA[]]></secondProductTagURL><secondProductTagAspectRatioString><![CDATA[0.0]]></secondProductTagAspectRatioString><firstGuaranteeWording><![CDATA[]]></firstGuaranteeWording><secondGuaranteeWording><![CDATA[]]></secondGuaranteeWording><thirdGuaranteeWording><![CDATA[]]></thirdGuaranteeWording><isPriceBeginShow>false</isPriceBeginShow><lastGMsgID><![CDATA[]]></lastGMsgID><promoterKey><![CDATA[]]></promoterKey><discountWording><![CDATA[]]></discountWording><priceSuffixDescription><![CDATA[]]></priceSuffixDescription><productCardKey><![CDATA[]]></productCardKey><isWxShop><![CDATA[]]></isWxShop><brandIconUrl><![CDATA[]]></brandIconUrl><rIconUrl><![CDATA[]]></rIconUrl><rIconUrlDarkMode><![CDATA[]]></rIconUrlDarkMode><topShopIconUrl><![CDATA[]]></topShopIconUrl><topShopIconUrlDarkMode><![CDATA[]]></topShopIconUrlDarkMode><simplifyTopShopIconUrl><![CDATA[]]></simplifyTopShopIconUrl><simplifyTopShopIconUrlDarkmode><![CDATA[]]></simplifyTopShopIconUrlDarkmode><topShopIconWidth><![CDATA[0]]></topShopIconWidth><topShopIconHeight><![CDATA[0]]></topShopIconHeight><simplifyTopShopIconWidth><![CDATA[0]]></simplifyTopShopIconWidth><simplifyTopShopIconHeight><![CDATA[0]]></simplifyTopShopIconHeight><maskPriceWording><![CDATA[]]></maskPriceWording><showBoxItemStringList></showBoxItemStringList><richLabelTitleB64><![CDATA[]]></richLabelTitleB64><richShopDescB64><![CDATA[]]></richShopDescB64></finderLiveProductShare><finderOrder><appID><![CDATA[]]></appID><orderID><![CDATA[]]></orderID><path><![CDATA[]]></path><priceWording><![CDATA[]]></priceWording><stateWording><![CDATA[]]></stateWording><productImageURL><![CDATA[]]></productImageURL><products><![CDATA[]]></products><productsCount><![CDATA[0]]></productsCount><orderType><![CDATA[0]]></orderType><newPriceWording><![CDATA[]]></newPriceWording><newStateWording><![CDATA[]]></newStateWording><useNewWording><![CDATA[0]]></useNewWording></finderOrder><finderShopWindowShare><finderUsername><![CDATA[]]></finderUsername><avatar><![CDATA[]]></avatar><nickname><![CDATA[]]></nickname><commodityInStockCount><![CDATA[]]></commodityInStockCount><appId><![CDATA[]]></appId><path><![CDATA[]]></path><appUsername><![CDATA[]]></appUsername><query><![CDATA[]]></query><liteAppId><![CDATA[]]></liteAppId><liteAppPath><![CDATA[]]></liteAppPath><liteAppQuery><![CDATA[]]></liteAppQuery><platformTagURL><![CDATA[]]></platformTagURL><saleWording><![CDATA[]]></saleWording><lastGMsgID><![CDATA[]]></lastGMsgID><profileTypeWording><![CDATA[]]></profileTypeWording><saleWordingExtra><![CDATA[]]></saleWordingExtra><isWxShop><![CDATA[]]></isWxShop><platformIconUrl><![CDATA[]]></platformIconUrl><brandIconUrl><![CDATA[]]></brandIconUrl><description><![CDATA[]]></description><backgroundUrl><![CDATA[]]></backgroundUrl><darkModePlatformIconUrl><![CDATA[]]></darkModePlatformIconUrl><rIconUrl><![CDATA[]]></rIconUrl><rIconUrlDarkMode><![CDATA[]]></rIconUrlDarkMode><rWords><![CDATA[]]></rWords><topShopIconUrl><![CDATA[]]></topShopIconUrl><topShopIconUrlDarkMode><![CDATA[]]></topShopIconUrlDarkMode><simplifyTopShopIconUrl><![CDATA[]]></simplifyTopShopIconUrl><simplifyTopShopIconUrlDarkmode><![CDATA[]]></simplifyTopShopIconUrlDarkmode><topShopIconWidth><![CDATA[0]]></topShopIconWidth><topShopIconHeight><![CDATA[0]]></topShopIconHeight><simplifyTopShopIconWidth><![CDATA[0]]></simplifyTopShopIconWidth><simplifyTopShopIconHeight><![CDATA[0]]></simplifyTopShopIconHeight><reputationInfo><hasReputationInfo>0</hasReputationInfo><reputationScore>0</reputationScore><reputationWording></reputationWording><reputationTextColor></reputationTextColor><reputationLevelWording></reputationLevelWording><reputationBackgroundColor></reputationBackgroundColor></reputationInfo><productImageURLList></productImageURLList></finderShopWindowShare><findernamecard><username></username><avatar><![CDATA[]]></avatar><nickname></nickname><auth_job></auth_job><auth_icon>0</auth_icon><auth_icon_url></auth_icon_url><ecSource><![CDATA[]]></ecSource><lastGMsgID><![CDATA[]]></lastGMsgID></findernamecard><finderGuarantee><scene><![CDATA[0]]></scene></finderGuarantee><directshare>0</directshare><gamecenter><namecard><iconUrl></iconUrl><name></name><desc></desc><tail></tail><jumpUrl></jumpUrl><liteappId></liteappId><liteappPath></liteappPath><liteappQuery></liteappQuery><liteappMinVersion></liteappMinVersion></namecard></gamecenter><patMsg><chatUser></chatUser><records><recordNum>0</recordNum></records></patMsg><secretmsg><issecretmsg>0</issecretmsg></secretmsg><referfromscene>0</referfromscene><gameshare><liteappext><liteappbizdata></liteappbizdata><priority>0</priority></liteappext><appbrandext><litegameinfo></litegameinfo><priority>-1</priority></appbrandext><gameshareid></gameshareid><sharedata></sharedata><isvideo>0</isvideo><duration>-1</duration><isexposed>0</isexposed><readtext></readtext></gameshare><tingChatRoomItem><type>0</type><categoryItem>null</categoryItem><categoryId></categoryId><listenItem>null</listenItem></tingChatRoomItem><photoaccountnamecard><username></username><nickname></nickname><fromnickname></fromnickname><fullpy></fullpy><shortpy></shortpy><alias></alias><imagestatus>0</imagestatus><scene>0</scene><province></province><city></city><sign></sign><percard>0</percard><sex>0</sex><certflag>0</certflag><certinfo></certinfo><certinfoext></certinfoext><brandIconUrl><![CDATA[]]></brandIconUrl><brandHomeUrl><![CDATA[]]></brandHomeUrl><brandSubscriptConfigUrl><![CDATA[]]></brandSubscriptConfigUrl><brandFlags>0</brandFlags><regionCode></regionCode><biznamecardinfo><![CDATA[]]></biznamecardinfo><brandType>0</brandType></photoaccountnamecard><mpsharetrace><hasfinderelement>0</hasfinderelement><lastgmsgid></lastgmsgid></mpsharetrace><wxgamecard><framesetname></framesetname><mbcarddata></mbcarddata><minpkgversion></minpkgversion><clientextinfo></clientextinfo><mbcardheight>0</mbcardheight><isoldversion>0</isoldversion></wxgamecard><ecskfcard><framesetname></framesetname><mbcarddata></mbcarddata><minupdateunixtimestamp>0</minupdateunixtimestamp><needheader>false</needheader><summary></summary></ecskfcard><liteapp><id>null</id><path></path><query></query><istransparent>0</istransparent><hideicon>0</hideicon><forbidforward>0</forbidforward></liteapp><opensdk_share_is_modified>0</opensdk_share_is_modified></appmsg></msg>"

                            WeMessageApi.INSTANCE?.sendXmlAppMsg(XposedHelpers.getAdditionalInstanceField(footer,
                                WeChatFooterApi.FIELD_TO_USER) as String, xmlContent)
                            Toasts.showToast(context, "发送成功")
                        }
                    }.show()
                }
        })
    }
}