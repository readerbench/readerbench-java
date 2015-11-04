package pojo;

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
@Table(name = "entity_x_valence")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "EntityXValence.findAll", query = "SELECT e FROM EntityXValence e"),
    @NamedQuery(name = "EntityXValence.findById", query = "SELECT e FROM EntityXValence e WHERE e.id = :id"),
    @NamedQuery(name = "EntityXValence.findByValue", query = "SELECT e FROM EntityXValence e WHERE e.value = :value")})
public class EntityXValence implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "value")
    private Double value;
    @JoinColumn(name = "sentiment_entity_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private SentimentEntity sentimentEntityId;
    @JoinColumn(name = "sentiment_valence_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private SentimentValence sentimentValenceId;

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

    public SentimentEntity getSentimentEntityId() {
        return sentimentEntityId;
    }

    public void setSentimentEntityId(SentimentEntity sentimentEntityId) {
        this.sentimentEntityId = sentimentEntityId;
    }

    public SentimentValence getSentimentValenceId() {
        return sentimentValenceId;
    }

    public void setSentimentValenceId(SentimentValence sentimentValenceId) {
        this.sentimentValenceId = sentimentValenceId;
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
    
}
