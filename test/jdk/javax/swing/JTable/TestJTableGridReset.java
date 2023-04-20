/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
/*
 * @test
 * @bug 7154070
 * @key headful
 * @summary  Verifies if switching between LaFs JTable dividers setting are not lost
 * @run main TestJTableGridReset
 */

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class TestJTableGridReset {

    private static JTable table;
    private static volatile boolean origHorizLines;
    private static volatile boolean origVertLines;
    private static volatile boolean curHorizLines;
    private static volatile boolean curVertLines;

    private static void setLookAndFeel(UIManager.LookAndFeelInfo laf) {
        try {
            UIManager.setLookAndFeel(laf.getClassName());
        } catch (UnsupportedLookAndFeelException ignored) {
            System.out.println("Unsupported L&F: " + laf.getClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        for (UIManager.LookAndFeelInfo laf :
                 UIManager.getInstalledLookAndFeels()) {
            System.out.println("Testing L&F: " + laf.getClassName());
            SwingUtilities.invokeAndWait(() -> {
                setLookAndFeel(laf);
                table = new JTable();
                table.setShowHorizontalLines(true);
                table.setShowVerticalLines(true);
                SwingUtilities.updateComponentTreeUI(table);
                origHorizLines = table.getShowHorizontalLines();
                origVertLines = table.getShowVerticalLines();
            });
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.invokeAndWait(() -> {
                SwingUtilities.updateComponentTreeUI(table);
                setLookAndFeel(laf);
                SwingUtilities.updateComponentTreeUI(table);
                curHorizLines = table.getShowHorizontalLines();
                curVertLines = table.getShowVerticalLines();
            });
            System.out.println("origHorizLines " + origHorizLines +
                               " origVertLines " + origVertLines);
            System.out.println("curHorizLines " + curHorizLines +
                                " curVertLines " + curVertLines);
            if (origHorizLines != curHorizLines ||
                origVertLines != curVertLines) {
                throw new RuntimeException("Horizontal/Vertical grid lines not restored");
            }
        }
    }
}

