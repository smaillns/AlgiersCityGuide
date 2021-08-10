package smaillns.smail.toulouse.view

import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

class GridSpacingItemDecoration() : RecyclerView.ItemDecoration() {

    private var mSpacing: Int = 0


    constructor (spacingPixelSize: Int) : this() {
        mSpacing = spacingPixelSize
    }


    override fun getItemOffsets(outRect: Rect, view: View, recyclerView: RecyclerView, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, recyclerView, state)

        val position = recyclerView.getChildPosition(view)
        val spanCount = getSpanCount(recyclerView)
        val itemCount = recyclerView.adapter.itemCount

        // top offset
        if (position < spanCount) {
            outRect.top = mSpacing
        } else {
            outRect.top = mSpacing / 2
        }

        // bottom offset
        if (itemCount % spanCount == 0 && position >= itemCount - spanCount) {
            outRect.bottom = mSpacing
        } else if (itemCount % spanCount != 0 && position >= itemCount - itemCount % spanCount) {
            outRect.bottom = mSpacing
        } else {
            outRect.bottom = mSpacing / 2
        }

        // left offset
        if (position % spanCount == 0) {
            outRect.left = mSpacing
        } else {
            outRect.left = mSpacing / 2
        }

        // right offset
        if (position % spanCount == spanCount - 1) {
            outRect.right = mSpacing
        } else {
            outRect.right = mSpacing / 2
        }
    }


    private fun getSpanCount(recyclerView: RecyclerView): Int {
        if (recyclerView.layoutManager is GridLayoutManager) {
            val gridLayoutManager = recyclerView.layoutManager as GridLayoutManager
            return gridLayoutManager.spanCount
        } else {
            throw IllegalStateException(this.javaClass.simpleName + " can only be used with a " + GridLayoutManager::class.java.simpleName)
        }
    }


}