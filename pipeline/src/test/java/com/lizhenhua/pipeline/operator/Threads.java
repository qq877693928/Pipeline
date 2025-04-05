package com.lizhenhua.pipeline.operator;

import org.jetbrains.annotations.Nullable;

public final class Threads {
  /**
   * 格式化线程名称
   * @param thread 线程对象
   * @return thread.name
   */
  public static String name(@Nullable Thread thread) {
    return formatThreadName(thread);
  }

  private static String formatThreadName(Thread thread) {
    if (thread == null) {
      return "";
    } else {
      return "[" + thread.getName() + "]";
    }
  }
}
