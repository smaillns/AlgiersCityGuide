package smaillns.smail.dzair.fragment

import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import java.util.*


open class TaskFragment : Fragment() {
    private val mLock = Any()
    private var mReady: Boolean? = false
    private val mPendingCallbacks = LinkedList<Runnable>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        synchronized(mLock) {
            mReady = true
            var pendingCallbacks = mPendingCallbacks.size
            while (pendingCallbacks-- > 0) {
                val runnable = mPendingCallbacks.removeAt(0)
                runNow(runnable)
            }
        }
    }


    override fun onDetach() {
        super.onDetach()
        synchronized(mLock) {
            mReady = false
        }
    }


    protected fun runTaskCallback(runnable: Runnable) {
        if (mReady!!)
            runNow(runnable)
        else
            addPending(runnable)
    }


    protected fun executeTask(task: AsyncTask<Void, *, *>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // use AsyncTask.THREAD_POOL_EXECUTOR or AsyncTask.SERIAL_EXECUTOR
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } else {
            task.execute()
        }
    }


    private fun runNow(runnable: Runnable) {
        //Logcat.d("TaskFragment.runNow(): " + runnable.getClass().getEnclosingMethod());
        activity!!.runOnUiThread(runnable)
    }


    private fun addPending(runnable: Runnable) {
        synchronized(mLock) {
            //Logcat.d("TaskFragment.addPending(): " + runnable.getClass().getEnclosingMethod());
            mPendingCallbacks.add(runnable)
        }
    }
}
