/*
 * Autopsy Forensic Browser
 *
 * Copyright 2018 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.casemodule;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JFileChooser;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataSourceProcessor;
import org.sleuthkit.autopsy.coreutils.MessageNotifyUtil;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.AbstractListModel;
import javax.swing.JOptionPane;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.coreutils.PathValidator;

/**
 * A panel which allows the user to select local files and/or directories.
 */
@SuppressWarnings("PMD.SingularField") // UI widgets cause lots of false positives
final class LocalFilesPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;

    private boolean enableNext = false;
    private static final Logger logger = Logger.getLogger(LocalFilesPanel.class.getName());
    private String displayName = "";
    private final LocalFilesModel listModel = new LocalFilesModel();

    /**
     * Creates new form LocalFilesPanel
     */
    LocalFilesPanel() {
        initComponents();
        customInit();
    }

    private void customInit() {
        localFileChooser.setMultiSelectionEnabled(true);
        errorLabel.setVisible(false);
        this.fileList.setModel(listModel);
        listModel.clear();
        this.displayNameLabel.setText(NbBundle.getMessage(this.getClass(), "LocalFilesPanel.displayNameLabel.text"));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        localFileChooser = new javax.swing.JFileChooser();
        selectButton = new javax.swing.JButton();
        deleteButon = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        javax.swing.JScrollPane fileListScrollpane = new javax.swing.JScrollPane();
        fileList = new javax.swing.JList<>();
        javax.swing.JPanel displayNamePanel = new javax.swing.JPanel();
        changeNameButton = new javax.swing.JButton();
        displayNameLabel = new javax.swing.JLabel();
        javax.swing.JPanel padding = new javax.swing.JPanel();
        javax.swing.JLabel timeStampToIncludeLabel = new javax.swing.JLabel();
        modifiedTimeCheckBox = new javax.swing.JCheckBox();
        createTimeCheckBox = new javax.swing.JCheckBox();
        accessTimeCheckBox = new javax.swing.JCheckBox();
        javax.swing.JLabel timeStampNoteLabel = new javax.swing.JLabel();
        errorLabel = new javax.swing.JLabel();
        javax.swing.JPanel paddingBottom = new javax.swing.JPanel();

        localFileChooser.setApproveButtonText(org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.localFileChooser.approveButtonText")); // NOI18N
        localFileChooser.setApproveButtonToolTipText(org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.localFileChooser.approveButtonToolTipText")); // NOI18N
        localFileChooser.setDialogTitle(org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.localFileChooser.dialogTitle")); // NOI18N
        localFileChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(selectButton, org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.selectButton.text")); // NOI18N
        selectButton.setToolTipText(org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.selectButton.toolTipText")); // NOI18N
        selectButton.setActionCommand(org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.selectButton.actionCommand")); // NOI18N
        selectButton.setMaximumSize(new java.awt.Dimension(70, 23));
        selectButton.setMinimumSize(new java.awt.Dimension(70, 23));
        selectButton.setPreferredSize(new java.awt.Dimension(70, 23));
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(selectButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(deleteButon, org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.deleteButon.text")); // NOI18N
        deleteButon.setMaximumSize(new java.awt.Dimension(70, 23));
        deleteButon.setMinimumSize(new java.awt.Dimension(70, 23));
        deleteButon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(deleteButon, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(clearButton, org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.clearButton.text")); // NOI18N
        clearButton.setToolTipText(org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.clearButton.toolTipText")); // NOI18N
        clearButton.setMaximumSize(new java.awt.Dimension(70, 23));
        clearButton.setMinimumSize(new java.awt.Dimension(70, 23));
        clearButton.setPreferredSize(new java.awt.Dimension(70, 23));
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(clearButton, gridBagConstraints);

        fileListScrollpane.setMaximumSize(new java.awt.Dimension(32767, 100));
        fileListScrollpane.setMinimumSize(new java.awt.Dimension(100, 100));
        fileListScrollpane.setPreferredSize(new java.awt.Dimension(258, 100));

        fileListScrollpane.setViewportView(fileList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        add(fileListScrollpane, gridBagConstraints);

        displayNamePanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(changeNameButton, org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.changeNameButton.text")); // NOI18N
        changeNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeNameButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        displayNamePanel.add(changeNameButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(displayNameLabel, org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.displayNameLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        displayNamePanel.add(displayNameLabel, gridBagConstraints);

        padding.setMinimumSize(new java.awt.Dimension(0, 0));
        padding.setPreferredSize(new java.awt.Dimension(0, 0));

        javax.swing.GroupLayout paddingLayout = new javax.swing.GroupLayout(padding);
        padding.setLayout(paddingLayout);
        paddingLayout.setHorizontalGroup(
            paddingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        paddingLayout.setVerticalGroup(
            paddingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        displayNamePanel.add(padding, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(displayNamePanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(timeStampToIncludeLabel, org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.timeStampToIncludeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(timeStampToIncludeLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(modifiedTimeCheckBox, org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.modifiedTimeCheckBox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 5);
        add(modifiedTimeCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(createTimeCheckBox, org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.createTimeCheckBox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 5);
        add(createTimeCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(accessTimeCheckBox, org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.accessTimeCheckBox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 5);
        add(accessTimeCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(timeStampNoteLabel, org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.timeStampNoteLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        add(timeStampNoteLabel, gridBagConstraints);

        errorLabel.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, org.openide.util.NbBundle.getMessage(LocalFilesPanel.class, "LocalFilesPanel.errorLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(errorLabel, gridBagConstraints);

        paddingBottom.setMinimumSize(new java.awt.Dimension(0, 0));

        javax.swing.GroupLayout paddingBottomLayout = new javax.swing.GroupLayout(paddingBottom);
        paddingBottom.setLayout(paddingBottomLayout);
        paddingBottomLayout.setHorizontalGroup(
            paddingBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        paddingBottomLayout.setVerticalGroup(
            paddingBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(paddingBottom, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        int returnVal = localFileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] files = localFileChooser.getSelectedFiles();
            this.listModel.add(files);
        }

        enableNext = !this.listModel.getFiles().isEmpty();

        try {
            firePropertyChange(DataSourceProcessor.DSP_PANEL_EVENT.UPDATE_UI.toString(), false, true);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "LocalFilesPanel listener threw exception", e); //NON-NLS
            MessageNotifyUtil.Notify.show(NbBundle.getMessage(this.getClass(), "LocalFilesPanel.moduleErr"),
                    NbBundle.getMessage(this.getClass(), "LocalFilesPanel.moduleErr.msg"),
                    MessageNotifyUtil.MessageType.ERROR);
        }
    }//GEN-LAST:event_selectButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        reset();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void changeNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeNameButtonActionPerformed
        final String selectedDisplayName = JOptionPane.showInputDialog("New Display Name: ");
        if (selectedDisplayName != null && !selectedDisplayName.isEmpty()) {
            this.displayName = selectedDisplayName;
            this.displayNameLabel.setText("Display Name: " + this.displayName);
        }
    }//GEN-LAST:event_changeNameButtonActionPerformed

    private void deleteButonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButonActionPerformed
        int minIdx = this.fileList.getMinSelectionIndex();
        int maxIdx = this.fileList.getMaxSelectionIndex();

        if (minIdx >= 0 && maxIdx >= minIdx) {
            this.listModel.remove(minIdx, maxIdx);
        }
        this.fileList.clearSelection();

        enableNext = !this.listModel.getFiles().isEmpty();

        try {
            firePropertyChange(DataSourceProcessor.DSP_PANEL_EVENT.UPDATE_UI.toString(), false, true);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "LocalFilesPanel listener threw exception", e); //NON-NLS
            MessageNotifyUtil.Notify.show(NbBundle.getMessage(this.getClass(), "LocalFilesPanel.moduleErr"),
                    NbBundle.getMessage(this.getClass(), "LocalFilesPanel.moduleErr.msg"),
                    MessageNotifyUtil.MessageType.ERROR);
        }
    }//GEN-LAST:event_deleteButonActionPerformed

    /**
     * Clear the fields and undo any selection of files.
     */
    void reset() {
        this.listModel.clear();
        enableNext = false;
        errorLabel.setVisible(false);
        displayName = "";
        this.displayNameLabel.setText(NbBundle.getMessage(this.getClass(), "LocalFilesPanel.displayNameLabel.text"));
        try {
            firePropertyChange(DataSourceProcessor.DSP_PANEL_EVENT.UPDATE_UI.toString(), false, true);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "LocalFilesPanel listener threw exception", e); //NON-NLS
            MessageNotifyUtil.Notify.show(NbBundle.getMessage(this.getClass(), "LocalFilesPanel.moduleErr"),
                    NbBundle.getMessage(this.getClass(), "LocalFilesPanel.moduleErr.msg"),
                    MessageNotifyUtil.MessageType.ERROR);
        }
    }

    /**
     * Get the path(s) which have been selected on this panel
     *
     * @return a List of Strings representing the path(s) for the selected files
     * or directories
     */
    List<String> getContentPaths() {
        return this.listModel.getFiles().stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
    }
    
    /**
     * Get whether the createTimestampcheckbox has been checked or not
     * @return  boolean if box was checked
     */

    Boolean getCreateTimestamps() {
        return createTimeCheckBox.isSelected();
    }

    /**
     * Get whether the ModifiedTimestampcheckbox has been checked or not
     * @return  boolean if box was checked
     */

    Boolean getModifiedTimestamps() {
        return modifiedTimeCheckBox.isSelected();
    }

    /**
     * Get whether the accessTimestampcheckbox has been checked or not
     * @return  boolean if box was checked
     */

    Boolean getAccessTimestamps() {
        return accessTimeCheckBox.isSelected();
    }

    /**
     * Validates path to selected data source and displays warning if it is
     * invalid.
     *
     * @return enableNext - true if the panel is valid, false if invalid
     */
    boolean validatePanel() {
        // display warning if there is one (but don't disable "next" button)
        warnIfPathIsInvalid(getContentPaths());
        return enableNext;
    }

    /**
     * Validates path to selected data source and displays warning if it is
     * invalid.
     *
     * @param paths Absolute paths to the selected data source
     */
    @NbBundle.Messages({
        "LocalFilesPanel.pathValidation.dataSourceOnCDriveError=Warning: Path to multi-user data source is on \"C:\" drive",
        "LocalFilesPanel.pathValidation.getOpenCase=WARNING: Exception while gettting open case."
    })
    private void warnIfPathIsInvalid(final List<String> pathsList) {
        errorLabel.setVisible(false);

        try {
            final Case.CaseType currentCaseType = Case.getCurrentCaseThrows().getCaseType();

            for (String currentPath : pathsList) {
                if (!PathValidator.isValidForCaseType(currentPath, currentCaseType)) {
                    errorLabel.setVisible(true);
                    errorLabel.setText(Bundle.LocalFilesPanel_pathValidation_dataSourceOnCDriveError());
                    return;
                }
            }
        } catch (NoCurrentCaseException ex) {
            errorLabel.setVisible(true);
            errorLabel.setText(Bundle.LocalFilesPanel_pathValidation_getOpenCase());
        }
    }

    /**
     * Get the name given to this collection of local files and directories
     *
     * @return a String which is the name for the file set.
     */
    String getFileSetName() {
        return this.displayName;
    }

    /**
     * A record of a file for the specific purposes of displaying in a JList
     * (with toString).
     */
    private static class FileRecord {

        private final File file;

        FileRecord(File file) {
            this.file = file;
        }

        @Override
        public String toString() {
            return file == null ? "" : file.getAbsolutePath();
        }

        /**
         * @return The underlying file.
         */
        File getFile() {
            return file;
        }
    }

    /**
     * JListModel for displaying files.
     */
    private static class LocalFilesModel extends AbstractListModel<FileRecord> {

        private List<File> items = Collections.emptyList();

        @Override
        public int getSize() {
            return items.size();
        }

        @Override
        public FileRecord getElementAt(int index) {
            File f = items.get(index);
            return new FileRecord(f);
        }

        /**
         * Adds a series of files to the list model.
         *
         * @param files The files.
         */
        void add(File... files) {
            items = Stream.concat(items.stream(), Stream.of(files))
                    .sorted(Comparator.comparing(f -> f.getAbsolutePath().toLowerCase()))
                    .distinct()
                    .collect(Collectors.toList());

            this.fireContentsChanged(this, 0, items.size() - 1);
        }

        /**
         * Removes files in the list starting at minIdx going to maxIdx.
         *
         * @param minIdx The minimum index of items to be removed.
         * @param maxIdx The maximum index to be removed.
         */
        void remove(int minIdx, int maxIdx) {
            for (int i = maxIdx; i >= minIdx; i--) {
                items.remove(i);
            }
            this.fireContentsChanged(this, 0, items.size() - 1);
        }

        /**
         * @return The files to be added to the local files data source.
         */
        List<File> getFiles() {
            return items;
        }

        /**
         * Clears currently tracked local files.
         */
        void clear() {
            items.clear();
            this.fireContentsChanged(this, 0, 0);
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox accessTimeCheckBox;
    private javax.swing.JButton changeNameButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JCheckBox createTimeCheckBox;
    private javax.swing.JButton deleteButon;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JList<FileRecord> fileList;
    private javax.swing.JFileChooser localFileChooser;
    private javax.swing.JCheckBox modifiedTimeCheckBox;
    private javax.swing.JButton selectButton;
    // End of variables declaration//GEN-END:variables
}
