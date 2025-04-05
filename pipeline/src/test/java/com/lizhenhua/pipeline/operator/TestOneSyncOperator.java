package com.lizhenhua.pipeline.operator;

public class TestOneSyncOperator extends SyncOperator<String, String> {
  public final static String TAG = "TestOneOperator";

  @Override
  public String process(String input) {
    return getValue(input);
  }

  private String getValue(String input) {
    return input + TAG;
  }
}
