package com.lizhenhua.pipeline;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import androidx.annotation.NonNull;

import com.lizhenhua.pipeline.function.OperatorConsumer;
import com.lizhenhua.pipeline.hybrid.OperatorResult;
import com.lizhenhua.pipeline.hybrid.TestStep1AsyncOperator;
import com.lizhenhua.pipeline.hybrid.TestStep1SyncOperator;
import com.lizhenhua.pipeline.hybrid.TestStep2AsyncOperator;
import com.lizhenhua.pipeline.hybrid.TestStep2SyncOperator;
import com.lizhenhua.pipeline.hybrid.TestStep3AsyncOperator;
import com.lizhenhua.pipeline.hybrid.TestStep3SyncOperator;
import com.lizhenhua.pipeline.hybrid.TestReturnEmptyResultSyncOperator;
import com.lizhenhua.pipeline.operator.AsyncOperator;
import com.lizhenhua.pipeline.operator.Operator;
import com.lizhenhua.pipeline.operator.SyncOperator;
import com.lizhenhua.pipeline.operator.TestOneAsyncOperator;
import com.lizhenhua.pipeline.operator.TestOneSyncOperator;
import com.lizhenhua.pipeline.operator.TestTwoSyncOperator;
import com.lizhenhua.pipeline.operator.Threads;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTest {
  private static final String INPUT = "test";

  /**
   * 测试构造流程时，传入null的同步操作实例对象
   * <p>1.判断是否会抛异常
   * <p>2.判断异常信息是否符合预期
   */
  @Test
  public void testPipelineWithNullSyncOperator() {
    Throwable exception = assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
      @Override
      public void run() throws Throwable {
        new Pipeline<>((SyncOperator<String, String>) null).execute(INPUT, null);
      }
    });
    assertEquals(Operator.PARAM_IS_NULL, exception.getMessage());
  }

  /**
   * 测试流程只有一个同步操作实例, 入参为null
   * <p>1.判断是否会抛异常
   * <p>2.判断异常信息是否符合预期
   */
  @Test
  public void testPipelineWithOneSyncOperatorNullInput() {
    Throwable exception = assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
      @Override
      public void run() throws Throwable {
        new Pipeline<>(new TestOneSyncOperator()).execute(null, null);
      }
    });
    assertEquals("input is null", exception.getMessage());
  }

  /**
   * 测试流程只有一个同步操作实例, 入参为""
   * <p>1.判断是否以操作结果结束
   * <p>2.判断是否主线程返回
   * <p>3.判断执行结果符合预期
   */
  @Test
  public void testPipelineWithOneSyncOperatorEmptyString() {
    String currentThreadName = Threads.name(Thread.currentThread());
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final String[] value = {null};
    new Pipeline<>(new TestOneSyncOperator()).execute("", s -> {
      value[0] = s;
      assertEquals(currentThreadName, Threads.name(Thread.currentThread()));
      countDownLatch.countDown();
    });
    try {
      countDownLatch.await();
      assertNotNull(value[0]);
      assertTrue(value[0].endsWith(TestOneSyncOperator.TAG));
      assertEquals(value[0], "" + TestOneSyncOperator.TAG);
    } catch (InterruptedException ignore) {
    }
  }

  /**
   * 测试三个同步操作对象，中间有一个操作返回空对象
   */
  @Test
  public void testPipelineWithThreeSyncOperatorAndMiddleNullOperator() {
    Pipeline<List<OperatorResult>, List<OperatorResult>> pipeline = new Pipeline<>(new TestStep1SyncOperator())
        .addSyncOperator(new TestStep2SyncOperator())
        .addSyncOperator(new TestReturnEmptyResultSyncOperator())
        .addSyncOperator(new TestStep3SyncOperator());
    Throwable exception = assertThrows(IllegalStateException.class,
        () -> pipeline.execute(new ArrayList<>(), System.out::println));

    assertEquals(Operator.PARAM_IS_NULL, exception.getMessage());
  }

  /**
   * 测试流程只有一个同步操作实例
   * <p>1.判断返回结果不为null
   * <p>2.判断执行结果是否以入参开始
   * <p>3.判断是否以操作结果结束
   * <p>4.判断是否主线程返回
   * <p>5.判断执行结果符合预期
   */
  @Test
  public void testPipelineWithOneSyncOperator() {
    String currentThreadName = Threads.name(Thread.currentThread());
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final String[] value = {null};
    new Pipeline<>(new TestOneSyncOperator()).execute(INPUT, s -> {
      value[0] = s;
      assertEquals(currentThreadName, Threads.name(Thread.currentThread()));
      countDownLatch.countDown();
    });
    try {
      countDownLatch.await();
      assertNotNull(value);
      assertTrue(value[0].startsWith(INPUT));
      assertTrue(value[0].endsWith(TestOneSyncOperator.TAG));
      assertEquals(value[0], INPUT + TestOneSyncOperator.TAG);
    } catch (InterruptedException ignore) {
    }
  }

  /**
   * 测试两个正序同步操作对象，判断操作结果是否按照正序执行
   * <p>1.判断返回结果不为null
   * <p>2.判断操作以第二个操作结束
   * <p>3.判断是否包含第一个操作
   * <p>4.判断是否主线程返回
   * <p>5.判断执行操作的顺序
   */
  @Test
  public void testPipelineWithTwoPositiveOrderSyncOperator() {
    String currentThreadName = Threads.name(Thread.currentThread());
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final String[] value = {null};
    new Pipeline<>(new TestOneSyncOperator())
        .addSyncOperator(new TestTwoSyncOperator())
        .execute(INPUT, s -> {
          value[0] = s;
          assertEquals(currentThreadName, Threads.name(Thread.currentThread()));
          countDownLatch.countDown();
        });
    try {
      countDownLatch.await();
      assertNotNull(value[0]);

      assertTrue(value[0].startsWith(INPUT));
      assertTrue(value[0].endsWith(TestTwoSyncOperator.TAG));
      assertTrue(value[0].contains(TestOneSyncOperator.TAG));
      assertTrue(value[0].indexOf(INPUT) < value[0].indexOf(TestOneSyncOperator.TAG));
      assertTrue(
          value[0].indexOf(TestOneSyncOperator.TAG) < value[0].indexOf(TestTwoSyncOperator.TAG));
      assertEquals(INPUT + TestOneSyncOperator.TAG + TestTwoSyncOperator.TAG, value[0]);
    } catch (InterruptedException ignored) {
    }
  }

  /**
   * 测试两个逆序同步操作对象，判断操作结果是否按照逆序执行
   * <p>1.判断返回结果不为null
   * <p>2.判断操作以第一个操作结束
   * <p>3.判断是否包含第二个操作
   * <p>4.判断是否主线程返回
   * <p>5.判断执行操作的顺序是否为逆序
   */
  @Test
  public void testPipelineWithTwoReverseOrderSyncOperator() {
    String currentThreadName = Threads.name(Thread.currentThread());
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final String[] value = {null};
    new Pipeline<>(new TestTwoSyncOperator())
        .addSyncOperator(new TestOneSyncOperator())
        .execute(INPUT, s -> {
          value[0] = s;
          assertEquals(currentThreadName, Threads.name(Thread.currentThread()));
          countDownLatch.countDown();
        });

    try {
      countDownLatch.await();
      assertNotNull(value[0]);
      assertTrue(value[0].startsWith(INPUT));
      assertTrue(value[0].endsWith(TestOneSyncOperator.TAG));
      assertTrue(value[0].contains(TestTwoSyncOperator.TAG));
      assertTrue(value[0].indexOf(INPUT) < value[0].indexOf(TestTwoSyncOperator.TAG));
      assertTrue(
          value[0].indexOf(TestTwoSyncOperator.TAG) < value[0].indexOf(TestOneSyncOperator.TAG));
      assertEquals(INPUT + TestTwoSyncOperator.TAG + TestOneSyncOperator.TAG, value[0]);
    } catch (InterruptedException ignored) {
    }
  }

  //===========================异步操作流程================================

  /**
   * 测试构造流程时，传入null的异步操作实例对象
   * <p>1.判断是否会抛异常
   * <p>2.判断异常信息是否符合预期
   */
  @Test
  public void testPipelineWithNullAsyncOperator() {
    Throwable exception = assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
      @Override
      public void run() throws Throwable {
        new Pipeline<>((AsyncOperator<String, String>) null).execute(INPUT, null);
      }
    });
    assertEquals(Operator.PARAM_IS_NULL, exception.getMessage());
  }

  /**
   * 测试流程只有一个异步操作实例, 入参为null
   * <p>1.判断是否会抛异常
   * <p>2.判断异常信息是否符合预期
   */
  @Test
  public void testPipelineWithOneAsyncOperatorNullInput() {
    Throwable exception = assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
      @Override
      public void run() throws Throwable {
        new Pipeline<>(new TestOneAsyncOperator()).execute(null, null);
      }
    });
    assertEquals("input is null", exception.getMessage());
  }


  /**
   * 测试流程只有一个异步操作实例, 入参为""
   * <p>1.判断是否以操作结果结束
   * <p>2.判断是否主线程返回
   * <p>3.判断执行结果符合预期
   */
  @Test
  public void testPipelineWithOneAsyncOperatorEmptyString() {
    String mainThreadName = Threads.name(Thread.currentThread());
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final String[] value = {null};
    new Pipeline<>(new TestOneAsyncOperator()).execute("", s -> {
      value[0] = s;
      assertNotEquals(mainThreadName, Threads.name(Thread.currentThread()));
      countDownLatch.countDown();
    });
    try {
      countDownLatch.await();
      assertNotNull(value[0]);
      assertTrue(value[0].endsWith(TestOneAsyncOperator.TAG));
    } catch (InterruptedException ignore) {
    }
  }


  /**
   * 测试三个异步操作的组合
   * <p>[Thread-0]:TestStep1AsyncOperator
   * <p>[Thread-1]:TestStep2AsyncOperator
   * <p>[Thread-2]:TestStep3AsyncOperator
   * <p>
   * <p>1.判断回调的线程不在主线程
   * <p>2.判断返回的结果不为空
   * <p>3.判断返回的结果列表为3
   * <p>4.判断列表的顺序是否正确
   * <p>5.判断每一步操作线程都不在主线程
   * <p>6.判断操作时间都间隔
   */
  @Test
  public void testPipelineWithThreadAsyncOperator() {
    String mainThreadName = Threads.name(Thread.currentThread());
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final List<OperatorResult>[] resultList = new List[]{null};
    new Pipeline<>(new TestStep1AsyncOperator())
        .addAsyncOperator(new TestStep2AsyncOperator())
        .addAsyncOperator(new TestStep3AsyncOperator())
        .execute(new ArrayList<>(),
            new OperatorConsumer<List<OperatorResult>>() {
              @Override
              public void accept(List<OperatorResult> operatorResults) {
                assertNotEquals(mainThreadName, Threads.name(Thread.currentThread()));
                resultList[0] = operatorResults;
                countDownLatch.countDown();
              }
            });
    try {
      countDownLatch.await();

      assertNotNull(resultList[0]);
      assertEquals(3, resultList[0].size());
      // 预期的执行顺序
      String[] prospectiveSequenceArray = new String[]{
          TestStep1AsyncOperator.class.getSimpleName(),
          TestStep2AsyncOperator.class.getSimpleName(),
          TestStep3AsyncOperator.class.getSimpleName()
      };
      // 判断列表的顺序是否正确
      assertArrayEquals(prospectiveSequenceArray,
          resultList[0].stream().map(operatorResult -> operatorResult.extra).toArray());

      // 判断每一步操作线程都不在主线程
      resultList[0].forEach(
          operatorResult -> assertNotEquals(mainThreadName, operatorResult.threadName));

      for (int i = 1; i < resultList[0].size(); i++) {
        assertTrue(resultList[0].get(i).calledDate.getTime() > resultList[0].get(i - 1).calledDate.getTime());
      }
    } catch (InterruptedException ignore) {
    }
  }

  /**
   * 测试三个异步操作对象，中间有一个操作返回空对象
   */
  @Test
  public void testPipelineWithThreeAsyncOperatorAndMiddleNullOperator() {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final List<OperatorResult>[] resultList = new List[]{null};
    new Pipeline<>(new TestStep1AsyncOperator())
        .addAsyncOperator(new TestStep2AsyncOperator())
        .addAsyncOperator(new AsyncOperator<List<OperatorResult>, List<OperatorResult>>() {
          @Override
          protected void process(@NonNull @NotNull List<OperatorResult> input,
              @NonNull @NotNull OperatorConsumer<List<OperatorResult>> output) {
            Throwable throwable = assertThrows(IllegalStateException.class, new ThrowingRunnable() {
              @Override
              public void run() throws Throwable {
                if (output != null) {
                  output.accept(null);
                }
              }
            });
            assertEquals(Operator.PARAM_IS_NULL, throwable.getMessage());
            if (throwable != null) {
              countDownLatch.countDown();
            }
          }
        })
        .addAsyncOperator(new TestStep3AsyncOperator())
        .execute(new ArrayList<>(), results -> {
          resultList[0] = results;
          countDownLatch.countDown();
        });

    try {
      countDownLatch.await();
      assertNull(resultList[0]);
    } catch (InterruptedException ignore) {
    }
  }

  //===========================混合（异步+同步）操作流程================================
  //1.一个同步一个异步
  //2.一个异步一个同步
  //3.三个同步三个异步
  //4.三个异步三个同步
  //5.同步+异步+同步
  //6.异步+同步+异步

  /**
   * 测试一个同步+一个异步操作的组合
   * <p>[main]:TestStep1SyncOperator
   * <p>[Thread-0]:TestStep1AsyncOperator
   * <p>
   * <p>1.判断回调的线程不在主线程
   * <p>2.判断返回的结果不为空
   * <p>3.判断返回的结果列表长度
   * <p>4.判断列表的顺序是否正确
   * <p>5.检查每一步操作线程线程
   * <p>6.判断回调时间
   */
  @Test
  public void testPipelineWithOneSyncAndOneAsync() {
    String mainThreadName = Threads.name(Thread.currentThread());
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final long methodCallFinishedTime;
    final List<OperatorResult>[] resultList = new List[]{null};
    new Pipeline<>(new TestStep1SyncOperator())
        .addAsyncOperator(new TestStep1AsyncOperator())
        .execute(new ArrayList<>(), new OperatorConsumer<List<OperatorResult>>() {
          @Override
          public void accept(List<OperatorResult> operatorResults) {
            resultList[0] = operatorResults;
            assertNotEquals(mainThreadName, Threads.name(Thread.currentThread()));
            countDownLatch.countDown();
          }
        });
    methodCallFinishedTime = new Date().getTime();
    try {
      countDownLatch.await();

      assertNotNull(resultList);
      assertEquals(2, resultList[0].size());
      // 预期的执行顺序
      String[] prospectiveSequenceArray = new String[]{
          TestStep1SyncOperator.class.getSimpleName(),
          TestStep1AsyncOperator.class.getSimpleName(),
      };
      // 判断列表的顺序是否正确
      assertArrayEquals(prospectiveSequenceArray,
          resultList[0].stream().map(operatorResult -> operatorResult.extra).toArray());

      // 第一个在主线程
      assertEquals(resultList[0].get(0).threadName, mainThreadName);
      // 第二个不在主线程
      assertNotEquals(resultList[0].get(resultList[0].size() - 1).threadName, mainThreadName);

      // 调用new Pipeline结束时间比第二个同步线程早
      assertTrue(resultList[0].get(resultList[0].size() - 1).calledDate.getTime() >
          methodCallFinishedTime);
    } catch (InterruptedException ignore) {
    }
  }

  /**
   * 测试一个同步+一个异步操作的组合
   * <p>[Thread-0]:TestStep1AsyncOperator
   * <p>[Thread-0]:TestStep1SyncOperator(第二个不主动切换线程则线程和上一个流程保持一致)
   * <p>
   * <p>1.判断回调的线程不在主线程
   * <p>2.判断返回的结果不为空
   * <p>3.判断返回的结果列表长度
   * <p>4.判断列表的顺序是否正确
   * <p>5.检查每一步操作线程线程
   * <p>6.判断回调时间
   */
  @Test
  public void testPipelineWithOneAsyncAndOneSync() {
    String mainThreadName = Threads.name(Thread.currentThread());
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final List<OperatorResult>[] resultList = new List[]{null};
    final long methodCallFinishedTime;
    new Pipeline<>(new TestStep1AsyncOperator())
        .addSyncOperator(new TestStep1SyncOperator())
        .execute(new ArrayList<>(), new OperatorConsumer<List<OperatorResult>>() {
          @Override
          public void accept(List<OperatorResult> operatorResults) {
            assertNotEquals(mainThreadName, Threads.name(Thread.currentThread()));
            resultList[0] = operatorResults;
            countDownLatch.countDown();
          }
        });
    methodCallFinishedTime = new Date().getTime();
    try {
      countDownLatch.await();

      assertNotNull(resultList[0]);
      assertEquals(2, resultList[0].size());
      // 预期的执行顺序
      String[] prospectiveSequenceArray = new String[]{
          TestStep1AsyncOperator.class.getSimpleName(),
          TestStep1SyncOperator.class.getSimpleName(),
      };
      // 判断列表的顺序是否正确
      assertArrayEquals(prospectiveSequenceArray,
          resultList[0].stream().map(operatorResult -> operatorResult.extra).toArray());

      resultList[0].forEach(
          operatorResult -> {
            assertNotEquals(operatorResult.threadName, mainThreadName);
            //由于第一个是异步操作，所以不阻塞调用方法
            assertTrue(operatorResult.calledDate.getTime() > methodCallFinishedTime);
          });
    } catch (InterruptedException ignore) {
    }
  }

  /**
   * 测试三个同步+三个异步操作的组合
   * <p>[main]:TestStep1SyncOperator
   * <p>[main]:TestStep2SyncOperator
   * <p>[main]:TestStep3SyncOperator
   * <p>[Thread-0]:TestStep1AsyncOperator
   * <p>[Thread-1]:TestStep2AsyncOperator
   * <p>[Thread-2]:TestStep3AsyncOperator
   * <p>
   * <p>1.判断回调的线程不在主线程
   * <p>2.判断返回的结果不为空
   * <p>3.判断返回的结果列表长度
   * <p>4.判断列表的顺序是否正确
   * <p>5.检查每一步操作线程线程
   * <p>6.判断回调时间
   */
  @Test
  public void testPipelineWithThreeSyncAndThreeAsync() {
    String mainThreadName = Threads.name(Thread.currentThread());
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final List<OperatorResult>[] resultList = new List[]{null};
    final long methodCallFinishedTime;
    new Pipeline<>(new TestStep1SyncOperator())
        .addSyncOperator(new TestStep2SyncOperator())
        .addSyncOperator(new TestStep3SyncOperator())
        .addAsyncOperator(new TestStep1AsyncOperator())
        .addAsyncOperator(new TestStep2AsyncOperator())
        .addAsyncOperator(new TestStep3AsyncOperator())
        .execute(new ArrayList<>(), new OperatorConsumer<List<OperatorResult>>() {
          @Override
          public void accept(List<OperatorResult> operatorResults) {
            assertNotEquals(mainThreadName, Threads.name(Thread.currentThread()));
            resultList[0] = operatorResults;
            countDownLatch.countDown();
          }
        });
    methodCallFinishedTime = new Date().getTime();
    try {
      countDownLatch.await();

      assertNotNull(resultList[0]);
      assertEquals(6, resultList[0].size());

      // 预期的执行顺序
      String[] prospectiveSequenceArray = new String[]{
          TestStep1SyncOperator.class.getSimpleName(),
          TestStep2SyncOperator.class.getSimpleName(),
          TestStep3SyncOperator.class.getSimpleName(),
          TestStep1AsyncOperator.class.getSimpleName(),
          TestStep2AsyncOperator.class.getSimpleName(),
          TestStep3AsyncOperator.class.getSimpleName(),
      };
      // 判断列表的顺序是否正确
      assertArrayEquals(prospectiveSequenceArray,
          resultList[0].stream().map(operatorResult -> operatorResult.extra).toArray());

      for (int i = 0; i < 3; i++) {
        // 前三个在主线程
        assertEquals(resultList[0].get(i).threadName, mainThreadName);
      }

      for (int i = 3; i < resultList[0].size(); i++) {
        // 后三个不在主线程
        assertNotEquals(resultList[0].get(i).threadName, mainThreadName);
        assertTrue(resultList[0].get(i).calledDate.getTime() > methodCallFinishedTime);
      }
    } catch (InterruptedException ignore) {
    }
  }

  /**
   * 测试三个异步+三个同步操作的组合
   * <p>[Thread-0]:TestStep1AsyncOperator
   * <p>[Thread-1]:TestStep2AsyncOperator
   * <p>[Thread-2]:TestStep3AsyncOperator
   * <p>[Thread-2]:TestStep1SyncOperator
   * <p>[Thread-2]:TestStep2SyncOperator
   * <p>[Thread-2]:TestStep3SyncOperator
   * <p>
   * <p>1.判断回调的线程不在主线程
   * <p>2.判断返回的结果不为空
   * <p>3.判断返回的结果列表长度
   * <p>4.判断列表的顺序是否正确
   * <p>5.检查每一步操作线程线程
   * <p>6.判断回调时间
   */
  @Test
  public void testPipelineWithThreeAsyncAndThreeSync() {
    String mainThreadName = Threads.name(Thread.currentThread());
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final List<OperatorResult>[] resultList = new List[]{null};
    final long methodCallFinishedTime;
    new Pipeline<>(new TestStep1AsyncOperator())
        .addAsyncOperator(new TestStep2AsyncOperator())
        .addAsyncOperator(new TestStep3AsyncOperator())
        .addSyncOperator(new TestStep1SyncOperator())
        .addSyncOperator(new TestStep2SyncOperator())
        .addSyncOperator(new TestStep3SyncOperator())
        .execute(new ArrayList<>(), new OperatorConsumer<List<OperatorResult>>() {
          @Override
          public void accept(List<OperatorResult> operatorResults) {
            assertNotEquals(mainThreadName, Threads.name(Thread.currentThread()));
            resultList[0] = operatorResults;
            countDownLatch.countDown();
          }
        });
    methodCallFinishedTime = new Date().getTime();
    try {
      countDownLatch.await();

      assertNotNull(resultList[0]);
      assertEquals(6, resultList[0].size());

      // 预期的执行顺序
      String[] prospectiveSequenceArray = new String[]{
          TestStep1AsyncOperator.class.getSimpleName(),
          TestStep2AsyncOperator.class.getSimpleName(),
          TestStep3AsyncOperator.class.getSimpleName(),
          TestStep1SyncOperator.class.getSimpleName(),
          TestStep2SyncOperator.class.getSimpleName(),
          TestStep3SyncOperator.class.getSimpleName(),
      };
      // 判断列表的顺序是否正确
      assertArrayEquals(prospectiveSequenceArray,
          resultList[0].stream().map(operatorResult -> operatorResult.extra).toArray());

      String lastAsyncThreadName = "";
      for (int i = 0; i < resultList[0].size(); i++) {
        lastAsyncThreadName = resultList[0].get(i).threadName;
        assertNotEquals(resultList[0].get(i).threadName, mainThreadName);
        assertTrue(resultList[0].get(i).calledDate.getTime() > methodCallFinishedTime);
      }

      for (int i = 2; i < resultList[0].size(); i++) {
        // 后四个在同一线程
        assertEquals(resultList[0].get(i).threadName, lastAsyncThreadName);
      }
    } catch (InterruptedException ignore) {
    }
  }

  /**
   * 测试异步+同步+异步操作的组合
   * <p>[Thread-0]:TestStep1AsyncOperator
   * <p>[Thread-1]:TestStep2AsyncOperator
   * <p>[Thread-1]:TestStep1SyncOperator
   * <p>[Thread-1]:TestStep2SyncOperator
   * <p>[Thread-2]:TestStep3AsyncOperator
   * <p>
   * <p>1.判断回调的线程不在主线程
   * <p>2.判断返回的结果不为空
   * <p>3.判断返回的结果列表长度
   * <p>4.判断列表的顺序是否正确
   * <p>5.检查每一步操作线程线程
   * <p>6.判断回调时间
   */
  @Test
  public void testPipelineWithSyncThenAsyncThenSync() {
    String mainThreadName = Threads.name(Thread.currentThread());
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final List<OperatorResult>[] resultList = new List[]{null};
    final long methodCallFinishedTime;
    new Pipeline<>(new TestStep1AsyncOperator())
        .addAsyncOperator(new TestStep2AsyncOperator())
        .addSyncOperator(new TestStep1SyncOperator())
        .addSyncOperator(new TestStep2SyncOperator())
        .addAsyncOperator(new TestStep3AsyncOperator())
        .execute(new ArrayList<>(), new OperatorConsumer<List<OperatorResult>>() {
          @Override
          public void accept(List<OperatorResult> operatorResults) {
            assertNotEquals(mainThreadName, Threads.name(Thread.currentThread()));
            resultList[0] = operatorResults;
            countDownLatch.countDown();
          }
        });

    methodCallFinishedTime = new Date().getTime();
    try {
      countDownLatch.await();

      assertNotNull(resultList[0]);
      assertEquals(5, resultList[0].size());

      // 预期的执行顺序
      String[] prospectiveSequenceArray = new String[]{
          TestStep1AsyncOperator.class.getSimpleName(),
          TestStep2AsyncOperator.class.getSimpleName(),
          TestStep1SyncOperator.class.getSimpleName(),
          TestStep2SyncOperator.class.getSimpleName(),
          TestStep3AsyncOperator.class.getSimpleName(),
      };

      // 判断列表的顺序是否正确
      assertArrayEquals(prospectiveSequenceArray,
          resultList[0].stream().map(operatorResult -> operatorResult.extra).toArray());

      for (int i = 0; i < resultList[0].size(); i++) {
        assertNotEquals(resultList[0].get(i).threadName, mainThreadName);
        assertTrue(resultList[0].get(i).calledDate.getTime() > methodCallFinishedTime);
      }

      String middleAsyncThreadName = resultList[0].get(1).threadName;
      for (int i = 1; i < resultList[0].size() - 1; i++) {
        // 后四个在同一线程
        assertEquals(resultList[0].get(i).threadName, middleAsyncThreadName);
      }

    } catch (InterruptedException ignore) {
    }
  }

  /**
   * 测试同步+异步+同步操作的组合
   * <p>[main]:TestStep1SyncOperator
   * <p>[main]:TestStep2SyncOperator
   * <p>[Thread-0]:TestStep1AsyncOperator
   * <p>[Thread-1]:TestStep2AsyncOperator
   * <p>[Thread-1]:TestStep3SyncOperator
   * <p>
   * <p>1.判断回调的线程不在主线程
   * <p>2.判断返回的结果不为空
   * <p>3.判断返回的结果列表长度
   * <p>4.判断列表的顺序是否正确
   * <p>5.检查每一步操作线程线程
   * <p>6.判断回调时间
   */
  @Test
  public void testPipelineWithAsyncThenSyncThenAsync() {
    String mainThreadName = Threads.name(Thread.currentThread());
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final List<OperatorResult>[] resultList = new List[]{null};
    final long methodCallFinishedTime;
    new Pipeline<>(new TestStep1SyncOperator())
        .addSyncOperator(new TestStep2SyncOperator())
        .addAsyncOperator(new TestStep1AsyncOperator())
        .addAsyncOperator(new TestStep2AsyncOperator())
        .addSyncOperator(new TestStep3SyncOperator())
        .execute(new ArrayList<>(), new OperatorConsumer<List<OperatorResult>>() {
          @Override
          public void accept(List<OperatorResult> operatorResults) {
            assertNotEquals(mainThreadName, Threads.name(Thread.currentThread()));
            resultList[0] = operatorResults;
            countDownLatch.countDown();
          }
        });

    methodCallFinishedTime = new Date().getTime();
    try {
      countDownLatch.await();

      assertNotNull(resultList[0]);
      assertEquals(5, resultList[0].size());

      // 预期的执行顺序
      String[] prospectiveSequenceArray = new String[]{
          TestStep1SyncOperator.class.getSimpleName(),
          TestStep2SyncOperator.class.getSimpleName(),
          TestStep1AsyncOperator.class.getSimpleName(),
          TestStep2AsyncOperator.class.getSimpleName(),
          TestStep3SyncOperator.class.getSimpleName(),
      };

      // 判断列表的顺序是否正确
      assertArrayEquals(prospectiveSequenceArray,
          resultList[0].stream().map(operatorResult -> operatorResult.extra).toArray());

      for (int i = 2; i < resultList[0].size(); i++) {
        assertNotEquals(resultList[0].get(i).threadName, mainThreadName);
        assertTrue(resultList[0].get(i).calledDate.getTime() > methodCallFinishedTime);
      }
    } catch (InterruptedException ignore) {
    }
  }

  //===========================取消（异步+同步）操作流程================================
  //1.三个同步取消
  //2.三个异步取消

  /**
   * 执行已经停止的pipeline是否报错
   * <p>1.构建pipeline with 3个同步线程
   * <p>2.在pipeline执行完结束之前调用stop()
   * <p>3.继续调用execute(),检查是否报异常
   * <p>4.listener没有回调，无返回值
   */
  @Test
  public void testThreeSyncPipelineExecuteWithStopState() {
    final Throwable[] exception = {null};
    final List<OperatorResult>[] resultList = new List[]{null};
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final Pipeline<List<OperatorResult>, List<OperatorResult>> pipeline =
        new Pipeline<>(new TestStep1SyncOperator())
            .addSyncOperator(new TestStep2SyncOperator())
            .addAsyncOperator(new TestStep1AsyncOperator());


    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      public void run() {
        pipeline.stop();
        exception[0] = assertThrows(IllegalStateException.class,
            () -> pipeline.execute(new ArrayList<>(), new OperatorConsumer<List<OperatorResult>>() {
              @Override
              public void accept(List<OperatorResult> operatorResults) {

              }
            }));
        countDownLatch.countDown();
        this.cancel();
      }
    }, 100);

    pipeline.execute(new ArrayList<>(), new OperatorConsumer<List<OperatorResult>>() {
      @Override
      public void accept(List<OperatorResult> operatorResults) {
        resultList[0] = operatorResults;
        countDownLatch.countDown();
      }
    });

    try {
      countDownLatch.await();

      assertNull(resultList[0]);
      assertNotNull(exception[0]);
      assertEquals("pipeline is stop", exception[0].getMessage());

    } catch (InterruptedException ignore) {

    }
  }

  /**
   * 执行已经停止的pipeline是否报错
   * <p>1.构建pipeline with 3个异步线程
   * <p>2.在pipeline执行完结束之前调用stop()
   * <p>3.继续调用execute(),检查是否报异常
   * <p>4.listener没有回调，无返回值
   */
  @Test
  public void testThreeAsyncPipelineExecuteWithStopState() {
    final Throwable[] exception = {null};
    final List<OperatorResult>[] resultList = new List[]{null};
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final Pipeline<List<OperatorResult>, List<OperatorResult>> pipeline =
        new Pipeline<>(new TestStep1AsyncOperator())
            .addAsyncOperator(new TestStep2AsyncOperator())
            .addAsyncOperator(new TestStep3AsyncOperator());


    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      public void run() {
        pipeline.stop();
        exception[0] = assertThrows(IllegalStateException.class,
            () -> pipeline.execute(new ArrayList<>(), new OperatorConsumer<List<OperatorResult>>() {
              @Override
              public void accept(List<OperatorResult> operatorResults) {

              }
            }));
        countDownLatch.countDown();
        this.cancel();
      }
    }, 100);

    pipeline.execute(new ArrayList<>(), new OperatorConsumer<List<OperatorResult>>() {
      @Override
      public void accept(List<OperatorResult> operatorResults) {
        resultList[0] = operatorResults;
        countDownLatch.countDown();
      }
    });

    try {
      countDownLatch.await();

      assertNull(resultList[0]);
      assertNotNull(exception[0]);
      assertEquals("pipeline is stop", exception[0].getMessage());

    } catch (InterruptedException ignore) {

    }
  }
}