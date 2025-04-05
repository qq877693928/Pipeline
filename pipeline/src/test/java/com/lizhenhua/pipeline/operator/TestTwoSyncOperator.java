package com.lizhenhua.pipeline.operator;

public class TestTwoSyncOperator extends SyncOperator<String, String> {
  public final static String TAG = "TestTwoOperator";

  @Override
  public String process(String input) {
    return getValue(input);
  }

  private String getValue(String input) {
    return input + TAG;
  }
}
