package com.reactnativemenu

import android.content.res.ColorStateList
import android.view.Menu
import android.view.ViewGroup
import com.facebook.react.bridge.*

class MenuModule(private val reactContext: ReactApplicationContext): ReactContextBaseJavaModule(reactContext) {
  override fun getName(): String = NAME

  @ReactMethod
  fun showMenu(actions: ReadableArray, headerConfig: ReadableMap, promise: Promise) {
    val activity = reactContext.currentActivity ?: return
    val view = (activity.window.decorView.rootView as ViewGroup).getChildAt(0)
    activity.registerForContextMenu(view)
    view?.setOnCreateContextMenuListener { menu, _, _ ->
      val headerTitle = headerConfig.getString("title")
      val headerTitleColor = when (!headerConfig.isNull("titleColor")) {
        true -> headerConfig.getInt("titleColor")
        else -> null
      }
      val headerImage = headerConfig.getString("image")
      val headerImageColor = when (!headerConfig.isNull("imageColor")) {
        true -> headerConfig.getInt("imageColor")
        else -> null
      }

      if (headerTitle != null) {
        menu.setHeaderTitle(headerTitle)
        if (headerTitleColor != null) {
          menu.setHeaderTitle(getTextWithColor(headerTitle, headerTitleColor))
        }
      }
      if (headerImage != null) {
        val resourceId: Int = getDrawableIdWithName(reactContext, headerImage)
        if (resourceId != 0) {
          val icon = reactContext.resources.getDrawable(resourceId, reactContext.theme)
          if (headerImageColor != null) {
            icon.setTintList(ColorStateList.valueOf(headerImageColor))
          }
          menu.setHeaderIcon(icon)
        }
      }

      val getActionsCount = actions.size()
      var i = 0
      while (i < getActionsCount) {
        val item = actions.getMap(i)
        val menuItem = when (item != null && item.hasKey("subactions") && !item.isNull("subactions")) {
          true -> menu.addSubMenu(Menu.NONE, Menu.NONE, i, item.getString("title")).item
          else -> menu.add(Menu.NONE, Menu.NONE, i, item?.getString("title"))
        }
        prepareMenuItem(reactContext, menuItem, item) {
          if (it == null) {
            promise.reject("Null action", "Subaction is null")
          } else {
            promise.resolve(it)
          }
        }
        menuItem.setOnMenuItemClickListener {
          if (!it.hasSubMenu()) {
            if (!actions.isNull(it.order)) {
              val selectedItem = actions.getMap(it.order)
              promise.resolve(selectedItem?.getString("id"))
            } else {
              promise.reject("Null action", "Action with index: ${it.order} is null")
            }
            true
          } else {
            false
          }
        }
        i++
      }
    }

    view?.showContextMenu()
    activity.unregisterForContextMenu(view)
  }

  companion object {
    const val NAME = "MenuModule"
  }
}
