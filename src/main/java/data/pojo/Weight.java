package data.pojo;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

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
