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

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class LinkMouseListener extends MouseAdapter {

	@Override
	public void mouseEntered(MouseEvent event) {
		JLabel label = (JLabel) event.getSource();
		label.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	@Override
	public void mouseExited(MouseEvent event) {
		JLabel label = (JLabel) event.getSource();
		label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		try {
			JLabel label = (JLabel) event.getSource();
			Desktop desktop = Desktop.getDesktop();
			URI uri = new java.net.URI(label.getText());
			desktop.browse(uri);
		} catch (URISyntaxException use) {
			throw new AssertionError(use);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			JOptionPane
					.showMessageDialog(
							null,
							"Sorry, a problem occurred while trying"
									+ " to open this link in your system's standard browser.",
							"A problem occured", JOptionPane.ERROR_MESSAGE);
		}
	}
}
