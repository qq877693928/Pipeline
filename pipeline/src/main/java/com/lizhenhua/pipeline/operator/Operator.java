package com.lizhenhua.pipeline.operator;

/**
 * 操作对象接口
 *
 * @param <I> 入参对象类型
 * @param <O> 返回对象类型
 */
public interface Operator<I, O> {
  String PARAM_IS_NULL = "input param is null";
}
