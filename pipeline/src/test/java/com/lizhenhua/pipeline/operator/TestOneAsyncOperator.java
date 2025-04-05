package com.lizhenhua.pipeline.operator;

import com.lizhenhua.pipeline.function.OperatorConsumer;

public class TestOneAsyncOperator extends AsyncOperator<String, String> {
  public final static String TAG = "TestOneAsyncOperator";

  private String getValue(String input) {
    return input + Threads.name(Thread.currentThread()) + TAG;
  }

  @Override
  public void process(String input, OperatorConsumer<String> output) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        output.accept(getValue(input));
      }
    }).start();
  }
}
