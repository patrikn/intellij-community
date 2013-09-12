/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
package com.intellij.ide.util.newProjectWizard;

import com.intellij.facet.impl.ui.libraries.LibraryCompositionSettings;
import com.intellij.facet.impl.ui.libraries.LibraryOptionsPanel;
import com.intellij.framework.FrameworkVersion;
import com.intellij.framework.addSupport.FrameworkSupportInModuleConfigurable;
import com.intellij.framework.addSupport.FrameworkSupportInModuleProvider;
import com.intellij.framework.library.FrameworkLibraryVersion;
import com.intellij.framework.library.FrameworkLibraryVersionFilter;
import com.intellij.framework.library.impl.FrameworkLibraryVersionImpl;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportConfigurableListener;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModelAdapter;
import com.intellij.ide.util.newProjectWizard.impl.FrameworkSupportModelBase;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.roots.ui.configuration.libraries.CustomLibraryDescription;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainer;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SeparatorFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author nik
 */
public class FrameworkSupportOptionsComponent {
  private final JPanel myMainPanel;
  private final FrameworkSupportModelBase myModel;
  private FrameworkVersionComponent myFrameworkVersionComponent;
  private LibraryCompositionSettings myLibraryCompositionSettings;
  private LibraryOptionsPanel myLibraryOptionsPanel;
  private final FrameworkSupportInModuleConfigurable myConfigurable;
  private JPanel myLibraryOptionsPanelWrapper;

  public FrameworkSupportOptionsComponent(FrameworkSupportModelBase model,
                                          LibrariesContainer container,
                                          Disposable parentDisposable,
                                          FrameworkSupportInModuleProvider provider,
                                          final FrameworkSupportInModuleConfigurable configurable) {
    this(model, container, parentDisposable, provider, configurable, false);
  }

  public FrameworkSupportOptionsComponent(FrameworkSupportModelBase model,
                                          LibrariesContainer container,
                                          Disposable parentDisposable,
                                          FrameworkSupportInModuleProvider provider,
                                          final FrameworkSupportInModuleConfigurable configurable,
                                          boolean inline) {
    myModel = model;
    myConfigurable = configurable;
    LayoutManager layout = inline ? new BorderLayout() : new VerticalFlowLayout(false, true);
    myMainPanel = new JPanel(layout);
    myModel.registerOptionsComponent(provider, this);
    List<FrameworkVersion> versions = provider.getFrameworkType().getVersions();
    if (!versions.isEmpty()) {
      myFrameworkVersionComponent = new FrameworkVersionComponent(model, provider.getFrameworkType().getId(), versions);
      if (inline) {
        myMainPanel.add(myFrameworkVersionComponent.getMainPanel(), BorderLayout.WEST);
      }
      else {
        myMainPanel.add(myFrameworkVersionComponent.getMainPanel());
      }
    }

    final JComponent component = myConfigurable.createComponent(inline);
    if (component != null) {
      myMainPanel.add(component);
    }

    final CustomLibraryDescription description = myConfigurable.createLibraryDescription();
    if (description != null) {
      final boolean addSeparator = component != null || myFrameworkVersionComponent != null;
      myLibraryOptionsPanelWrapper = new JPanel(new BorderLayout());
      myMainPanel.add(myLibraryOptionsPanelWrapper);
      if (myConfigurable instanceof OldFrameworkSupportProviderWrapper.FrameworkSupportConfigurableWrapper) {
        ((OldFrameworkSupportProviderWrapper.FrameworkSupportConfigurableWrapper)myConfigurable).getConfigurable().addListener(
          new FrameworkSupportConfigurableListener() {
            public void frameworkVersionChanged() {
              updateLibrariesPanel();
            }
          });
      }
      model.addFrameworkListener(new FrameworkSupportModelAdapter() {
        @Override
        public void wizardStepUpdated() {
          updateLibrariesPanel();
        }
      }, parentDisposable);

      myLibraryOptionsPanel = new LibraryOptionsPanel(description, myModel.getBaseDirectoryForLibrariesPath(), createLibraryVersionFilter(),
                                                      container, !myConfigurable.isOnlyLibraryAdded());
      Disposer.register(myConfigurable, myLibraryOptionsPanel);
      if (addSeparator) {
        JComponent separator = SeparatorFactory.createSeparator("Libraries", null);
        separator.setBorder(IdeBorderFactory.createEmptyBorder(5, 0, 5, 5));
        myLibraryOptionsPanelWrapper.add(BorderLayout.NORTH, separator);
      }
      myLibraryOptionsPanelWrapper.add(BorderLayout.CENTER, myLibraryOptionsPanel.getMainPanel());
      myLibraryOptionsPanelWrapper.setVisible(myConfigurable.isVisible());
    }
  }

  public void updateLibrariesPanel() {
    if (myLibraryOptionsPanel != null) {
      myLibraryOptionsPanel.changeBaseDirectoryPath(myModel.getBaseDirectoryForLibrariesPath());
      myLibraryOptionsPanel.setVersionFilter(createLibraryVersionFilter());
      myLibraryOptionsPanelWrapper.setVisible(myConfigurable.isVisible());
    }
  }

  public void updateVersionsComponent() {
    if (myFrameworkVersionComponent != null) {
      myFrameworkVersionComponent.updateVersionsList();
    }
  }


  private FrameworkLibraryVersionFilter createLibraryVersionFilter() {
    return new FrameworkLibraryVersionFilter() {
      @Override
      public boolean isAccepted(@NotNull FrameworkLibraryVersion version) {
        return myConfigurable.getLibraryVersionFilter().isAccepted(version) && ((FrameworkLibraryVersionImpl)version).getAvailabilityCondition().isAvailableFor(
          myModel);
      }
    };
  }

  public JPanel getMainPanel() {
    return myMainPanel;
  }

  @Nullable
  public LibraryCompositionSettings getLibraryCompositionSettings() {
    if (myLibraryCompositionSettings == null && myLibraryOptionsPanel != null) {
      myLibraryCompositionSettings = myLibraryOptionsPanel.apply();
    }
    return myLibraryCompositionSettings;
  }

  public LibraryOptionsPanel getLibraryOptionsPanel() {
    return myLibraryOptionsPanel;
  }
}
