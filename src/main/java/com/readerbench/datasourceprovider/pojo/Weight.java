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
package com.readerbench.datasourceprovider.pojo;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 *
 * @author Stefan
 */
@Entity
@Table(name = "weight")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Weight.findAll", query = "SELECT w FROM Weight w"),
    @NamedQuery(name = "Weight.findById", query = "SELECT w FROM Weight w WHERE w.id = :id"),
    @NamedQuery(name = "Weight.findByValue", query = "SELECT w FROM Weight w WHERE w.value = :value"),
	@NamedQuery(name = "Weight.findByPair", query = "SELECT w FROM Weight w WHERE w.fkPrimaryValence = :primaryValence AND w.fkRageValence = :rageValence")})
public class Weight implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "value")
    private double value;
    @JoinColumn(name = "fk_primary_valence", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private SentimentValence fkPrimaryValence;
    @JoinColumn(name = "fk_rage_valence", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private SentimentValence fkRageValence;

    public Weight() {
    }

    public Weight(Integer id) {
        this.id = id;
    }

    public Weight(Integer id, double value) {
        this.id = id;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public SentimentValence getFkPrimaryValence() {
        return fkPrimaryValence;
    }

    public void setFkPrimaryValence(SentimentValence fkPrimaryValence) {
        this.fkPrimaryValence = fkPrimaryValence;
    }

    public SentimentValence getFkRageValence() {
        return fkRageValence;
    }

    public void setFkRageValence(SentimentValence fkRageValence) {
        this.fkRageValence = fkRageValence;
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
        if (!(object instanceof Weight)) {
            return false;
        }
        Weight other = (Weight) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "pojo.Weight[ id=" + id + " ]";
    }
    
}
