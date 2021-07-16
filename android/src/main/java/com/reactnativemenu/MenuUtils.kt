package com.reactnativemenu

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import com.facebook.react.bridge.*
import java.lang.reflect.Field

fun prepareMenuItem(reactContext: ReactContext, menuItem: MenuItem, config: ReadableMap?, onItemSelected: (id: String?) -> Unit) {
  val titleColor = when (config != null && config.hasKey("titleColor") && !config.isNull("titleColor")) {
    true -> config.getInt("titleColor")
    else -> null
  }
  val imageName = when (config != null && config.hasKey("image") && !config.isNull("image")) {
    true -> config.getString("image")
    else -> null
  }
  val imageColor = when (config != null && config.hasKey("imageColor") && !config.isNull("imageColor")) {
    true -> config.getInt("imageColor")
    else -> null
  }
  val attributes = when (config != null && config.hasKey("attributes") && !config.isNull(("attributes"))) {
    true -> config.getMap("attributes")
    else -> null
  }
  val subactions = when (config != null && config.hasKey("subactions") && !config.isNull(("subactions"))) {
    true -> config.getArray("subactions")
    else -> null
  }

  if (titleColor != null) {
    menuItem.title = getTextWithColor(menuItem.title.toString(), titleColor)
  }

  if (imageName != null) {
    val resourceId: Int = getDrawableIdWithName(reactContext, imageName)
    if (resourceId != 0) {
      val icon = reactContext.resources.getDrawable(resourceId, reactContext.theme)
      if (imageColor != null) {
        icon.setTintList(ColorStateList.valueOf(imageColor))
      }
      menuItem.icon = icon
    }
  }

  if (attributes != null) {
    // actions.attributes.disabled
    val disabled = when (attributes.hasKey("disabled") && !attributes.isNull("disabled")) {
      true -> attributes.getBoolean("disabled")
      else -> false
    }
    menuItem.isEnabled = !disabled
    if (!menuItem.isEnabled) {
      val disabledColor = 0x77888888
      menuItem.title = getTextWithColor(menuItem.title.toString(), disabledColor)
      if (imageName != null) {
        val icon = menuItem.icon
        icon.setTintList(ColorStateList.valueOf(disabledColor))
        menuItem.icon = icon
      }
    }

    // actions.attributes.hidden
    val hidden = when (attributes.hasKey("hidden") && !attributes.isNull("hidden")) {
      true -> attributes.getBoolean("hidden")
      else -> false
    }
    menuItem.isVisible = !hidden

    // actions.attributes.destructive
    val destructive = when (attributes.hasKey("destructive") && !attributes.isNull("destructive")) {
      true -> attributes.getBoolean("destructive")
      else -> false
    }
    if (destructive) {
      menuItem.title = getTextWithColor(menuItem.title.toString(), Color.RED)
      if (imageName != null) {
        val icon = menuItem.icon
        icon.setTintList(ColorStateList.valueOf(Color.RED))
        menuItem.icon = icon
      }
    }
  }

  // On Android SubMenu cannot contain another SubMenu, so even if there are subactions provided
  // we are checking if item has submenu (which will occur only for 1 lvl nesting)
  if (subactions != null && menuItem.hasSubMenu()) {
    var i = 0
    val subactionsCount = subactions.size()
    while (i < subactionsCount) {
      if (!subactions.isNull(i)) {
        val subMenuConfig = subactions.getMap(i)
        val subMenuItem = menuItem.subMenu.add(Menu.NONE, Menu.NONE, i, subMenuConfig?.getString("title"))
        prepareMenuItem(reactContext, subMenuItem, subMenuConfig, onItemSelected)
        subMenuItem.setOnMenuItemClickListener {
          if (!it.hasSubMenu()) {
            if (!subactions.isNull(it.order)) {
              val selectedItem = subactions.getMap(it.order)
              onItemSelected(selectedItem?.getString("id"))
            } else {
              onItemSelected(null)
            }
            true
          } else {
            false
          }
        }
      }
      i++
    }
  }
}

fun getDrawableIdWithName(context: Context, name: String): Int {
  val appResources: Resources = context.resources
  var resourceId = appResources.getIdentifier(name, "drawable", context.packageName)
  if (resourceId == 0) {
    // If drawable is not present in app's resources, check system's resources
    resourceId = getResId(name, android.R.drawable::class.java)
  }
  return resourceId
}

private fun getResId(resName: String?, c: Class<*>): Int {
  return try {
    val idField: Field = c.getDeclaredField(resName!!)
    idField.getInt(idField)
  } catch (e: Exception) {
    e.printStackTrace()
    0
  }
}

fun getTextWithColor(text: String, color: Int): SpannableStringBuilder {
  val textWithColor = SpannableStringBuilder()
  textWithColor.append(text)
  textWithColor.setSpan(
    ForegroundColorSpan(color),
    0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
  return textWithColor
}
