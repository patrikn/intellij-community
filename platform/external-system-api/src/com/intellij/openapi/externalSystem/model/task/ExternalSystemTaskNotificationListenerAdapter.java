package com.intellij.openapi.externalSystem.model.task;

import org.jetbrains.annotations.NotNull;

/**
 * @author Denis Zhdanov
 * @since 11/10/11 12:18 PM
 */
public abstract class ExternalSystemTaskNotificationListenerAdapter implements ExternalSystemTaskNotificationListener {

  @Override
  public void onQueued(@NotNull ExternalSystemTaskId id) {
  }

  @Override
  public void onStart(@NotNull ExternalSystemTaskId id) {
  }

  @Override
  public void onStatusChange(@NotNull ExternalSystemTaskNotificationEvent event) {
  }

  @Override
  public void onEnd(@NotNull ExternalSystemTaskId id) {
  }
}
