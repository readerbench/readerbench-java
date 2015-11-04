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
@Table(name = "sentiment_entity")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SentimentEntity.findAll", query = "SELECT s FROM SentimentEntity s"),
    @NamedQuery(name = "SentimentEntity.findById", query = "SELECT s FROM SentimentEntity s WHERE s.id = :id")})
public class SentimentEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sentimentEntityId", fetch = FetchType.LAZY)
    private List<EntityXValence> entityXValenceList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkSentimentEntity", fetch = FetchType.LAZY)
    private List<Word> wordList;

    public SentimentEntity() {
    }

    public SentimentEntity(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @XmlTransient
    @JsonIgnore
    public List<EntityXValence> getEntityXValenceList() {
        return entityXValenceList;
    }

    public void setEntityXValenceList(List<EntityXValence> entityXValenceList) {
        this.entityXValenceList = entityXValenceList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Word> getWordList() {
        return wordList;
    }

    public void setWordList(List<Word> wordList) {
        this.wordList = wordList;
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
        if (!(object instanceof SentimentEntity)) {
            return false;
        }
        SentimentEntity other = (SentimentEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "pojo.SentimentEntity[ id=" + id + " ]";
    }
    
}
