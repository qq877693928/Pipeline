package com.lizhenhua.pipeline.operator;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.lizhenhua.pipeline.exception.OperatorStateException;

/**
 * 同步操作类
 *
 * @param <I> 输入值类型
 * @param <O> 输出值类型
 */
public abstract class SyncOperator<I, O> implements Operator<I, O> {
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
  public O doProcess(I input, boolean isStop) throws IllegalStateException, OperatorStateException {
    if (input == null) {
      throw new IllegalStateException(Operator.PARAM_IS_NULL);
    }
    if (isStop) {
      throw new OperatorStateException();
    }
    return process(input);
  }

  /**
   * 同步操作处理方法
   * @param input 输入值，不为空
   * @return 同步操作处理的结果，结果值不能为空，否则会抛出{@link IllegalStateException}
   */
  @NonNull
  protected abstract O process(@NonNull I input);
}
