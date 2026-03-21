package moe.ouom.wekit.hooks.item.chat.msg

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.PopupWindow
import moe.ouom.wekit.core.model.BaseSwitchFunctionHookItem
import moe.ouom.wekit.hooks.core.annotation.HookItem
import moe.ouom.wekit.hooks.sdk.ui.WeChatFooterApi
import moe.ouom.wekit.loader.hookapi.IMenu
import moe.ouom.wekit.ui.CommonContextWrapper
import moe.ouom.wekit.util.log.WeLogger
import java.text.Collator
import java.util.Locale

@HookItem(path = "聊天与消息/快捷菜单", desc = "长按加号")
class ShortcutMenu : BaseSwitchFunctionHookItem() {
    companion object {
        val menus = mutableListOf<IMenu>()
    }

    private var popupWindow: PopupWindow? = null

    override fun entry(classLoader: ClassLoader) {
        WeChatFooterApi.listener.add { footer ->
            try {
                val fields = footer.javaClass.declaredFields
                for (field in fields) {
                    field.isAccessible = true
                    if (!ImageButton::class.java.isAssignableFrom(field.type)) continue

                    val button = field.get(footer) as? ImageButton ?: continue
                    val text = button.contentDescription?.toString()?.trim() ?: ""

                    if (text.contains("更多功能按钮")) {
                        WeLogger.i("定位到更多功能按钮 -> 字段名: ${field.name}")
                        button.setOnLongClickListener { anchor ->
                            val context = CommonContextWrapper.createAppCompatContext(anchor.context)

                            val collator = Collator.getInstance(Locale.CHINA)

                            val sortedMenus = menus.sortedWith { a, b ->
                                collator.compare(a.menuName, b.menuName)
                            }

                            val menuNames = sortedMenus.filter { it.isShow() }.map { it.menuName }
                            val menuFunc = sortedMenus.filter { it.isShow() }.map { it.onClick }

                            showQuickMenuPopup(
                                context = context,
                                anchor = anchor,
                                menuNames = menuNames,
                                onItemClick = { which ->
                                    menuFunc[which].invoke(context, footer)
                                }
                            )
                            true
                        }
                    }
                }
            } catch (e: Throwable) {
                WeLogger.e("查找按钮过程出错", e)
            }
        }
    }

    private fun showQuickMenuPopup(
        context: Context,
        anchor: View,
        menuNames: List<String>,
        onItemClick: (Int) -> Unit
    ) {
        if (menuNames.isEmpty()) return

        popupWindow?.dismiss()

        val popupWidth = dp(context, 180)

        val listView = ListView(context).apply {
            dividerHeight = 0
            overScrollMode = View.OVER_SCROLL_NEVER
            isVerticalScrollBarEnabled = false
            setPadding(0, dp(context, 6), 0, dp(context, 6))
            setSelector(android.R.color.transparent)

            adapter = object : ArrayAdapter<String>(
                context,
                android.R.layout.simple_list_item_1,
                menuNames
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    view.minimumHeight = dp(context, 44)
                    view.setPadding(
                        dp(context, 16),
                        dp(context, 10),
                        dp(context, 16),
                        dp(context, 10)
                    )

                    val textView = view.findViewById<android.widget.TextView>(android.R.id.text1)
                    textView.setTextColor(Color.BLACK)
                    textView.textSize = 15f

                    return view
                }
            }

            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(context, 14).toFloat()
                setColor(Color.WHITE)
            }

            setOnItemClickListener { _, _, position, _ ->
                val pw = popupWindow
                if (pw != null && pw.isShowing) {
                    animateDismiss(this, pw) {
                        onItemClick(position)
                    }
                } else {
                    onItemClick(position)
                }
            }
        }

        val widthSpec = View.MeasureSpec.makeMeasureSpec(popupWidth, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        listView.measure(widthSpec, heightSpec)
        val popupHeight = listView.measuredHeight

        val popup = PopupWindow(
            listView,
            popupWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            isFocusable = true
            elevation = dp(context, 12).toFloat()
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
        }

        popupWindow = popup

        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val anchorX = location[0]
        val anchorY = location[1]

        val x = anchorX + (anchor.width - popupWidth) / 2
        val y = anchorY - popupHeight

        listView.alpha = 0f
        listView.scaleX = 0.92f
        listView.scaleY = 0.92f
        listView.translationY = dp(context, 8).toFloat()

        popup.showAtLocation(anchor, android.view.Gravity.START or android.view.Gravity.TOP, x, y)

        listView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setDuration(180)
            .start()
    }

    private fun dp(context: Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density + 0.5f).toInt()
    }

    private fun animateDismiss(view: View, popup: PopupWindow, endAction: (() -> Unit)? = null) {
        view.animate()
            .alpha(0f)
            .scaleX(0.92f)
            .scaleY(0.92f)
            .translationY(dp(view.context, 8).toFloat())
            .setDuration(140)
            .withEndAction {
                try {
                    popup.dismiss()
                } catch (_: Throwable) {
                }
                endAction?.invoke()
            }
            .start()
    }
}