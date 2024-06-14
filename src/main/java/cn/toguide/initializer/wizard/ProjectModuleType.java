package cn.toguide.initializer.wizard;

import com.github.yaoyuquan.projectinitializer.MyBundle;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ProjectModuleType extends ModuleType<ProjectModuleTypeBuilder> {


    protected ProjectModuleType(@NotNull @NonNls String id) {
        super(id);
    }

    @NotNull
    @Override
    public ProjectModuleTypeBuilder createModuleBuilder() {
        return new ProjectModuleTypeBuilder();
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getName() {
        return MyBundle.message("menu.name");
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getDescription() {
        return MyBundle.message("menu.description");
    }

    @Override
    public @NotNull Icon getNodeIcon(boolean isOpened) {
        try {
            byte[] data = Files.readAllBytes(new File("D://test.png").toPath());
            ImageIcon icon = new ImageIcon(data);
            return icon;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
