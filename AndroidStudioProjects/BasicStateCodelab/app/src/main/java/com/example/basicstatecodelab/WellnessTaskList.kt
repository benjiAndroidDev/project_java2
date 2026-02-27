package com.example.basicstatecodelab

import WellnessTaskItem
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Column
fun getWellnessTask() = List(30) { i -> WellnessTask(i, "Task # $i") }


@Composable
fun WellnessTaskList(
    list: List<WellnessTask>,
    onCloseTask: (WellnessTask) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(
            items = list,
            key = {  task -> task.id}
        ) { task ->}
          WellnessTaskItem(taskName = task.label, onClose = { onCloseTask(task)})
    }
}

