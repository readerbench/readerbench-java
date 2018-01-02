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
package com.readerbench.data.pojo;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 *
 * @author Stefan
 */
@Entity
@Table(name = "word")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Word.findAll", query = "SELECT w FROM Word w"),
    @NamedQuery(name = "Word.findById", query = "SELECT w FROM Word w WHERE w.id = :id"),
    @NamedQuery(name = "Word.findByLabel", query = "SELECT w FROM Word w WHERE w.fkLanguage = :lang and w.label = :label"),
    @NamedQuery(name = "Word.findByLang", query = "SELECT w FROM Word w WHERE w.fkLanguage = :lang"),
    @NamedQuery(name = "Word.findByPrefix", query = "SELECT w FROM Word w WHERE w.fkLanguage = :lang and w.label like :label")})
public class Word implements Serializable {
    @JoinColumn(name = "fk_language", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Language fkLanguage;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "label")
    private String label;
    @JoinColumn(name = "fk_sentiment_entity", referencedColumnName = "id")
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private SentimentEntity fkSentimentEntity;

    public Word() {
    }

    public Word(Integer id) {
        this.id = id;
    }

    public Word(Integer id, String label) {
        this.id = id;
        this.label = label;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public SentimentEntity getFkSentimentEntity() {
        return fkSentimentEntity;
    }

    public void setFkSentimentEntity(SentimentEntity fkSentimentEntity) {
        this.fkSentimentEntity = fkSentimentEntity;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Word)) {
            return false;
        }
        Word other = (Word) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return label;
    }

    public Language getFkLanguage() {
        return fkLanguage;
    }

    public void setFkLanguage(Language fkLanguage) {
        this.fkLanguage = fkLanguage;
    }

}
