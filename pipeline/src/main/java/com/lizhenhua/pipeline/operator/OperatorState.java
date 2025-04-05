package com.lizhenhua.pipeline.operator;

import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class OperatorState {
  /**
   * 是否stop状态
   */
  private final AtomicBoolean isStop = new AtomicBoolean(false);

  public boolean isStop() {
    return isStop.get();
  }

  public void stop(boolean isStop) {
    this.isStop.set(isStop);
  }
}
