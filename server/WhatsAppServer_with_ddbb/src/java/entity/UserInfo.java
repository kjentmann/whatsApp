/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author upcnet
 */
@Entity
@Table(name = "user_info")
@XmlRootElement
@NamedQueries({
  @NamedQuery(name = "UserInfo.findAll", query = "SELECT u FROM UserInfo u"),
  @NamedQuery(name = "UserInfo.findById", query = "SELECT u FROM UserInfo u WHERE u.id = :id"),
  @NamedQuery(name = "UserInfo.findByName", query = "SELECT u FROM UserInfo u WHERE u.name = :name"),
  @NamedQuery(name = "UserInfo.findBySurname", query = "SELECT u FROM UserInfo u WHERE u.surname = :surname")})
public class UserInfo implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "id")
  private Integer id;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 45)
  @Column(name = "name")
  private String name;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 45)
  @Column(name = "surname")
  private String surname;
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "userReceiver")
  private Collection<Message> messageCollection;
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "userSender")
  private Collection<Message> messageCollection1;
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "userInfo")
  private Collection<User> userCollection;

  public UserInfo() {
  }

  public UserInfo(Integer id) {
    this.id = id;
  }

  public UserInfo(Integer id, String name, String surname) {
    this.id = id;
    this.name = name;
    this.surname = surname;
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

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  @XmlTransient
  public Collection<Message> getMessageCollection() {
    return messageCollection;
  }

  public void setMessageCollection(Collection<Message> messageCollection) {
    this.messageCollection = messageCollection;
  }

  @XmlTransient
  public Collection<Message> getMessageCollection1() {
    return messageCollection1;
  }

  public void setMessageCollection1(Collection<Message> messageCollection1) {
    this.messageCollection1 = messageCollection1;
  }

  @XmlTransient
  public Collection<User> getUserCollection() {
    return userCollection;
  }

  public void setUserCollection(Collection<User> userCollection) {
    this.userCollection = userCollection;
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
    if (!(object instanceof UserInfo)) {
      return false;
    }
    UserInfo other = (UserInfo) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "entity.UserInfo[ id=" + id + " ]";
  }
  
}
