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
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import view.models.document.CustomLineAnnotation;
import view.models.document.CustomToolTipGenerator;
import data.Block;
import data.Sentence;
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;

public class ChatTimeEvolution extends JFrame {

	private static final long serialVersionUID = -1563886410201416973L;

	private static final int AVERAGE_TYPING_SPEED = 19; // words per minute for
														// composition

	
	public ChatTimeEvolution(Conversation c) {
		super("ReaderBench - Chat Time Evolution");

		this.setSize(800, 600);
		this.setLocation(50, 50);
		TaskSeries s = new TaskSeries("Chat");
		Map<Participant, LinkedList<Block>> subTasks = new TreeMap<Participant, LinkedList<Block>>();

		for (Participant p : c.getParticipants()) {
			subTasks.put(p, new LinkedList<Block>());
			int startIndex = 0, endIndex = p.getInterventions().getBlocks()
					.size() - 1;
			while (startIndex < p.getInterventions().getBlocks().size()
					&& p.getInterventions().getBlocks().get(startIndex) == null)
				startIndex++;

			while (endIndex >= 0
					&& p.getInterventions().getBlocks().get(endIndex) == null)
				endIndex--;
			Block startUtter = p.getInterventions().getBlocks().get(startIndex);
			Block endUtter = p.getInterventions().getBlocks().get(endIndex);

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
			for (Block b : p.getInterventions().getBlocks()) {
				if (b != null) {
					Task subT = null;
					start = Calendar.getInstance();
					start.setTime(start((Utterance) b));
					end = Calendar.getInstance();
					end.setTime(end((Utterance) b));

					lastEndCal.add(Calendar.SECOND, 2);

					if (lastEndCal.after(start))
						start = lastEndCal;
					if (b.getRefBlock() != null) {
						Calendar endRef = Calendar.getInstance();
						endRef.setTime(end((Utterance) b.getRefBlock()));
						endRef.add(Calendar.SECOND, 2);
						if (endRef.after(start))
							start.setTime(endRef.getTime());
					}
					if (start.after(end)) {
						start.setTime(end.getTime());
						start.add(Calendar.SECOND, -2);
					}
					subT = new Task(b.getText(), start.getTime(), end.getTime());
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

		this.setContentPane(chartPanel);
	}

	public ChatTimeEvolution(String title) {
		super("Chat Time Evolution");
		this.setSize(800, 600);
		this.setLocation(50, 50);

		IntervalCategoryDataset dataset = createSampleDataset();

		// create the chart...
		JFreeChart chart = ChartFactory.createGanttChart("Gantt Chart Demo", // chart
				// title
				"Task", // domain axis label
				"Date", // range axis label
				dataset, // data
				true, // include legend
				true, // tooltips
				true // urls
				);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		CustomLineAnnotation annotation = new CustomLineAnnotation(
				"Write Proposal", dataset.getEndValue(0, 0).doubleValue(),
				"Obtain Approval", dataset.getStartValue(0, 1).doubleValue(),
				Color.DARK_GRAY, new BasicStroke(2.0f));

		plot.addAnnotation(annotation);

		annotation = new CustomLineAnnotation("Requirements Analysis",
				((TaskSeriesCollection) dataset).getEndValue(0, 2, 0)
						.doubleValue(), "Design Phase",
				((TaskSeriesCollection) dataset).getStartValue(0, 3, 0)
						.doubleValue(), Color.DARK_GRAY, new BasicStroke(2.0f));

		plot.addAnnotation(annotation);

		GanttRenderer renderer = new GanttRenderer();
		renderer.setBaseToolTipGenerator(new CustomToolTipGenerator());
		renderer.setSeriesPaint(0, Color.blue);
		plot.setRenderer(renderer);

		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(chart);

		this.setContentPane(chartPanel);
	}

	private IntervalCategoryDataset createSampleDataset() {

		TaskSeries s1 = new TaskSeries("Scheduled");

		Task t1 = new Task("Write Proposal", date(1, Calendar.APRIL, 2001),
				date(5, Calendar.APRIL, 2001));
		s1.add(t1);

		Task t2 = new Task("Obtain Approval", date(9, Calendar.APRIL, 2001),
				date(9, Calendar.APRIL, 2001));
		s1.add(t2);

		// here is a task split into two subtasks...
		Task t3 = new Task("Requirements Analysis", date(10, Calendar.APRIL,
				2001), date(5, Calendar.MAY, 2001));
		Task st31 = new Task("Requirements 1", date(10, Calendar.APRIL, 2001),
				date(25, Calendar.APRIL, 2001));
		Task st32 = new Task("Requirements 2", date(1, Calendar.MAY, 2001),
				date(5, Calendar.MAY, 2001));
		t3.addSubtask(st31);
		t3.addSubtask(st32);
		s1.add(t3);

		// and another...
		Task t4 = new Task("Design Phase", date(6, Calendar.MAY, 2001), date(
				30, Calendar.MAY, 2001));
		Task st41 = new Task("Design 1", date(6, Calendar.MAY, 2001), date(10,
				Calendar.MAY, 2001));
		Task st42 = new Task("Design 2", date(15, Calendar.MAY, 2001), date(20,
				Calendar.MAY, 2001));
		Task st43 = new Task("Design 3", date(23, Calendar.MAY, 2001), date(30,
				Calendar.MAY, 2001));
		t4.addSubtask(st41);
		t4.addSubtask(st42);
		t4.addSubtask(st43);
		s1.add(t4);

		Task t5 = new Task("Design Signoff", date(2, Calendar.JUNE, 2001),
				date(2, Calendar.JUNE, 2001));
		s1.add(t5);

		Task t6 = new Task("Alpha Implementation",
				date(3, Calendar.JUNE, 2001), date(31, Calendar.JULY, 2001));

		s1.add(t6);

		Task t7 = new Task("Design Review", date(1, Calendar.AUGUST, 2001),
				date(8, Calendar.AUGUST, 2001));
		s1.add(t7);

		Task t8 = new Task("Revised Design Signoff", date(10, Calendar.AUGUST,
				2001), date(10, Calendar.AUGUST, 2001));
		s1.add(t8);

		Task t9 = new Task("Beta Implementation", date(12, Calendar.AUGUST,
				2001), date(12, Calendar.SEPTEMBER, 2001));
		s1.add(t9);

		Task t10 = new Task("Testing", date(13, Calendar.SEPTEMBER, 2001),
				date(31, Calendar.OCTOBER, 2001));
		s1.add(t10);

		Task t11 = new Task(" Implementation",
				date(1, Calendar.NOVEMBER, 2001), date(15, Calendar.NOVEMBER,
						2001));
		s1.add(t11);

		Task t12 = new Task("Signoff", date(28, Calendar.NOVEMBER, 2001), date(
				30, Calendar.NOVEMBER, 2001));
		s1.add(t12);

		TaskSeriesCollection collection = new TaskSeriesCollection();
		collection.add(s1);

		return collection;
	}

	private static Date date(int day, int month, int year) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day);
		Date result = calendar.getTime();
		return result;
	}

	private static Date end(Utterance u) {
		if (u.getTime() != null)
			return u.getTime();
		return new Date((u.getIndex() + 1) * 60 * 1000);
	}

	private static Date start(Utterance u) {
		if (u != null) {
			int noWords = 0;
			for (Sentence s : u.getSentences()) {
				if (s != null) {
					noWords += s.getAllWords().size();
				}
			}
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
