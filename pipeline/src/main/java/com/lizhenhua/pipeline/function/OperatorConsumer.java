package com.lizhenhua.pipeline.function;

/**
 * 操作回调类
 */
public interface OperatorConsumer<T> {
  /**
   * 回调结果
   *
   * @param result 操作结果
   */
  void accept(T result);
}
