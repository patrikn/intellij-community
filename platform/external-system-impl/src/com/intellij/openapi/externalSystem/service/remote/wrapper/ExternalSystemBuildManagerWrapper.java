/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.externalSystem.service.remote.wrapper;

import com.intellij.openapi.externalSystem.model.ExternalSystemException;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskDescriptor;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId;
import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings;
import com.intellij.openapi.externalSystem.service.remote.RemoteExternalSystemBuildManager;
import com.intellij.openapi.externalSystem.service.remote.RemoteExternalSystemProgressNotificationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Denis Zhdanov
 * @since 3/14/13 5:53 PM
 */
public class ExternalSystemBuildManagerWrapper<S extends ExternalSystemExecutionSettings>
  extends AbstractRemoteExternalSystemServiceWrapper<S, RemoteExternalSystemBuildManager<S>>
  implements RemoteExternalSystemBuildManager<S>
{

  @NotNull private final RemoteExternalSystemProgressNotificationManager myProgressManager;

  public ExternalSystemBuildManagerWrapper(@NotNull RemoteExternalSystemBuildManager<S> delegate,
                                           @NotNull RemoteExternalSystemProgressNotificationManager progressManager)
  {
    super(delegate);
    myProgressManager = progressManager;
  }

  @Nullable
  @Override
  public Map<String, Collection<ExternalSystemTaskDescriptor>> listTasks(@NotNull ExternalSystemTaskId id,
                                                                         @NotNull String projectPath,
                                                                         @Nullable S settings)
    throws RemoteException, ExternalSystemException
  {
    myProgressManager.onQueued(id);
    try {
      return getDelegate().listTasks(id, projectPath, settings);
    }
    finally {
      myProgressManager.onEnd(id);
    }
  }

  @Override
  public void executeTasks(@NotNull ExternalSystemTaskId id,
                           @NotNull List<String> taskNames,
                           @NotNull String projectPath,
                           @Nullable S settings) throws RemoteException, ExternalSystemException
  {
    myProgressManager.onQueued(id);
    try {
      getDelegate().executeTasks(id, taskNames, projectPath, settings);
    }
    finally {
      myProgressManager.onEnd(id);
    }
  }
}
