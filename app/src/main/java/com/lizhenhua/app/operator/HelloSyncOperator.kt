package com.lizhenhua.app.operator

import com.lizhenhua.pipeline.operator.SyncOperator

class HelloSyncOperator : SyncOperator<MutableList<String>, MutableList<String>>() {

    override fun process(input: MutableList<String>): MutableList<String> {
        input.add("Hello")
        return input
    }
}