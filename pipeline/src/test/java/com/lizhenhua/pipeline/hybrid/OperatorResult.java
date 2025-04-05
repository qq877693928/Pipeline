package com.lizhenhua.pipeline.hybrid;

import java.util.Date;

import com.lizhenhua.pipeline.operator.Times;

public class OperatorResult {
  /**
   * 操作被调用时间
   */
  public Date calledDate;
  /**
   * 执行时的线程名称
   */
  public String threadName;
  /**
   * 执行时信息
   */
  public String extra;

  @Override
  public String toString() {
    return "OperatorResult{" +
        "date=" + Times.formatDateTime(calledDate) +
        ", thread='" + threadName + '\'' +
        ", extra='" + extra + '\'' +
        '}';
  }
}
