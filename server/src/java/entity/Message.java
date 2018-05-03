/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author upcnet
 */
@Entity
@Table(name = "message")
@XmlRootElement
@NamedQueries({
  @NamedQuery(name = "Message.findAll", query = "SELECT m FROM Message m"),
  @NamedQuery(name = "Message.findById", query = "SELECT m FROM Message m WHERE m.id = :id"),
  @NamedQuery(name = "Message.findByContent", query = "SELECT m FROM Message m WHERE m.content = :content"),
  @NamedQuery(name = "Message.findByDate", query = "SELECT m FROM Message m WHERE m.date = :date")})
public class Message implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "id")
  private Integer id;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 250)
  @Column(name = "content")
  private String content;
  @Column(name = "date")
  @Temporal(TemporalType.TIMESTAMP)
  private Date date;
  @JoinColumn(name = "user_receiver", referencedColumnName = "id")
  @ManyToOne(optional = false)
  private UserInfo userReceiver;
  @JoinColumn(name = "user_sender", referencedColumnName = "id")
  @ManyToOne(optional = false)
  private UserInfo userSender;

  public Message() {
  }

  public Message(Integer id) {
    this.id = id;
  }

  public Message(Integer id, String content) {
    this.id = id;
    this.content = content;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public UserInfo getUserReceiver() {
    return userReceiver;
  }

  public void setUserReceiver(UserInfo userReceiver) {
    this.userReceiver = userReceiver;
  }

  public UserInfo getUserSender() {
    return userSender;
  }

  public void setUserSender(UserInfo userSender) {
    this.userSender = userSender;
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
    if (!(object instanceof Message)) {
      return false;
    }
    Message other = (Message) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "entity.Message[ id=" + id + " ]";
  }
  
}
