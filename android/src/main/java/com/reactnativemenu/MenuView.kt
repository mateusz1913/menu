package com.reactnativemenu

import android.os.Build
import android.view.*
import android.widget.PopupMenu
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.facebook.react.views.view.ReactViewGroup

class MenuView(private val mContext: ReactContext): ReactViewGroup(mContext) {
  private lateinit var mActions: ReadableArray
  private var mIsAnchoredToRight = false
  private val mPopupMenu: PopupMenu = PopupMenu(context, this)
  private var mIsOnLongPress = false
  private var mGestureDetector: GestureDetector

  init {
    mGestureDetector = GestureDetector(mContext, object : GestureDetector.SimpleOnGestureListener() {
      override fun onLongPress(e: MotionEvent?) {
        if (!mIsOnLongPress) {
          return
        }
        prepareMenu()
      }

      override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        if (!mIsOnLongPress) {
          prepareMenu()
        }
        return true
      }
    })
  }

  override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
    return true
  }

  override fun onTouchEvent(ev: MotionEvent?): Boolean {
    mGestureDetector.onTouchEvent(ev)
    return true
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    mPopupMenu.dismiss()
  }

  fun setActions(actions: ReadableArray) {
    mActions = actions
  }

  fun setIsAnchoredToRight(isAnchoredToRight: Boolean) {
    if (mIsAnchoredToRight == isAnchoredToRight) {
      return
    }
    mIsAnchoredToRight = isAnchoredToRight
  }

  fun setIsOpenOnLongPress(isLongPress: Boolean) {
    mIsOnLongPress = isLongPress
  }

  private val getActionsCount: Int
    get() = mActions.size()

  private fun prepareMenu() {
    if (getActionsCount > 0) {
      mPopupMenu.menu.clear()
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        mPopupMenu.gravity = when (mIsAnchoredToRight) {
          true -> Gravity.RIGHT
          false -> Gravity.LEFT
        }
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mPopupMenu.setForceShowIcon(true)
      }
      var i = 0
      while (i < getActionsCount) {
        if (!mActions.isNull(i)) {
          val item = mActions.getMap(i)
          val menuItem = when (item != null && item.hasKey("subactions") && !item.isNull("subactions")) {
            true -> mPopupMenu.menu.addSubMenu(Menu.NONE, Menu.NONE, i, item.getString("title")).item
            else -> mPopupMenu.menu.add(Menu.NONE, Menu.NONE, i, item?.getString("title"))
          }
          prepareMenuItem(mContext, menuItem, item) {
            val args: WritableMap = Arguments.createMap()
            args.putString("event", it)
            args.putString("target", "$id")
            mContext
              .getJSModule(RCTEventEmitter::class.java)
              .receiveEvent(id, "onPressAction", args)
          }
          menuItem.setOnMenuItemClickListener {
            if (!it.hasSubMenu()) {
              val args: WritableMap = Arguments.createMap()
              if (!mActions.isNull(it.order)) {
                val selectedItem = mActions.getMap(it.order)
                args.putString("event", selectedItem?.getString("id"))
                args.putString("target", "$id")
                mContext
                  .getJSModule(RCTEventEmitter::class.java)
                  .receiveEvent(id, "onPressAction", args)
              }
              true
            } else {
              false
            }
          }
        }
        i++
      }
      mPopupMenu.show()
    }
  }
}
