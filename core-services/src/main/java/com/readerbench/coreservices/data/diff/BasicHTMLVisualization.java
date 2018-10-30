/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.coreservices.data.diff;

import com.readerbench.coreservices.data.AnalysisElement;
import com.readerbench.coreservices.data.Sentence;
import com.readerbench.coreservices.data.document.Document;
import difflib.DiffRow;

/**
 *
 * @author Adrian-Ionuț Tănase
 */
public class BasicHTMLVisualization {
    
    
    static String diffToString(AnalysisElement oldLine) {
        StringBuilder sb = new StringBuilder();

        oldLine.getChanges().stream().map((block) -> {
            if (block.getTag() == DiffRow.Tag.CHANGE) {
                if (oldLine instanceof Sentence){
                    if (((Object) block.getOldLine()) instanceof String) {
                    } else {
                        sb.append(colorText(block.getOldLine().getText(), "red"))
                                .append(" ");
                    }

                    if (((Object) block.getNewLine()) instanceof String) {
                    } else {
                        sb.append(colorText(block.getNewLine().getText(), "blue"))
                                .append(" ");
                    }
                } else {
                    if (((Object)block.getOldLine()) instanceof String)
                    {
                        sb.append(block.getOldLine());
                    } else 
                    {
                        sb.append(diffToString(block.getOldLine()));
                    }
                }
            }
            return block;
        }).map((block) -> {
            if (block.getTag() == DiffRow.Tag.EQUAL) {
                sb.append(block.getOldLine().getText());
            }
            return block;
        }).map((block) -> {
            if (block.getTag() == DiffRow.Tag.DELETE) {
                sb.append(colorText(block.getOldLine().getText(), "orange"));
            }
            return block;
        }).map((block) -> {
            if (block.getTag() == DiffRow.Tag.INSERT) {
                sb.append(colorText(block.getNewLine().getText(), "green"));
            }
            return block;
        }).forEachOrdered((_item) -> {
            if (oldLine instanceof Document) {
                sb.append("<br>");
            } else {
                sb.append(" ");
            }
        });

        return sb.toString();
    }
    
    private static String colorText(String text, String color) {
        String phrase = " <font color='" + color + "'><b>" + text + "</b></font> ";
        return phrase.trim();
    }
}
