package pojo;

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
    @NamedQuery(name = "SentimentValence.findByName", query = "SELECT s FROM SentimentValence s WHERE s.name = :name"),
    @NamedQuery(name = "SentimentValence.findByRage", query = "SELECT s FROM SentimentValence s WHERE s.rage = :rage")})
public class SentimentValence implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @Column(name = "rage")
    private boolean rage;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkPrimaryValence", fetch = FetchType.LAZY)
    private List<Weight> weightList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkRageValence", fetch = FetchType.LAZY)
    private List<Weight> weightList1;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sentimentValenceId", fetch = FetchType.LAZY)
    private List<EntityXValence> entityXValenceList;

    public SentimentValence() {
    }

    public SentimentValence(Integer id) {
        this.id = id;
    }

    public SentimentValence(Integer id, String name, boolean rage) {
        this.id = id;
        this.name = name;
        this.rage = rage;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getRage() {
        return rage;
    }

    public void setRage(boolean rage) {
        this.rage = rage;
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
    
}
