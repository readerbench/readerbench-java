/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view.widgets.article.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ionutparaschiv
 */
public class OrderedAuthorsArticlesByBetweenness implements java.io.Serializable {

    public static String SerializedFileLocation = "resources/in/LAK_corpus/parsed-documents/orderedAuthorsArticlesByBetweenness.set";

    private List<String> authorUriList;
    private List<String> articleUriList;

    public OrderedAuthorsArticlesByBetweenness(List<GraphMeasure> graphMeasures) {
        this.authorUriList = new ArrayList<>();
        this.articleUriList = new ArrayList<>();
        graphMeasures.stream().forEach(measure -> {
            if (measure.getNodeType() == GraphNodeItemType.Article) {
                this.articleUriList.add(measure.getUri());
            } else if (measure.getNodeType() == GraphNodeItemType.Author) {
                this.authorUriList.add(measure.getUri());
            }
        });
    }

    public void saveSerializedObject() {
        try {
            FileOutputStream fout = new FileOutputStream(SerializedFileLocation);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(this);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static OrderedAuthorsArticlesByBetweenness readSerializedObject() {
        OrderedAuthorsArticlesByBetweenness obj = null;
        try {
            ObjectInputStream objectinputstream = new ObjectInputStream(new FileInputStream(SerializedFileLocation));
            obj = (OrderedAuthorsArticlesByBetweenness) objectinputstream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public String toString() {
        return "authors = { " + this.authorUriList + " }, articles = { " + this.articleUriList + " }";
    }

    public List<String> getAuthorUriList() {
        return this.authorUriList;
    }

    public List<String> getArticleUriList() {
        return this.articleUriList;
    }
}
