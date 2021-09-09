package me.bytebeats.views.nestedscrolling.app.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ListViewModel : ViewModel() {
    private var times = 0

    private val _listData = MutableLiveData<List<String>>()

    val listData: LiveData<List<String>> = _listData

    fun generate(count: Int) {
        val data = mutableListOf<String>()
        if (_listData?.value != null) {
            data.addAll(_listData!!.value!!)
        }
        for (i in 0 until count) {
            data.add("Text ${times * 10 + i}")
        }
        _listData.value = data
        times
    }
}