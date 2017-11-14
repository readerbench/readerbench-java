/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package view.events;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

public class TCPopupEventQueue extends EventQueue {

    public JPopupMenu popup;
    JTable table;
    private BasicAction cut, copy, paste, selectAll;

    public TCPopupEventQueue() {
        // createPopupMenu();
    }

    public void createPopupMenu(JTextComponent text) {
        cut = new CutAction("Cut", null);
        copy = new CopyAction("Copy", null);
        paste = new PasteAction("Paste", null);
        selectAll = new SelectAllAction("Select All", null);
        cut.setTextComponent(text);
        copy.setTextComponent(text);
        paste.setTextComponent(text);
        selectAll.setTextComponent(text);

        popup = new JPopupMenu();
        popup.add(cut);
        popup.add(copy);
        popup.add(paste);
        popup.addSeparator();
        popup.add(selectAll);
    }

    public void showPopup(Component parent, MouseEvent me) {
        popup.validate();
        popup.show(parent, me.getX(), me.getY());
    }

    protected void dispatchEvent(AWTEvent event) {
        super.dispatchEvent(event);
        if (!(event instanceof MouseEvent)) {
            return;
        }
        MouseEvent me = (MouseEvent) event;
        if (!me.isPopupTrigger()) {
            return;
        }
        if (!(me.getSource() instanceof Component)) {
            return;
        }
        Component comp = SwingUtilities.getDeepestComponentAt(
                (Component) me.getSource(), me.getX(), me.getY());
        if (!(comp instanceof JTextComponent)) {
            return;
        }
        if (MenuSelectionManager.defaultManager().getSelectedPath().length > 0) {
            return;
        }
        createPopupMenu((JTextComponent) comp);
        showPopup((Component) me.getSource(), me);
    }

    abstract class BasicAction extends AbstractAction {

        private static final long serialVersionUID = 5006680440254933254L;
        JTextComponent comp;

        public BasicAction(String text, Icon icon) {
            super(text, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void setTextComponent(JTextComponent comp) {
            this.comp = comp;
        }

        @Override
        public abstract void actionPerformed(ActionEvent e);
    }

    class CopyAction extends BasicAction {

        private static final long serialVersionUID = -1214148594589905316L;

        public CopyAction(String text, Icon icon) {
            super(text, icon);
            super.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            comp.copy();
        }

        @Override
        public boolean isEnabled() {
            return comp != null && comp.getSelectedText() != null;
        }
    }

    class PasteAction extends BasicAction {

        private static final long serialVersionUID = -7839049996225684877L;

        public PasteAction(String text, Icon icon) {
            super(text, icon);
            super.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl V"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            comp.paste();
        }

        @Override
        public boolean isEnabled() {
            Transferable content = Toolkit.getDefaultToolkit()
                    .getSystemClipboard().getContents(null);
            return comp != null && comp.isEnabled() && comp.isEditable()
                    && content.isDataFlavorSupported(DataFlavor.stringFlavor);
        }
    }

    class CutAction extends BasicAction {

        private static final long serialVersionUID = 7908757781681132110L;

        public CutAction(String text, Icon icon) {
            super(text, icon);
            super.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl X"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            comp.cut();
        }

        @Override
        public boolean isEnabled() {
            return comp != null && comp.isEditable()
                    && comp.getSelectedText() != null;
        }
    }

    class SelectAllAction extends BasicAction {

        private static final long serialVersionUID = 307510531271503675L;

        public SelectAllAction(String text, Icon icon) {
            super(text, icon);
            super.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl A"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            comp.selectAll();
        }

        @Override
        public boolean isEnabled() {
            return comp != null
                    && comp.isEnabled()
                    && comp.getText().length() > 0
                    && (comp.getSelectedText() == null || comp
                    .getSelectedText().length() < comp.getText()
                            .length());
        }
    }
}
