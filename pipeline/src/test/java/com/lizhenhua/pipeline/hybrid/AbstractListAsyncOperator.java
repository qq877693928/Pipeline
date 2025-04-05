package com.lizhenhua.pipeline.hybrid;

import java.util.List;

import com.lizhenhua.pipeline.operator.AsyncOperator;

/**
 * 单测异步操作抽象类
 */
public abstract class AbstractListAsyncOperator extends
    AsyncOperator<List<OperatorResult>, List<OperatorResult>> {
}
