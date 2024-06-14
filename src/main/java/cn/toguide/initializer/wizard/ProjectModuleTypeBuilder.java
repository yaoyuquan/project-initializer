package cn.toguide.initializer.wizard;

import com.github.yaoyuquan.projectinitializer.MyBundle;
import com.intellij.ide.util.projectWizard.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.InvalidDataException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ProjectModuleTypeBuilder extends JavaModuleBuilder {

    @Override
    public String getDescription() {
        return MyBundle.message("menu.description");
    }

    @Override
    public String getPresentableName() {
        return MyBundle.message("menu.name");
    }


    @Override
    public Icon getNodeIcon() {
        //return IconLoader.getIcon("/40.png", ProjectModuleTypeBuilder.class);
        return new ImageIcon();
    }

    @NotNull
    @Override
    public Module createModule(@NotNull ModifiableModuleModel moduleModel) throws InvalidDataException {

        return null;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Nullable
    @Override
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        return new BaseInfoWizardStep(context);
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[]{new DependencyWizardStep(wizardContext)};
    }
}
