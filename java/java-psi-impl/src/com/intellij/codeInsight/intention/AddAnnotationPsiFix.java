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
package com.intellij.codeInsight.intention;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.ExternalAnnotationsManager;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.lang.findUsages.LanguageFindUsages;
import com.intellij.openapi.command.undo.UndoUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AddAnnotationPsiFix extends LocalQuickFixOnPsiElement {
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInsight.intention.AddAnnotationPsiFix");
  protected final String myAnnotation;
  protected final String[] myAnnotationsToRemove;
  protected final PsiNameValuePair[] myPairs; // not used when registering local quick fix
  protected final String myText;

  public AddAnnotationPsiFix(@NotNull String fqn,
                            @NotNull PsiModifierListOwner modifierListOwner,
                            @NotNull PsiNameValuePair[] values,
                            @NotNull String... annotationsToRemove) {
    super(modifierListOwner);
    myAnnotation = fqn;
    myPairs = values;
    myAnnotationsToRemove = annotationsToRemove;
    myText = calcText(modifierListOwner, myAnnotation);
  }

  public static String calcText(PsiModifierListOwner modifierListOwner, @NotNull String annotation) {
    final String shortName = annotation.substring(annotation.lastIndexOf('.') + 1);
    if (modifierListOwner instanceof PsiNamedElement) {
      final String name = ((PsiNamedElement)modifierListOwner).getName();
      if (name != null) {
        FindUsagesProvider provider = LanguageFindUsages.INSTANCE.forLanguage(modifierListOwner.getLanguage());
        return CodeInsightBundle
          .message("inspection.i18n.quickfix.annotate.element.as", provider.getType(modifierListOwner), name, shortName);
      }
    }
    return CodeInsightBundle.message("inspection.i18n.quickfix.annotate.as", shortName);
  }

  @Nullable
  public static PsiModifierListOwner getContainer(final PsiElement element) {
    PsiModifierListOwner listOwner = PsiTreeUtil.getParentOfType(element, PsiParameter.class, false);
    if (listOwner == null) {
      final PsiIdentifier psiIdentifier = PsiTreeUtil.getParentOfType(element, PsiIdentifier.class, false);
      if (psiIdentifier != null && psiIdentifier.getParent() instanceof PsiModifierListOwner) {
        listOwner = (PsiModifierListOwner)psiIdentifier.getParent();
      }
    }
    return listOwner;
  }

  @Override
  @NotNull
  public String getText() {
    return myText;
  }

  @Override
  @NotNull
  public String getFamilyName() {
    return CodeInsightBundle.message("intention.add.annotation.family");
  }

  @Override
  public boolean isAvailable(@NotNull Project project,
                             @NotNull PsiFile file,
                             @NotNull PsiElement startElement,
                             @NotNull PsiElement endElement) {
    if (!startElement.isValid()) return false;
    if (!PsiUtil.isLanguageLevel5OrHigher(startElement)) return false;
    final PsiModifierListOwner myModifierListOwner = (PsiModifierListOwner)startElement;

    return !AnnotationUtil.isAnnotated(myModifierListOwner, myAnnotation, false, false);
  }

  @Override
  public void invoke(@NotNull Project project,
                     @NotNull PsiFile file,
                     @NotNull PsiElement startElement,
                     @NotNull PsiElement endElement) {
    final PsiModifierListOwner myModifierListOwner = (PsiModifierListOwner)startElement;

    final ExternalAnnotationsManager annotationsManager = ExternalAnnotationsManager.getInstance(project);
    final PsiModifierList modifierList = myModifierListOwner.getModifierList();
    LOG.assertTrue(modifierList != null);
    if (modifierList.findAnnotation(myAnnotation) != null) return;
    final ExternalAnnotationsManager.AnnotationPlace annotationAnnotationPlace = annotationsManager.chooseAnnotationsPlace(myModifierListOwner);
    if (annotationAnnotationPlace == ExternalAnnotationsManager.AnnotationPlace.NOWHERE) return;
    if (annotationAnnotationPlace == ExternalAnnotationsManager.AnnotationPlace.EXTERNAL) {
      for (String fqn : myAnnotationsToRemove) {
        annotationsManager.deannotate(myModifierListOwner, fqn);
      }
      annotationsManager.annotateExternally(myModifierListOwner, myAnnotation, file, myPairs);
    }
    else {
      final PsiFile containingFile = myModifierListOwner.getContainingFile();
      if (!FileModificationService.getInstance().preparePsiElementForWrite(containingFile)) return;
      for (String fqn : myAnnotationsToRemove) {
        PsiAnnotation annotation = AnnotationUtil.findAnnotation(myModifierListOwner, fqn);
        if (annotation != null) {
          annotation.delete();
        }
      }

      PsiAnnotation inserted = modifierList.addAnnotation(myAnnotation);
      for (PsiNameValuePair pair : myPairs) {
        inserted.setDeclaredAttributeValue(pair.getName(), pair.getValue());
      }
      JavaCodeStyleManager.getInstance(project).shortenClassReferences(inserted);
      if (containingFile != file) {
        UndoUtil.markPsiFileForUndo(file);
      }
    }
  }

  @NotNull
  public String[] getAnnotationsToRemove() {
    return myAnnotationsToRemove;
  }
}
