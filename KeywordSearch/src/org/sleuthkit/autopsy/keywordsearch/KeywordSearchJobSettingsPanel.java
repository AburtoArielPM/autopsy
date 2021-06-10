/*
 * Autopsy Forensic Browser
 * 
 * Copyright 2011-2018 Basis Technology Corp.
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
package org.sleuthkit.autopsy.keywordsearch;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import org.sleuthkit.autopsy.coreutils.StringExtract.StringExtractUnicodeTable.SCRIPT;
import org.sleuthkit.autopsy.guiutils.SimpleTableCellRenderer;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettingsPanel;
import org.sleuthkit.autopsy.keywordsearch.KeywordSearchIngestModule.StringsExtractOptions;

/**
 * Ingest job settings panel for keyword search file ingest modules.
 */
@SuppressWarnings("PMD.SingularField") // UI widgets cause lots of false positives
public final class KeywordSearchJobSettingsPanel extends IngestModuleIngestJobSettingsPanel implements PropertyChangeListener {

    private final KeywordListsTableModel tableModel = new KeywordListsTableModel();
    private final List<String> keywordListNames = new ArrayList<>();
    private final Map<String, Boolean> keywordListStates = new HashMap<>();
    private final XmlKeywordSearchList keywordListsManager = XmlKeywordSearchList.getCurrent();

    KeywordSearchJobSettingsPanel(KeywordSearchJobSettings initialSettings) {
        initializeKeywordListSettings(initialSettings);
        initComponents();
        customizeComponents();
    }
    
    /*
    
    
        TODO load:
        ocrCheckBox.setSelected(KeywordSearchSettings.getOcrOption());
        limitedOcrCheckbox.setSelected(KeywordSearchSettings.getLimitedOcrOption());
        ocrCheckBox.setEnabled(!ingestRunning);
        limitedOcrCheckbox.setEnabled(ocrCheckBox.isSelected() && !ingestRunning);
    
        TODO store:
        KeywordSearchSettings.setOcrOption(ocrCheckBox.isSelected());
        KeywordSearchSettings.setLimitedOcrOption(limitedOcrCheckbox.isSelected());
    
    
        TODO other enable:
    
        if (!PlatformUtil.isWindowsOS() || !PlatformUtil.is64BitOS()) {
            ocrCheckBox.setText(Bundle.KeywordSearchGlobalSearchSettingsPanel_customizeComponents_windowsOCR());
            ocrCheckBox.setSelected(false);
            ocrCheckBox.setEnabled(false);
            limitedOcrCheckbox.setSelected(false);
            limitedOcrCheckbox.setEnabled(false);
            limitedOcrCheckbox.setText(Bundle.KeywordSearchGlobalSearchSettingsPanel_customizeComponents_windowsLimitedOCR());
        }
    */

    private void initializeKeywordListSettings(KeywordSearchJobSettings settings) {
        keywordListNames.clear();
        keywordListStates.clear();
        List<KeywordList> keywordLists = keywordListsManager.getListsL();
        for (KeywordList list : keywordLists) {
            String listName = list.getName();
            keywordListNames.add(listName);
            keywordListStates.put(listName, settings.keywordListIsEnabled(listName));
        }
    }

    private void customizeComponents() {
        customizeKeywordListsTable();
        displayLanguages();
        displayEncodings();
        keywordListsManager.addPropertyChangeListener(this);
        languagesLabel.setText("<html>" + org.openide.util.NbBundle.getMessage(KeywordSearchJobSettingsPanel.class, "KeywordSearchJobSettingsPanel.languagesLabel.text") + "</html>"); // NOI18N NON-NLS
    }

    private void customizeKeywordListsTable() {
        listsTable.setModel(tableModel);
        listsTable.setTableHeader(null);
        listsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final int width = listsScrollPane.getPreferredSize().width;
        listsTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        TableColumn column;
        for (int i = 0; i < listsTable.getColumnCount(); i++) {
            column = listsTable.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(((int) (width * 0.07)));
            } else {
                column.setPreferredWidth(((int) (width * 0.92)));
            }
        }
    }

    private void displayLanguages() {
        List<SCRIPT> scripts = KeywordSearchSettings.getStringExtractScripts();
        StringBuilder langs = new StringBuilder();
        langs.append("<html>"); //NON-NLS
        for (int i = 0; i < scripts.size(); i++) {
            langs.append(scripts.get(i).toString());
            if (i + 1 < scripts.size()) {
                langs.append(", ");
            }
        }
        langs.append("</html>"); //NON-NLS
        String langsS = langs.toString();
        this.languagesValLabel.setText(langsS);
        this.languagesValLabel.setToolTipText(langsS);
    }

    private void displayEncodings() {
        String utf8 = KeywordSearchSettings.getStringExtractOption(StringsExtractOptions.EXTRACT_UTF8.toString());
        String utf16 = KeywordSearchSettings.getStringExtractOption(StringsExtractOptions.EXTRACT_UTF16.toString());
        ArrayList<String> encodingsList = new ArrayList<>();
        if (utf8 == null || Boolean.parseBoolean(utf8)) {
            encodingsList.add("UTF8");
        }
        if (utf16 == null || Boolean.parseBoolean(utf16)) {
            encodingsList.add("UTF16"); //NON-NLS
        }
        String encodings = encodingsList.toString();
        encodings = encodings.substring(1, encodings.length() - 1);
        keywordSearchEncodings.setText(encodings);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(XmlKeywordSearchList.ListsEvt.LIST_ADDED.name())
                || event.getPropertyName().equals(XmlKeywordSearchList.ListsEvt.LIST_DELETED.name())
                || event.getPropertyName().equals(XmlKeywordSearchList.ListsEvt.LIST_UPDATED.name())
                || event.getPropertyName().equals(XmlKeywordSearchList.LanguagesEvent.LANGUAGES_CHANGED.name())) {
            update();
        }
    }

    private void update() {
        updateKeywordListSettings();
        displayLanguages();
        displayEncodings();
        tableModel.fireTableDataChanged();
    }

    private void updateKeywordListSettings() {
        // Get the names of the current set of keyword lists.
        List<KeywordList> keywordLists = keywordListsManager.getListsL();
        List<String> currentListNames = new ArrayList<>();
        for (KeywordList list : keywordLists) {
            currentListNames.add(list.getName());
        }

        // Remove deleted lists from the list states map.
        for (String listName : keywordListNames) {
            if (!currentListNames.contains(listName)) {
                keywordListStates.remove(listName);
            }
        }

        // Reset the names list and add any new lists to the states map.
        keywordListNames.clear();
        for (String currentListName : currentListNames) {
            keywordListNames.add(currentListName);
            if (!keywordListStates.containsKey(currentListName)) {
                keywordListStates.put(currentListName, Boolean.TRUE);
            }
        }
    }

    @Override
    public IngestModuleIngestJobSettings getSettings() {
        List<String> enabledListNames = new ArrayList<>();
        List<String> disabledListNames = new ArrayList<>();
        for (String listName : keywordListNames) {
            if (keywordListStates.get(listName)) {
                enabledListNames.add(listName);
            } else {
                disabledListNames.add(listName);
            }
        }
        return new KeywordSearchJobSettings(enabledListNames, disabledListNames);
    }

    void reset(KeywordSearchJobSettings newSettings) {
        initializeKeywordListSettings(newSettings);
        displayLanguages();
        displayEncodings();
        tableModel.fireTableDataChanged();
    }

    private class KeywordListsTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return KeywordSearchJobSettingsPanel.this.keywordListNames.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            String listName = KeywordSearchJobSettingsPanel.this.keywordListNames.get(rowIndex);
            if (columnIndex == 0) {
                return keywordListStates.get(listName);
            } else {
                return listName;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            String listName = KeywordSearchJobSettingsPanel.this.keywordListNames.get(rowIndex);
            if (columnIndex == 0) {
                keywordListStates.put(listName, (Boolean) aValue);
            }
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        listsScrollPane = new javax.swing.JScrollPane();
        listsTable = new javax.swing.JTable();
        titleLabel = new javax.swing.JLabel();
        languagesLabel = new javax.swing.JLabel();
        languagesValLabel = new javax.swing.JLabel();
        encodingsLabel = new javax.swing.JLabel();
        keywordSearchEncodings = new javax.swing.JLabel();
        ocrCheckBox = new javax.swing.JCheckBox();
        limitedOcrCheckbox = new javax.swing.JCheckBox();
        ocrOnly = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(300, 170));

        listsScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        listsScrollPane.setPreferredSize(new java.awt.Dimension(300, 100));

        listsTable.setBackground(new java.awt.Color(240, 240, 240));
        listsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        listsScrollPane.setViewportView(listsTable);
        listsTable.setDefaultRenderer(String.class, new SimpleTableCellRenderer());

        titleLabel.setText(org.openide.util.NbBundle.getMessage(KeywordSearchJobSettingsPanel.class, "KeywordSearchJobSettingsPanel.titleLabel.text")); // NOI18N

        languagesLabel.setText(org.openide.util.NbBundle.getMessage(KeywordSearchJobSettingsPanel.class, "KeywordSearchJobSettingsPanel.languagesLabel.text")); // NOI18N
        languagesLabel.setToolTipText(org.openide.util.NbBundle.getMessage(KeywordSearchJobSettingsPanel.class, "KeywordSearchJobSettingsPanel.languagesLabel.toolTipText")); // NOI18N
        languagesLabel.setPreferredSize(new java.awt.Dimension(294, 35));
        languagesLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        languagesValLabel.setText(org.openide.util.NbBundle.getMessage(KeywordSearchJobSettingsPanel.class, "KeywordSearchJobSettingsPanel.languagesValLabel.text")); // NOI18N
        languagesValLabel.setToolTipText(org.openide.util.NbBundle.getMessage(KeywordSearchJobSettingsPanel.class, "KeywordSearchJobSettingsPanel.languagesValLabel.toolTipText")); // NOI18N

        encodingsLabel.setText(org.openide.util.NbBundle.getMessage(KeywordSearchJobSettingsPanel.class, "KeywordSearchJobSettingsPanel.encodingsLabel.text")); // NOI18N

        keywordSearchEncodings.setText(org.openide.util.NbBundle.getMessage(KeywordSearchJobSettingsPanel.class, "KeywordSearchJobSettingsPanel.keywordSearchEncodings.text")); // NOI18N

        ocrCheckBox.setText(org.openide.util.NbBundle.getMessage(KeywordSearchJobSettingsPanel.class, "KeywordSearchJobSettingsPanel.ocrCheckBox.text")); // NOI18N
        ocrCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ocrCheckBoxActionPerformed(evt);
            }
        });

        limitedOcrCheckbox.setText(org.openide.util.NbBundle.getMessage(KeywordSearchJobSettingsPanel.class, "KeywordSearchJobSettingsPanel.limitedOcrCheckbox.text")); // NOI18N
        limitedOcrCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limitedOcrCheckboxActionPerformed(evt);
            }
        });

        ocrOnly.setText(org.openide.util.NbBundle.getMessage(KeywordSearchJobSettingsPanel.class, "KeywordSearchJobSettingsPanel.ocrOnly.text")); // NOI18N
        ocrOnly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ocrOnlyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(languagesLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(listsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(titleLabel)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(encodingsLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(keywordSearchEncodings)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(languagesValLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(ocrCheckBox)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(limitedOcrCheckbox)
                                    .addComponent(ocrOnly))))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(titleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(listsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 22, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(languagesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(languagesValLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(encodingsLabel)
                    .addComponent(keywordSearchEncodings))
                .addGap(18, 18, 18)
                .addComponent(ocrCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ocrOnly)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(limitedOcrCheckbox)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ocrCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ocrCheckBoxActionPerformed
        limitedOcrCheckbox.setEnabled(ocrCheckBox.isSelected());
        firePropertyChange(KeywordSearchOptionsPanelController.PROP_CHANGED, null, null);
    }//GEN-LAST:event_ocrCheckBoxActionPerformed

    private void limitedOcrCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limitedOcrCheckboxActionPerformed
        firePropertyChange(KeywordSearchOptionsPanelController.PROP_CHANGED, null, null);
    }//GEN-LAST:event_limitedOcrCheckboxActionPerformed

    private void ocrOnlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ocrOnlyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ocrOnlyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel encodingsLabel;
    private javax.swing.JLabel keywordSearchEncodings;
    private javax.swing.JLabel languagesLabel;
    private javax.swing.JLabel languagesValLabel;
    private javax.swing.JCheckBox limitedOcrCheckbox;
    private javax.swing.JScrollPane listsScrollPane;
    private javax.swing.JTable listsTable;
    private javax.swing.JCheckBox ocrCheckBox;
    private javax.swing.JCheckBox ocrOnly;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
}
