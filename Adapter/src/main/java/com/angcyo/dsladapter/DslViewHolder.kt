package com.angcyo.dsladapter

import android.os.Build
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.angcyo.dsladapter.internal.ThrottleClickListener
import java.lang.ref.WeakReference

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/08/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslViewHolder(
    itemView: View,
    initialCapacity: Int = DEFAULT_INITIAL_CAPACITY
) : ViewHolder(itemView) {

    companion object {
        var DEFAULT_INITIAL_CAPACITY = 32
    }

    val content get() = itemView.context

    /**
     * findViewById是循环枚举所有子View的, 多少也是消耗性能的, +一个缓存
     */
    val sparseArray: SparseArray<WeakReference<View?>> = SparseArray(initialCapacity)

    /**
     * 清理缓存
     */
    fun clear() {
        sparseArray.clear()
    }

    //<editor-fold desc="事件处理">

    /**
     * 单击某个View
     */
    fun clickView(view: View?) {
        view?.performClick()
    }

    fun click(@IdRes id: Int, listener: View.OnClickListener?) {
        val view = v<View>(id)
        view?.setOnClickListener(listener)
    }

    fun click(@IdRes id: Int, listener: (View) -> Unit) {
        click(id, View.OnClickListener { listener.invoke(it) })
    }

    /**节流点击事件*/
    fun throttleClick(@IdRes id: Int, action: (View) -> Unit) {
        click(id, ThrottleClickListener(action = action))
    }

    fun clickItem(listener: View.OnClickListener?) {
        click(itemView, listener)
    }

    fun clickItem(listener: (View) -> Unit) {
        click(itemView, View.OnClickListener { listener.invoke(it) })
    }

    fun throttleClickItem(action: (View) -> Unit) {
        click(itemView, ThrottleClickListener(action = action))
    }

    fun click(view: View?, listener: View.OnClickListener?) {
        view?.setOnClickListener(listener)
    }

    fun click(view: View?, listener: (View) -> Unit) {
        view?.setOnClickListener { listener.invoke(it) }
    }

    //</editor-fold desc="事件处理">

    //<editor-fold desc="post回调">

    fun post(runnable: Runnable) {
        itemView.post(runnable)
    }

    fun post(runnable: () -> Unit) {
        postDelay(0, runnable)
    }

    fun postDelay(runnable: Runnable, delayMillis: Long) {
        itemView.postDelayed(runnable, delayMillis)
    }

    fun postDelay(delayMillis: Long, runnable: Runnable) {
        postDelay(runnable, delayMillis)
    }

    fun postOnAnimation(runnable: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            itemView.postOnAnimation(object : Runnable {
                override fun run() {
                    runnable.invoke()
                    removeCallbacks(this)
                }
            })
        } else {
            itemView.post(object : Runnable {
                override fun run() {
                    runnable.invoke()
                    removeCallbacks(this)
                }
            })
        }
    }

    fun postDelay(delayMillis: Long, runnable: () -> Unit) {
        postDelay(object : Runnable {
            override fun run() {
                runnable.invoke()
                removeCallbacks(this)
            }

        }, delayMillis)
    }

    var _onceRunnbale: Runnable? = null

    fun postOnce(delayMillis: Long = 0, runnable: () -> Unit) {
        removeCallbacks(_onceRunnbale)
        _onceRunnbale = Runnable {
            runnable.invoke()
            removeCallbacks(_onceRunnbale)
        }
        postDelay(_onceRunnbale!!, delayMillis)
    }

    fun removeCallbacks(runnable: Runnable?) {
        itemView.removeCallbacks(runnable)
    }

    //</editor-fold desc="post回调">

    //<editor-fold desc="可见性控制">

    fun <T : View?> focus(@IdRes resId: Int): T? {
        val v = v<View>(resId)
        if (v != null) {
            v.isFocusable = true
            v.isFocusableInTouchMode = true
            v.requestFocus()
            return v as? T
        }
        return null
    }

    fun enable(@IdRes resId: Int, enable: Boolean = true): DslViewHolder {
        val view = v<View>(resId)
        enable(view, enable)
        return this
    }

    private fun enable(view: View?, enable: Boolean) {
        if (view == null) {
            return
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                enable(view.getChildAt(i), enable)
            }
        } else {
            if (view.isEnabled != enable) {
                view.isEnabled = enable
            }
            (view as? EditText)?.clearFocus()
        }
    }

    fun selected(@IdRes resId: Int, selected: Boolean = true): DslViewHolder {
        val view = v<View>(resId)
        selected(view, selected)
        return this
    }

    private fun selected(view: View?, selected: Boolean) {
        if (view == null) {
            return
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                selected(view.getChildAt(i), selected)
            }
        } else {
            if (view.isSelected != selected) {
                view.isSelected = selected
            }
        }
    }

    fun isVisible(@IdRes resId: Int): Boolean {
        return v<View>(resId)?.visibility == View.VISIBLE
    }

    fun visible(@IdRes resId: Int): View? {
        return visible(v<View>(resId))
    }

    fun visible(@IdRes resId: Int, visible: Boolean): DslViewHolder {
        val view = v<View>(resId)
        if (visible) {
            visible(view)
        } else {
            gone(view)
        }
        return this
    }

    fun invisible(@IdRes resId: Int, visible: Boolean): DslViewHolder {
        val view = v<View>(resId)!!
        if (visible) {
            visible(view)
        } else {
            invisible(view)
        }
        return this
    }

    fun visible(view: View?): View? {
        if (view != null) {
            if (view.visibility != View.VISIBLE) {
                view.visibility = View.VISIBLE
            }
        }
        return view
    }

    fun invisible(@IdRes resId: Int): View? {
        return invisible(v<View>(resId))
    }

    fun invisible(view: View?): View? {
        if (view != null) {
            if (view.visibility != View.INVISIBLE) {
                view.clearAnimation()
                view.visibility = View.INVISIBLE
            }
        }
        return view
    }

    fun gone(@IdRes resId: Int): DslViewHolder {
        return gone(v<View>(resId))
    }

    fun gone(@IdRes resId: Int, gone: Boolean) {
        if (gone) {
            gone(v<View>(resId))
        } else {
            visible(resId)
        }
    }

    fun gone(view: View?): DslViewHolder {
        if (view != null) {
            if (view.visibility != View.GONE) {
                view.clearAnimation()
                view.visibility = View.GONE
            }
        }
        return this
    }

    //</editor-fold desc="可见性控制">

    //<editor-fold desc="findViewById">

    fun <T : View?> v(@IdRes resId: Int): T? {
        val viewWeakReference =
            sparseArray[resId]
        var view: View?
        if (viewWeakReference == null) {
            view = itemView.findViewById(resId)
            sparseArray.put(resId, WeakReference(view))
        } else {
            view = viewWeakReference.get()
            if (view == null) {
                view = itemView.findViewById(resId)
                sparseArray.put(resId, WeakReference(view))
            }
        }
        return view as? T
    }

    fun tv(@IdRes resId: Int): TextView? {
        return v(resId)
    }

    fun et(@IdRes resId: Int): EditText? {
        return v(resId)
    }

    fun ev(@IdRes resId: Int): EditText? {
        return v(resId)
    }

    fun img(@IdRes resId: Int): ImageView? {
        return v(resId)
    }

    fun rv(@IdRes resId: Int): RecyclerView? {
        return v(resId)
    }

    fun group(@IdRes resId: Int): ViewGroup? {
        return v(resId)
    }

    fun group(view: View?): ViewGroup? {
        return view as? ViewGroup
    }

    fun view(@IdRes resId: Int): View? {
        return v<View>(resId)
    }

    fun cb(@IdRes resId: Int): CompoundButton? {
        return v(resId)
    }

    //</editor-fold desc="findViewById">

}