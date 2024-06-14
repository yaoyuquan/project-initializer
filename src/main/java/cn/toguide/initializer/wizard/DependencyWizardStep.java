package cn.toguide.initializer.wizard;

import cn.toguide.initializer.wizard.entity.Dependency;
import cn.toguide.initializer.wizard.entity.DependencyCategory;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ResourceUtil;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class DependencyWizardStep extends ModuleWizardStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyWizardStep.class);

    private final ObjectMapper objectMapper;

    private final WizardContext wizardContext;

    private JPanel outerPanel;

    public DependencyWizardStep(WizardContext wizardContext) {
        this.wizardContext = wizardContext;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public JComponent getComponent() {

        outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(JBUI.Borders.empty(20));

        JBSplitter extensionsPanel = new JBSplitter(false, 0.5f);
        extensionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        //extensions component
        List<DependencyCategory> categories = getCategory();
        CheckedTreeNode root = getModel(categories);
        CheckboxTree extensionsTree = new ExtensionsTree(root);
        JTextPane extensionDetailTextPane = new JTextPane();
        extensionDetailTextPane.setEditorKit(getHtmlEditorKit());
        extensionDetailTextPane.setEditable(false);
        JBSplitter extensionsSplitter = new JBSplitter(true, 0.8f);
        extensionsSplitter.setFirstComponent(new JBScrollPane(extensionsTree));
        extensionsSplitter.setSecondComponent(extensionDetailTextPane);
        extensionsPanel.setFirstComponent(extensionsSplitter);

        JBList<Dependency> selectedExtensions = new JBList<>();
        selectedExtensions.setBackground(null);
        selectedExtensions.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectedExtensions.setModel(new SelectedExtensionsModel(categories));
        selectedExtensions.setCellRenderer(new SelectedExtensionsCellRenderer());

        JPanel selectedExtensionsPanel = new JPanel();
        selectedExtensionsPanel.setLayout(new BoxLayout(selectedExtensionsPanel, BoxLayout.Y_AXIS));
        selectedExtensionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel("Selected extensions");
        label.setFont(label.getFont().deriveFont(label.getFont().getStyle() | Font.BOLD));
        selectedExtensionsPanel.add(label);

        selectedExtensionsPanel.add(selectedExtensions);
        extensionsPanel.setSecondComponent(new JBScrollPane(selectedExtensionsPanel));
        panel.add(extensionsPanel);

        extensionsTree.addCheckboxTreeListener(onNodeCheckedStateChanged(categories, selectedExtensions));

        //(Un)Check extension on double-click
        extensionsTree.addMouseListener(onAvailableExtensionClicked(extensionsTree));

        //Unselect extensions on double-click
        selectedExtensions.addMouseListener(onSelectedExtensionClicked(categories, extensionsTree, selectedExtensions));

        //Unselect extensions when pressing the DELETE or BACKSPACE key
        selectedExtensions.addKeyListener(onSelectedExtensionsKeyPressed(categories, extensionsTree, selectedExtensions));

        extensionsTree.getSelectionModel().addTreeSelectionListener(e -> {
            if (e.getNewLeadSelectionPath() != null) {
                Object comp = ((DefaultMutableTreeNode) e.getNewLeadSelectionPath().getLastPathComponent()).getUserObject();
                if (comp instanceof Dependency extension) {
                    StringBuilder builder = new StringBuilder("<html><body>").append(extension.getDescription()).append(".");
                    builder.append("</body></html>");
                    extensionDetailTextPane.setText(builder.toString());
                } else {
                    extensionDetailTextPane.setText("");
                }
            }
        });
        outerPanel.add(panel, BorderLayout.CENTER);


        return outerPanel;
    }

    @Override
    public void updateDataModel() {

    }

    private List<DependencyCategory> getCategory() {

        List<DependencyCategory> categories = new ArrayList<>();

        try {
            byte[] json = ResourceUtil.getResourceAsBytes("/metadata.json", this.getClass().getClassLoader());
            JsonNode jsonNode = objectMapper.readTree(json);
            ArrayNode arrayNode = (ArrayNode) jsonNode.get("dependencies").get("values");

            JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, DependencyCategory.class);
            categories = objectMapper.treeToValue(arrayNode, type);

        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        }

        return categories;

    }

    private CheckedTreeNode getModel(List<DependencyCategory> categories) {

        CheckedTreeNode root = new CheckedTreeNode();
        for(DependencyCategory category : categories) {
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category);
            for(Dependency extension : category.getList()) {
                CheckedTreeNode extensionNode = new CheckedTreeNode(extension);
                extensionNode.setChecked(false);
                categoryNode.add(extensionNode);
            }
            root.add(categoryNode);
        }
        return root;
    }

    private static class ExtensionsTree extends CheckboxTree {

        public ExtensionsTree(CheckedTreeNode root) {
            super(new ExtensionsTreeCellRenderer(), root);
        }
    }

    private static class ExtensionsTreeCellRenderer extends CheckboxTree.CheckboxTreeCellRenderer {

        @Override
        public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof DependencyCategory) {
                getTextRenderer().append(((DependencyCategory) userObject).getName());
            } else if (userObject instanceof Dependency)  {
                getTextRenderer().append(((Dependency) userObject).getName());
            }
        }
    }

    /**
     * Use reflection to get IntelliJ specific HTML editor kit as it has moved in 2020.1
     *
     * @return the HTML editor kit to use
     */
    @NotNull
    private EditorKit getHtmlEditorKit() {
        try {
            return (EditorKit) Class.forName("com.intellij.util.ui.JBHtmlEditorKit").newInstance();
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
            try {
                return (EditorKit) Class.forName("com.intellij.util.ui.UIUtil$JBHtmlEditorKit").newInstance();
            } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e1) {
                return new HTMLEditorKit();
            }
        }
    }

    private static class SelectedExtensionsModel extends AbstractListModel<Dependency> {

        private final List<Dependency> extensions = new ArrayList<>();

        private SelectedExtensionsModel(List<DependencyCategory> categories) {
            Set<String> ids = new HashSet<>();
            categories.stream().flatMap(category -> category.getList().stream()).filter(Dependency::isSelected).forEach(extension -> {
                if (!ids.contains(extension.getId())) {
                    ids.add(extension.getId());
                    extensions.add(extension);
                }
            });
        }

        @Override
        public int getSize() {
            return extensions.size();
        }

        @Override
        public Dependency getElementAt(int index) {
            return extensions.get(index);
        }
    }


    private static class SelectedExtensionsCellRenderer extends ColoredListCellRenderer<Dependency> {

        @Override
        protected void customizeCellRenderer(@NotNull JList<? extends Dependency> list, Dependency extension, int index, boolean selected, boolean hasFocus) {
            append(extension.getName());
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Dependency> list, Dependency value, int index, boolean selected, boolean hasFocus) {
            super.getListCellRendererComponent(list, value, index, selected, hasFocus);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            return this;
        }
    }

    @NotNull
    private static CheckboxTreeListener onNodeCheckedStateChanged(List<DependencyCategory> categories, JBList<Dependency> selectedExtensions) {
        return new CheckboxTreeListener() {
            @Override
            public void nodeStateChanged(@NotNull CheckedTreeNode node) {
                Dependency extension = (Dependency) node.getUserObject();
                if (extension == null) {
                    // Since ExtensionsTree doesn't extend CheckboxTreeBase directly,
                    // you can't customize its CheckboxTreeBase.CheckPolicy,
                    // so CheckboxTreeHelper.adjustParentsAndChildren basically calls nodeStateChanged(node.getParent())
                    // which doesn't hold a QuarkusExtension and leads to https://github.com/redhat-developer/intellij-quarkus/issues/639
                    // So we bail here.
                    return;
                }
                extension.setSelected(node.isChecked());
                selectedExtensions.setModel(new SelectedExtensionsModel(categories));
            }
        };
    }


    @NotNull
    private static MouseAdapter onAvailableExtensionClicked(CheckboxTree extensionsTree) {
        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = extensionsTree.getPathForLocation(e.getX(), e.getY());
                    if (path != null && path.getLastPathComponent() instanceof CheckedTreeNode treeNode) {
                        extensionsTree.setNodeState(treeNode, !treeNode.isChecked());
                    }
                }
            }
        };
    }

    @NotNull
    private MouseAdapter onSelectedExtensionClicked(List<DependencyCategory> categories, CheckboxTree extensionsTree, JBList<Dependency> selectedExtensions) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedIndex = selectedExtensions.getSelectedIndex();
                    if (selectedIndex > -1) {
                        Dependency extension = selectedExtensions.getModel().getElementAt(selectedIndex);
                        if (unselectExtension(extensionsTree, extension)) {
                            // The extensions was not visible in the tree so didn't trigger a selectedExtension model refresh
                            // so we force it manually
                            selectedExtensions.setModel(new SelectedExtensionsModel(categories));
                        }
                    }
                }
            }
        };
    }

    /**
     * Unselects a selected extension from the extension tree. Returns true if the extension was not found in the tree, false otherwise.
     */
    private boolean unselectExtension(@NotNull CheckboxTree extensionsTree, @NotNull Dependency extension) {
        var treeNodes = findTreeNodesForExtension(extensionsTree, extension);
        for (var treeNode : treeNodes) {
            extensionsTree.setNodeState(treeNode, false);
        }
        extension.setSelected(false);
        return treeNodes.isEmpty();
    }

    /**
     * Find CheckedTreeNode for a given extension, as it can belong to several categories
     */
    private @NotNull Set<CheckedTreeNode> findTreeNodesForExtension(@NotNull CheckboxTree extensionsTree, @NotNull Dependency extension) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) extensionsTree.getModel().getRoot();
        Enumeration<TreeNode> enumeration = rootNode.depthFirstEnumeration();
        Set<CheckedTreeNode> nodes = new HashSet<>();
        while (enumeration.hasMoreElements()) {
            TreeNode node = enumeration.nextElement();
            if (node instanceof CheckedTreeNode && ((CheckedTreeNode)node).getUserObject() == extension) {
                nodes.add( (CheckedTreeNode)node);
            }
        }
        return nodes;
    }


    private KeyListener onSelectedExtensionsKeyPressed(List<DependencyCategory> categories, CheckboxTree extensionsTree, JList<Dependency> selectedExtensions) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    boolean requiresModelRefresh = false;
                    for (Dependency extension : selectedExtensions.getSelectedValuesList()) {
                        requiresModelRefresh = unselectExtension(extensionsTree, extension) || requiresModelRefresh;
                    }
                    selectedExtensions.clearSelection();
                    if (requiresModelRefresh) {
                        // Some extensions were not visible in the tree so didn't trigger a selectedExtension model refresh
                        // so we force it manually
                        selectedExtensions.setModel(new SelectedExtensionsModel(categories));
                    }
                }
            }
        };
    }
}
