package com.lizhenhua.pipeline.hybrid;

import java.util.Date;
import java.util.List;

import com.lizhenhua.pipeline.operator.Times;

public class TestReturnEmptyResultSyncOperator extends AbstractListSyncOperator{
  @Override
  public List<OperatorResult> process(List<OperatorResult> input) {
    Logger.printStartMethod(TestReturnEmptyResultSyncOperator.class.getSimpleName());
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Logger.printEndMethod(TestReturnEmptyResultSyncOperator.class.getSimpleName());
    return null;
  }
}
