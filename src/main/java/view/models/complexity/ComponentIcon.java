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
package view.models.complexity;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class ComponentIcon implements Icon {

    private final JComponent cmp;

    public ComponentIcon(JComponent cmp) {
        this.cmp = cmp;
    }

    @Override
    public int getIconWidth() {
        return cmp.getPreferredSize().width;
    }

    @Override
    public int getIconHeight() {
        return cmp.getPreferredSize().height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        SwingUtilities.paintComponent(g, cmp, (Container) c, x, y,
                getIconWidth(), getIconHeight());
    }
}
