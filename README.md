# Pipeline
Pipeline管道模式，简化数据流转过程，解耦流程之间的依赖，支持同步流程和异步流程混排

## 使用说明
### 简单使用
```kotlin
// 添加同步操作
val pipeline: Pipeline<MutableList<String>, MutableList<String>> = Pipeline(object: SyncOperator<MutableList<String>, MutableList<String>>() {
    override fun process(input: MutableList<String>): MutableList<String> {
        input.add("Hello")
        return input
    }
})
// 添加异步操作
pipeline.addAsyncOperator(object: AsyncOperator<MutableList<String>, MutableList<String>>() {
    override fun process(input: MutableList<String>, output: OperatorConsumer<MutableList<String>>) {
        input.add("World")
        output.accept(input)
    }
})
```

### 流程封装
简化流程代码，封装Operator

HelloSyncOperator.kt
```kotlin
class HelloSyncOperator : SyncOperator<MutableList<String>, MutableList<String>>() {
    override fun process(input: MutableList<String>): MutableList<String> {
        input.add("Hello")
        return input
    }
}
```


WorldAsyncOperator.kt
```kotlin
class WorldAsyncOperator : AsyncOperator<MutableList<String>, MutableList<String>>() {
    override fun process(
        input: MutableList<String>,
        output: OperatorConsumer<MutableList<String>>
    ) {
        input.add("World")
        output.accept(input)
    }
}
```

调用方法
```kotlin
val pipeline: Pipeline<MutableList<String>, MutableList<String>> =
    Pipeline(HelloSyncOperator()).addAsyncOperator(WorldAsyncOperator())
pipeline.execute(mutableListOf()) { list->
    TODO("Not yet implemented")
}
```

