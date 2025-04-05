package com.lizhenhua.pipeline.hybrid;

import java.util.List;

import com.lizhenhua.pipeline.operator.SyncOperator;

/**
 * 单测同步操作抽象类
 */
public abstract class AbstractListSyncOperator extends SyncOperator<List<OperatorResult>, List<OperatorResult>> {
}
