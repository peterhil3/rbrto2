package com.angcyo.dsladapter.internal

import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.FilterParams
import com.angcyo.dsladapter.L

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/05
 */

class HideItemFilterInterceptor : FilterInterceptor {

    /**过滤需要被隐藏后的数据列表*/
    override fun intercept(
        dslAdapter: DslAdapter,
        filterParams: FilterParams,
        requestList: List<DslAdapterItem>
    ): MutableList<DslAdapterItem> {
        val result = mutableListOf<DslAdapterItem>()

        //遍历所有数据, 找出需要隐藏的项
        val hideChildFormItemList = mutableListOf<DslAdapterItem>()

        requestList.forEach { currentItem ->

            requestList.forEachIndexed { index, subEachItem ->
                if (currentItem.isItemInHiddenList(subEachItem, index)) {

                    filterParams.let {
                        //包含需要隐藏的item, 也算updateDependItem
                        if (it.fromDslAdapterItem == currentItem) {
                            it.updateDependItemWithEmpty = true
                        }
                    }

                    hideChildFormItemList.add(subEachItem)
                }
            }

            //表单需要隐藏
            if (currentItem.itemHidden) {
                hideChildFormItemList.add(currentItem)
            }
        }

        var index = 0
        requestList.forEachIndexed { i, dslAdapterItem ->
            if (hideChildFormItemList.contains(dslAdapterItem)) {
                //需要隐藏表单 item

                L.v(
                    "${index++}. 隐藏表单:" +
                            "${dslAdapterItem.javaClass.simpleName} " +
                            "${dslAdapterItem.itemTag ?: ""} " +
                            "index:$i"
                )
            } else {
                result.add(dslAdapterItem)
            }
        }

        return result
    }

}