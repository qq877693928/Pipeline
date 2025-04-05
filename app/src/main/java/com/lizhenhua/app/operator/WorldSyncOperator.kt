package com.lizhenhua.app.operator

import com.lizhenhua.pipeline.operator.SyncOperator

class WorldSyncOperator : SyncOperator<MutableList<String>, MutableList<String>>() {

    override fun process(input: MutableList<String>): MutableList<String> {
        input.add("World")
        return input
    }
}