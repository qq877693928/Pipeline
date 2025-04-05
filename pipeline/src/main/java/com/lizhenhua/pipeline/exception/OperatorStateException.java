package com.lizhenhua.pipeline.exception;

import com.lizhenhua.pipeline.Pipeline;

/**
 * 操作状态异常，用于操作状态检查控制，主要用于同步流程检查{@link Pipeline#stop()}抛出异常达到结束流程的目标
 */
public class OperatorStateException extends RuntimeException {
}
