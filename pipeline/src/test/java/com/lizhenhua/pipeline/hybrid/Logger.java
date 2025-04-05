package com.lizhenhua.pipeline.hybrid;

import java.util.Date;

import com.lizhenhua.pipeline.operator.Times;

public final class Logger {
  public static void printStartMethod(String tag) {
    System.out.println(Times.formatDateTime(new Date()) + " in " + getValidTag(tag));
  }

  public static void printEndMethod(String tag) {
    System.out.println(Times.formatDateTime(new Date()) + " end " + getValidTag(tag));
  }

  private static String getValidTag(String tag) {
    return tag != null ? tag : "";
  }
}
