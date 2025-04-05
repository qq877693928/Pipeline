package com.lizhenhua.app.operator

import com.lizhenhua.pipeline.function.OperatorConsumer
import com.lizhenhua.pipeline.operator.AsyncOperator
import com.lizhenhua.pipeline.operator.SyncOperator

class HelloAsyncOperator : AsyncOperator<MutableList<String>, MutableList<String>>() {

    override fun process(
        input: MutableList<String>,
        output: OperatorConsumer<MutableList<String>>
    ) {
        input.add("Hello")
        output.accept(input)
    }
}