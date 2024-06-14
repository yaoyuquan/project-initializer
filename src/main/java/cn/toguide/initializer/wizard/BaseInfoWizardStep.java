package cn.toguide.initializer.wizard;

import com.github.yaoyuquan.projectinitializer.MyBundle;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.util.Key;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;

import javax.swing.*;
import java.awt.*;

public class BaseInfoWizardStep extends ModuleWizardStep {

    private final JPanel component;

    private final WizardContext context;

    private final JBRadioButton singleProject = new JBRadioButton(MyBundle.message("form.project.build.type.single"), true);

    private final JBRadioButton microServiceProject = new JBRadioButton(MyBundle.message("form.project.build.type.microservice"), false);

    private final JTextField group = new JTextField("cn.toguide", 20);

    private final JTextField artifact = new JTextField(20);

    private final JTextField name = new JTextField(20);

    private final JTextField desc = new JTextField(20);

    private final JTextField packageName = new JTextField(20);


    public BaseInfoWizardStep(WizardContext context) {
        this.component = createComponent();
        this.context = context;
    }

    @Override
    public JComponent getComponent() {
        return component;
    }


    public JPanel createComponent() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(20));

        FormBuilder builder = new FormBuilder();

        builder.addLabeledComponent(MyBundle.message("form.project.spring-boot.version"), new JRadioButton("2.6.13", true));

        builder.addLabeledComponent("Group: ", group);

        builder.addLabeledComponent("Artifact: ", artifact);

        ButtonGroup group = new ButtonGroup();
        group.add(this.singleProject);
        group.add(this.microServiceProject);

        BorderLayoutPanel defaultPanel = JBUI.Panels.simplePanel(10, 0);
        defaultPanel.addToLeft(singleProject);
        defaultPanel.addToCenter(microServiceProject);
        builder.addLabeledComponent(MyBundle.message("form.project.build.type"), defaultPanel);

        builder.addLabeledComponent(MyBundle.message("form.project.build.name"), name);

        builder.addLabeledComponent(MyBundle.message("form.project.build.project.desc"), desc);

        builder.addLabeledComponent(MyBundle.message("form.project.build.package.name"), packageName);


        panel.add(ScrollPaneFactory.createScrollPane(builder.getPanel(), true), "North");


        return panel;
    }



    @Override
    public void updateDataModel() {

        this.context.putUserData(Key.create("group"), group.getText());
        this.context.putUserData(Key.create("artifact"), artifact.getText());
        this.context.putUserData(Key.create("name"), name.getText());
        this.context.putUserData(Key.create("desc"), desc.getText());
        this.context.putUserData(Key.create("packageName"), packageName.getText());

    }
}
