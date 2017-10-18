package org.priere.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.priere.domain.security.UserEntity;

@Entity(name="Programme")
@Table(name="\"PROGRAMME\"")
public class ProgrammeEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private ProgrammeImage image;
    
    @Column(name="\"date\"")
    @Temporal(TemporalType.DATE)
    private Date date;

    @ManyToOne(optional=true)
    @JoinColumn(name = "OFFICIANT_ID", referencedColumnName = "ID")
    private UserEntity officiant;

    @ManyToOne(optional=true)
    @JoinColumn(name = "PREDICATEUR_ID", referencedColumnName = "ID")
    private UserEntity predicateur;

    public ProgrammeImage getImage() {
        return image;
    }

    public void setImage(ProgrammeImage image) {
        this.image = image;
    }
    
    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public UserEntity getOfficiant() {
        return this.officiant;
    }

    public void setOfficiant(UserEntity user) {
        this.officiant = user;
    }

    public UserEntity getPredicateur() {
        return this.predicateur;
    }

    public void setPredicateur(UserEntity user) {
        this.predicateur = user;
    }

}
