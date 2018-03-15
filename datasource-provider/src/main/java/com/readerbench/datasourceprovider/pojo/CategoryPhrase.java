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
@Table(name = "category_phrase")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CategoryPhrase.findAll", query = "SELECT c FROM CategoryPhrase c"),
    @NamedQuery(name = "CategoryPhrase.findById", query = "SELECT c FROM CategoryPhrase c WHERE c.id = :id"),
    @NamedQuery(name = "CategoryPhrase.findByLabel", query = "SELECT c FROM CategoryPhrase c WHERE c.label = :label")})
public class CategoryPhrase implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "label")
    private String label;
    @JoinColumn(name = "fk_category", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Category fkCategory;

    public CategoryPhrase() {
    }

    public CategoryPhrase(Integer id) {
        this.id = id;
    }

    public CategoryPhrase(Integer id, String label) {
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

    public Category getFkCategory() {
        return fkCategory;
    }

    public void setFkCategory(Category fkCategory) {
        this.fkCategory = fkCategory;
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
        if (!(object instanceof CategoryPhrase)) {
            return false;
        }
        CategoryPhrase other = (CategoryPhrase) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "data.pojo.CategoryPhrase[ id=" + id + " ]";
    }
    
}
