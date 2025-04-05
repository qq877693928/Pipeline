package com.lizhenhua.pipeline.operator;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;

public final class Times {
  /**
   * 获取格式化时间 年-月-日 小时:分钟:秒:毫秒
   * @param date 需要格式化时间
   * @return 时间string(年-月-日 小时:分钟:秒:毫秒)
   */
  public static String formatDateTime(@NonNull Date date) {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    return df.format(date != null ? date : new Date());
  }
}
