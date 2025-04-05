package com.lizhenhua.pipeline.hybrid;

import java.util.Date;
import java.util.List;

import com.lizhenhua.pipeline.operator.Times;
import com.lizhenhua.pipeline.function.OperatorConsumer;
import com.lizhenhua.pipeline.operator.Threads;

public class TestStep1AsyncOperator extends AbstractListAsyncOperator{
  @Override
  public void process(List<OperatorResult> input, OperatorConsumer<List<OperatorResult>> output) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        Logger.printStartMethod("TestStep1AsyncOperator");
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        Logger.printEndMethod("TestStep1AsyncOperator");
        OperatorResult result = new OperatorResult();
        result.calledDate = new Date();
        result.threadName = Threads.name(Thread.currentThread());
        result.extra = TestStep1AsyncOperator.class.getSimpleName();
        input.add(result);
        if (output != null) {
          output.accept(input);
        }
      }
    }).start();
  }
}
