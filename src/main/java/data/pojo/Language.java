package data.pojo;

import dao.LanguageDAO;
import edu.cmu.lti.jawjaw.pobj.Lang;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@Table(name = "language")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Language.findAll", query = "SELECT l FROM Language l"),
    @NamedQuery(name = "Language.findById", query = "SELECT l FROM Language l WHERE l.id = :id"),
    @NamedQuery(name = "Language.findByShortLabel", query = "SELECT l FROM Language l WHERE l.shortLabel = :shortLabel"),
    @NamedQuery(name = "Language.findByLabel", query = "SELECT l FROM Language l WHERE l.label = :label")})
public class Language implements Serializable {
    private static final Map<Lang, Language> convertion = new HashMap<>();
    static {
        convertion.put(Lang.eng, LanguageDAO.getInstance().findById(1));
        convertion.put(Lang.es, LanguageDAO.getInstance().findById(2));
        convertion.put(Lang.fr, LanguageDAO.getInstance().findById(3));
        convertion.put(Lang.it, LanguageDAO.getInstance().findById(4));
        convertion.put(Lang.jpn, LanguageDAO.getInstance().findById(5));
        convertion.put(Lang.nl, LanguageDAO.getInstance().findById(6));
        convertion.put(Lang.ro, LanguageDAO.getInstance().findById(7));
    }
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "short_label")
    private String shortLabel;
    @Basic(optional = false)
    @Column(name = "label")
    private String label;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkLanguage", fetch = FetchType.LAZY)
    private List<Word> wordList;

    public Language() {
    }

    public Language(Integer id) {
        this.id = id;
    }
    
    public static Language fromLang(Lang lang) {
        return convertion.get(lang);
    }

    public Language(Integer id, String shortLabel, String label) {
        this.id = id;
        this.shortLabel = shortLabel;
        this.label = label;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public void setShortLabel(String shortLabel) {
        this.shortLabel = shortLabel;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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
        if (!(object instanceof Language)) {
            return false;
        }
        Language other = (Language) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "data.pojo.Language[ id=" + id + " ]";
    }
    
}
