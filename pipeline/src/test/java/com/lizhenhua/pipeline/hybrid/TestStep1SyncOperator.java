package com.lizhenhua.pipeline.hybrid;

import java.util.Date;
import java.util.List;

import com.lizhenhua.pipeline.operator.Times;
import com.lizhenhua.pipeline.operator.Threads;

public class TestStep1SyncOperator extends AbstractListSyncOperator{
  @Override
  public List<OperatorResult> process(List<OperatorResult> input) {
    Logger.printStartMethod("TestStep1SyncOperator");
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Logger.printEndMethod("TestStep1SyncOperator");
    OperatorResult result = new OperatorResult();
    result.calledDate = new Date();
    result.threadName = Threads.name(Thread.currentThread());
    result.extra = TestStep1SyncOperator.class.getSimpleName();
    input.add(result);
    return input;
  }
}
