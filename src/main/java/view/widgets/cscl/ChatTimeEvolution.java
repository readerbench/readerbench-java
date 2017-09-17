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
package view.widgets.cscl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import view.models.document.CustomLineAnnotation;
import view.models.document.CustomToolTipGenerator;
import data.Block;
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import java.util.List;
import utils.LocalizationUtils;

public class ChatTimeEvolution extends JFrame {

    private static final long serialVersionUID = -1563886410201416973L;

    private static final int AVERAGE_TYPING_SPEED = 19; // words per minute for
    // composition

    public ChatTimeEvolution(Conversation c) {
        super.setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
        super.setSize(800, 600);
        super.setLocation(50, 50);
        TaskSeries s = new TaskSeries("Chat");
        Map<Participant, List<Block>> subTasks = new TreeMap<>();

        for (Participant p : c.getParticipants()) {
            subTasks.put(p, new ArrayList<>());
            int startIndex = 0, endIndex = p.getContributions().getBlocks()
                    .size() - 1;
            while (startIndex < p.getContributions().getBlocks().size()
                    && p.getContributions().getBlocks().get(startIndex) == null) {
                startIndex++;
            }

            while (endIndex >= 0
                    && p.getContributions().getBlocks().get(endIndex) == null) {
                endIndex--;
            }
            Block startUtter = p.getContributions().getBlocks().get(startIndex);
            Block endUtter = p.getContributions().getBlocks().get(endIndex);

            Calendar start, end;
            start = Calendar.getInstance();
            start.setTime(start((Utterance) startUtter));
            end = Calendar.getInstance();
            end.setTime(end((Utterance) endUtter));
            if (start.after(end)) {
                start.setTime(end.getTime());
                start.add(Calendar.SECOND, -2);
            }
            Task t = new Task(p.getName(), start.getTime(), end.getTime());
            s.add(t);

            Calendar lastEndCal = Calendar.getInstance();
            lastEndCal.set(1900, 0, 0);
            for (Block b : p.getContributions().getBlocks()) {
                if (b != null) {
                    start = Calendar.getInstance();
                    start.setTime(start((Utterance) b));
                    end = Calendar.getInstance();
                    end.setTime(end((Utterance) b));

                    lastEndCal.add(Calendar.SECOND, 2);

                    if (lastEndCal.after(start)) {
                        start = lastEndCal;
                    }
                    if (b.getRefBlock() != null) {
                        Calendar endRef = Calendar.getInstance();
                        endRef.setTime(end((Utterance) b.getRefBlock()));
                        endRef.add(Calendar.SECOND, 2);
                        if (endRef.after(start)) {
                            start.setTime(endRef.getTime());
                        }
                    }
                    if (start.after(end)) {
                        start.setTime(end.getTime());
                        start.add(Calendar.SECOND, -2);
                    }
                    Task subT = new Task(b.getText(), start.getTime(), end.getTime());
                    lastEndCal.setTime(end((Utterance) b));
                    t.addSubtask(subT);

                    subTasks.get(p).add(b);
                }
            }
        }

        TaskSeriesCollection collection = new TaskSeriesCollection();
        collection.add(s);

        // create the chart...
        JFreeChart chart = ChartFactory.createGanttChart("Chat Evolution", // chart
                // title
                "Participant", // domain axis label
                "Time", // range axis label
                collection, // data
                true, // include legend
                true, // tooltips
                false // urls
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();

        // add dependencies
        CustomLineAnnotation annotation;
        c.getParticipants();
        for (int i = 0; i < c.getBlocks().size() - 1; i++) {
            for (int j = i + 1; j < c.getBlocks().size(); j++) {
                if (c.getPrunnedBlockDistances()[i][j] != null) {
                    Block b1 = c.getBlocks().get(i);
                    Participant participant1 = ((Utterance) b1)
                            .getParticipant();
                    int idParticipant1 = c.getParticipants().indexOf(participant1);
                    Number end1 = collection.getEndValue(0, idParticipant1,
                            subTasks.get(participant1).indexOf(b1));

                    Block b2 = c.getBlocks().get(j);
                    Participant participant2 = ((Utterance) b2)
                            .getParticipant();
                    int idParticipant2 = c.getParticipants().indexOf(participant2);
                    Number start2 = collection.getStartValue(0, idParticipant2,
                            subTasks.get(participant2).indexOf(b2));

                    annotation = new CustomLineAnnotation(
                            participant1.getName(), end1.doubleValue(),
                            participant2.getName(), start2.doubleValue(),
                            Color.DARK_GRAY, new BasicStroke(2.0f));

                    plot.addAnnotation(annotation);
                }
            }
        }

        GanttRenderer renderer = new GanttRenderer();
        renderer.setBaseToolTipGenerator(new CustomToolTipGenerator());
        renderer.setSeriesPaint(0, Color.blue);
        plot.setRenderer(renderer);

        // add the chart to a panel...
        ChartPanel chartPanel = new ChartPanel(chart);

        super.setContentPane(chartPanel);
    }

    private static Date end(Utterance u) {
        if (u.getTime() != null) {
            return u.getTime();
        }
        return new Date((u.getIndex() + 1) * 60 * 1000);
    }

    private static Date start(Utterance u) {
        if (u != null) {
            int noWords = 0;
            noWords = u.getSentences().stream().filter((s) -> (s != null)).map((s) -> s.getAllWords().size()).reduce(noWords, Integer::sum);
            if (u.getTime() != null) {
                int seconds = (int) (((double) (noWords * 60)) / AVERAGE_TYPING_SPEED);
                Calendar cal = Calendar.getInstance();
                cal.setTime(u.getTime());
                cal.add(Calendar.SECOND, -seconds);
                return cal.getTime();
            } else {
                return new Date(u.getIndex() * 60 * 1000);
            }
        }
        return null;
    }
}
