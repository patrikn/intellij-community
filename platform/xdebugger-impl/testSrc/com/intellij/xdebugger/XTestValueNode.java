package com.intellij.xdebugger;

import com.intellij.util.NotNullFunction;
import com.intellij.xdebugger.frame.XFullValueEvaluator;
import com.intellij.xdebugger.frame.XValueNode;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.concurrent.Semaphore;

public class XTestValueNode implements XValueNode {
  public String myName;
  public String myType;
  public String myValue;
  public boolean myHasChildren;

  public XFullValueEvaluator myFullValueEvaluator;

  private final Semaphore myFinished = new Semaphore(0);


  @Override
  public void setPresentation(@Nullable Icon icon, @NonNls @Nullable String type, @NonNls @NotNull String value, boolean hasChildren) {
    myType = type;
    myValue = value;
    myHasChildren = hasChildren;

    myFinished.release();
  }

  @Override
  public void setGroupingPresentation(@Nullable Icon icon, @NonNls @Nullable String type, boolean expand) {
    setPresentation(icon, type, "", true);
  }

  @Override
  public void setPresentation(@Nullable Icon icon,
                              @NonNls @Nullable String type,
                              @NonNls @NotNull String separator,
                              @NonNls @NotNull String value,
                              @Nullable NotNullFunction<String, String> valuePresenter,
                              boolean hasChildren) {
    setPresentation(icon, type, value, hasChildren);
  }

  @Override
  public void setPresentation(@Nullable Icon icon,
                              @NonNls @Nullable String type,
                              @NonNls @NotNull String separator,
                              @NonNls @NotNull String value,
                              boolean hasChildren) {
    setPresentation(icon, type, value, hasChildren);
  }

  @Override
  public void setPresentation(@Nullable Icon icon,
                              @NonNls @Nullable String type,
                              @NonNls @NotNull String value,
                              @Nullable NotNullFunction<String, String> valuePresenter,
                              boolean hasChildren) {
    setPresentation(icon, type, value, hasChildren);
  }

  public void setPresentation(@NonNls @NotNull String name,
                              @Nullable Icon icon,
                              @NonNls @Nullable String type,
                              @NonNls @NotNull String value,
                              boolean hasChildren) {
    setPresentation(icon, type, value, hasChildren);
  }

  public void setPresentation(@NonNls @NotNull String name,
                              @Nullable Icon icon,
                              @NonNls @Nullable String type,
                              @NonNls @NotNull String separator,
                              @NonNls @NotNull String value,
                              boolean hasChildren) {
    setPresentation(icon, type, value, hasChildren);
  }

  public void setFullValueEvaluator(@NotNull XFullValueEvaluator fullValueEvaluator) {
    myFullValueEvaluator = fullValueEvaluator;
  }

  public boolean isObsolete() {
    return false;
  }

  public boolean waitFor(long timeoutInMillis) throws InterruptedException {
    return XDebuggerTestUtil.waitFor(myFinished, timeoutInMillis);
  }
}
