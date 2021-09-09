package me.bytebeats.views.nestedscrolling.app.ui.recycler

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecyclerViewModel : ViewModel() {

    private var times = 0

    private val _listData = MutableLiveData<List<Int>>()

    val listData: LiveData<List<Int>> = _listData

    fun generate(count: Int) {
        val data = mutableListOf<Int>()
        if (_listData?.value != null) {
            data.addAll(_listData!!.value!!)
        }
        for (i in 0 until count) {
            data.add(times * 10 + i)
        }
        _listData.value = data
        times
    }
}