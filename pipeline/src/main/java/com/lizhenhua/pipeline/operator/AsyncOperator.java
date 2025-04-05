package com.lizhenhua.pipeline.operator;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.lizhenhua.pipeline.function.OperatorConsumer;

/**
 * 异步操作类
 *
 * @param <I> 输入值类型
 * @param <O> 输出值类型
 */
public abstract class AsyncOperator<I, O> implements Operator<I, O> {
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
  public void doProcess(I input, OperatorConsumer<O> output, boolean isStop)
      throws IllegalStateException {
    if (input == null) {
      throw new IllegalStateException(Operator.PARAM_IS_NULL);
    }
    if (isStop) {
      return;
    }
    this.process(input, output);
  }

  /**
   * 异常操作处理方法
   * <p>在方法体内，可以指定自定义的线程中运行
   * <p>若想不在主线程执行则通过new Thread()或者线程池的方式切换到异常线程，结果成功返回时，调用{@link OperatorConsumer#accept(Object)}方法
   * <p><note>如果{@link OperatorConsumer#accept(Object)}方法不被调用则流程中断<note/>
   *
   * @param input  输入对象
   * @param output 输出值回调对象
   */
  protected abstract void process(@NonNull I input, @NonNull OperatorConsumer<O> output);
}
