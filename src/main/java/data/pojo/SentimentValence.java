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
package data.pojo;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author Stefan
 */
@Entity
@Table(name = "sentiment_valence")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SentimentValence.findAll", query = "SELECT s FROM SentimentValence s"),
    @NamedQuery(name = "SentimentValence.findById", query = "SELECT s FROM SentimentValence s WHERE s.id = :id"),
    @NamedQuery(name = "SentimentValence.findByIndexLabel", query = "SELECT s FROM SentimentValence s WHERE s.indexLabel = :index"),
    @NamedQuery(name = "SentimentValence.findByRage", query = "SELECT s FROM SentimentValence s WHERE s.rage = :rage")})
public class SentimentValence implements Serializable {
	@Basic(optional = false)
    @Column(name = "label")
	private String label;
	@Basic(optional = false)
    @Column(name = "index_label")
	private String indexLabel;
	@Column(name = "rage")
	private Boolean rage;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkPrimaryValence", fetch = FetchType.LAZY)
    private List<Weight> weightList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkRageValence", fetch = FetchType.LAZY)
    private List<Weight> weightList1;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkSentimentValence", fetch = FetchType.LAZY)
    private List<EntityXValence> entityXValenceList;

    public SentimentValence() {
    }

    public SentimentValence(Integer id) {
        this.id = id;
    }

    public SentimentValence(Integer id, String label, boolean rage) {
        this.id = id;
        this.label = label;
        this.rage = rage;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @XmlTransient
    @JsonIgnore
    public List<Weight> getWeightList() {
        return weightList;
    }

    public void setWeightList(List<Weight> weightList) {
        this.weightList = weightList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Weight> getWeightList1() {
        return weightList1;
    }

    public void setWeightList1(List<Weight> weightList1) {
        this.weightList1 = weightList1;
    }

    @XmlTransient
    @JsonIgnore
    public List<EntityXValence> getEntityXValenceList() {
        return entityXValenceList;
    }

    public void setEntityXValenceList(List<EntityXValence> entityXValenceList) {
        this.entityXValenceList = entityXValenceList;
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
        if (!(object instanceof SentimentValence)) {
            return false;
        }
        SentimentValence other = (SentimentValence) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "pojo.SentimentValence[ id=" + id + " ]";
    }
	
	public Boolean getRage() {
		return rage;
	}

	public void setRage(Boolean rage) {
		this.rage = rage;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getIndexLabel() {
		return indexLabel;
	}

	public void setIndexLabel(String indexLabel) {
		this.indexLabel = indexLabel;
	}
    
}
