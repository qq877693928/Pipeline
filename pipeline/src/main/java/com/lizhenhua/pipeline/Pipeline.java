package com.lizhenhua.pipeline;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lizhenhua.pipeline.exception.OperatorStateException;
import com.lizhenhua.pipeline.function.OperatorConsumer;
import com.lizhenhua.pipeline.operator.AsyncOperator;
import com.lizhenhua.pipeline.operator.Operator;
import com.lizhenhua.pipeline.operator.OperatorState;
import com.lizhenhua.pipeline.operator.SyncOperator;

public class Pipeline<I, O> implements IDisposable {
  private final static String TAG = "AdLink-" + "Pipeline";
  @NonNull
  private final Operator<I, O> currentOperator;
  /**
   * 当前Pipeline的状态，用于调用{@link Pipeline#stop()}方法后标注状态
   */
  private final OperatorState currentState;

  /**
   * 构造Pipeline
   *
   * @param syncOperator 同步操作流程{@link SyncOperator}, 如果传参为空则会抛出异常{@link IllegalArgumentException}
   */
  public Pipeline(@NonNull SyncOperator<I, O> syncOperator) throws IllegalArgumentException {
    this(syncOperator, new OperatorState());
  }

  /**
   * 构造Pipeline
   *
   * @param asyncOperator 异步操作流程{@link AsyncOperator}, 如果传参为空则会抛出异常{@link IllegalArgumentException}
   */
  public Pipeline(@NonNull AsyncOperator<I, O> asyncOperator) throws IllegalArgumentException {
    this(asyncOperator, new OperatorState());
  }

  private Pipeline(@NonNull SyncOperator<I, O> syncOperator, @NonNull OperatorState operatorState) {
    if (syncOperator == null) {
      throw new IllegalArgumentException(Operator.PARAM_IS_NULL);
    }
    this.currentOperator = syncOperator;
    this.currentState = operatorState;
  }

  private Pipeline(@NonNull AsyncOperator<I, O> asyncOperator, @NonNull OperatorState operatorState) {
    if (asyncOperator == null) {
      throw new IllegalArgumentException(Operator.PARAM_IS_NULL);
    }
    this.currentOperator = asyncOperator;
    this.currentState = operatorState;
  }

  /**
   * 添加同步操作流程
   *
   * @param newSyncOperator 待添加的同步操作流程，不能为空，为空则会抛出NPE
   * @param <T>             输出对象类型
   * @return Pipeline实体
   */
  public <T> Pipeline<I, T> addSyncOperator(@NonNull SyncOperator<O, T> newSyncOperator) {
    if (isAsyncOperator()) {
      return new Pipeline<>(new AsyncOperator<I, T>() {
        @Override
        public void process(I input, OperatorConsumer<T> outputConsumer) {
          ((AsyncOperator<I, O>) currentOperator).doProcess(input, new OperatorConsumer<O>() {
            @Override
            public void accept(O output) {
              try {
                outputConsumer.accept(newSyncOperator.doProcess(output, currentState.isStop()));
              } catch (OperatorStateException | IllegalStateException ignore) {
              }
            }
          }, currentState.isStop());
        }
      }, currentState);
    } else {
      return new Pipeline<I, T>(new SyncOperator<I, T>() {
        @Override
        public T process(I input) {
          return newSyncOperator
              .doProcess(((SyncOperator<I, O>) currentOperator).doProcess(input, currentState.isStop()),
                  currentState.isStop());
        }
      }, currentState);
    }
  }

  /**
   * 添加异步操作流程
   *
   * @param newAsyncOperator 待添加的异步操作流程，不能为空, 为空则会抛出NPE
   * @param <T>              输出对象类型
   * @return Pipeline实体
   */
  public <T> Pipeline<I, T> addAsyncOperator(@NonNull AsyncOperator<O, T> newAsyncOperator) {
    if (isAsyncOperator()) {
      return new Pipeline<>(new AsyncOperator<I, T>() {
        @Override
        protected void process(@NonNull I input,
            @NonNull OperatorConsumer<T> outputConsumer) {
          ((AsyncOperator<I, O>) currentOperator).doProcess(input, new OperatorConsumer<O>() {
            @Override
            public void accept(O output) {
              newAsyncOperator.doProcess(output, outputConsumer, currentState.isStop());
            }
          }, currentState.isStop());
        }
      }, currentState);
    } else {
      return new Pipeline<>(new AsyncOperator<I, T>() {
        @Override
        public void process(I input, OperatorConsumer<T> outputConsumer) {
          newAsyncOperator
              .doProcess(((SyncOperator<I, O>) currentOperator).doProcess(input, currentState.isStop()),
                  outputConsumer, currentState.isStop());
        }
      }, currentState);
    }
  }

  /**
   * 执行Pipeline流程
   *
   * @param input          输入值<I>，不能为空否则抛异常{@link IllegalArgumentException}
   * @param outputConsumer 操作回调Consumer对象
   */
  public void execute(I input, @Nullable OperatorConsumer<O> outputConsumer)
      throws IllegalArgumentException, IllegalStateException {
    if (input == null) {
      throw new IllegalArgumentException("input is null");
    }
    if (currentState.isStop()) {
      throw new IllegalStateException("pipeline is stop");
    }
    try {
      if (isAsyncOperator()) {
        ((AsyncOperator<I, O>) currentOperator).doProcess(input, outputConsumer, currentState.isStop());
      } else {
        //当前操作对象为同步操作类时
        O returnObject = ((SyncOperator<I, O>) currentOperator).doProcess(input, currentState.isStop());
        if (outputConsumer != null) {
          outputConsumer.accept(returnObject);
        }
      }
    } catch (OperatorStateException ignore) {
    }
  }

  /**
   * 停止Pipeline流程
   */
  @Override
  @AnyThread
  public void stop() {
    currentState.stop(true);
  }

  private boolean isAsyncOperator() {
    // 当前的操作是否是异步流程
    return currentOperator instanceof AsyncOperator;
  }
}