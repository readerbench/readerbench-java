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
@Table(name = "entity_x_valence")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "EntityXValence.findAll", query = "SELECT e FROM EntityXValence e"),
    @NamedQuery(name = "EntityXValence.findById", query = "SELECT e FROM EntityXValence e WHERE e.id = :id"),
    @NamedQuery(name = "EntityXValence.findByValue", query = "SELECT e FROM EntityXValence e WHERE e.value = :value"),
    @NamedQuery(name = "EntityXValence.findBySentimentEntity", query = "SELECT e FROM EntityXValence e WHERE e.fkSentimentEntity = :se")
})
public class EntityXValence implements Serializable {

    @JoinColumn(name = "fk_sentiment_entity", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private SentimentEntity fkSentimentEntity;
    @JoinColumn(name = "fk_sentiment_valence", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private SentimentValence fkSentimentValence;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "value")
    private Double value;

    public EntityXValence() {
    }

    public EntityXValence(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
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
        if (!(object instanceof EntityXValence)) {
            return false;
        }
        EntityXValence other = (EntityXValence) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "pojo.EntityXValence[ id=" + id + " ]";
    }

    public SentimentEntity getFkSentimentEntity() {
        return fkSentimentEntity;
    }

    public void setFkSentimentEntity(SentimentEntity fkSentimentEntity) {
        this.fkSentimentEntity = fkSentimentEntity;
    }

    public SentimentValence getFkSentimentValence() {
        return fkSentimentValence;
    }

    public void setFkSentimentValence(SentimentValence fkSentimentValence) {
        this.fkSentimentValence = fkSentimentValence;
    }

}
