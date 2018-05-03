/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity.service;

import entity.Message;
import entity.UserInfo;
import java.util.List;
import java.util.Objects;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import webSocketService.WebSocketServer;

/**
 *
 * @author juan luis
 */
@Stateless
@Path("entity.message")
public class MessageFacadeREST extends AbstractFacade<Message> {
  @PersistenceContext(unitName = "WhatsAppServerPU")
  private EntityManager em;

  public MessageFacadeREST() {
    super(Message.class);
  }
  
  @POST
  @Override
  @Consumes({"application/xml", "application/json"})
  public void create(Message entity) {
    super.create(entity);
  }

  @PUT
  @Path("{id}")
  @Consumes({"application/xml", "application/json"})
  public void edit(@PathParam("id") Integer id, Message entity) {
    super.edit(entity);
  }

  @DELETE
  @Path("{id}")
  public void remove(@PathParam("id") Integer id) {
    super.remove(super.find(id));
  }

  @GET
  @Path("{id}")
  @Produces({"application/xml", "application/json"})
  public Message find(@PathParam("id") Integer id) {
    return super.find(id);
  }

  @GET
  @Override
  @Produces({"application/xml", "application/json"})
  public List<Message> findAll() {
    return super.findAll();
  }

  @GET
  @Path("{from}/{to}")
  @Produces({"application/xml", "application/json"})
  public List<Message> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
    return super.findRange(new int[]{from, to});
  }

  @GET
  @Path("count")
  @Produces("text/plain")
  public String countREST() {
    return String.valueOf(super.count());
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }
  
  @POST
  @Path("create")
  @Consumes({"application/xml", "application/json"})
  public void myCreate(Message entity) {
    //we check out if user_sender exist, user_receiver exist and message's content is not empty
    Query query = em.createQuery("select ui from UserInfo ui where ui.id=:id");
    query.setParameter("id", entity.getUserSender().getId());
    UserInfo user_sender = (UserInfo)query.getSingleResult();
    query.setParameter("id", entity.getUserReceiver().getId());
    UserInfo user_receiver = (UserInfo)query.getSingleResult();
    if(user_sender!=null && user_receiver!=null && !entity.getContent().equals("")){
      super.create(entity);
      em.flush();
      WebSocketServer.push(entity);
      System.out.println("message created: "+entity.getContent());
    }
  }
  
  @GET
  @Path("users/{from}/{to}")
  @Produces({"application/xml", "application/json"})
  public List<Message> retrieveMessages(@PathParam("from") Integer from, @PathParam("to") Integer to) {
    System.out.println("executing findMessages");
    Query query = em.createQuery("select ui from UserInfo ui where ui.id=:id");
    query.setParameter("id", from);
    UserInfo user_from = (UserInfo)query.getSingleResult();
    
    query.setParameter("id", to);
    UserInfo user_to = (UserInfo)query.getSingleResult();
    
    query = em.createQuery("select m from Message m where "
      + "(m.userSender=:sender and m.userReceiver=:receiver) or "
      + "(m.userSender=:receiver and m.userReceiver=:sender) order by m.date");
    query.setParameter("sender", user_from);
    query.setParameter("receiver", user_to);
    List list = query.getResultList();
    
    return list;
  }
  
  @POST
  @Path("users/{from}/{to}")
  @Consumes({"application/xml", "application/json"})
  @Produces({"application/xml", "application/json"})
  public List<Message> retrieveNewMessages(@PathParam("from") Integer from, @PathParam("to") Integer to, Message message) {
    System.out.println("executing findNewMessages");
    System.out.println("requesting New Messages from date: "+message.getDate().toString());
    Query query = em.createQuery("select ui from UserInfo ui where ui.id=:id");
    query.setParameter("id", from);
    UserInfo user_from = (UserInfo)query.getSingleResult();
    
    query.setParameter("id", to);
    UserInfo user_to = (UserInfo)query.getSingleResult();
    
    query = em.createQuery("select m from Message m where "
      + "((m.userSender=:sender and m.userReceiver=:receiver) or "
      + "(m.userSender=:receiver and m.userReceiver=:sender)) and m.date>=:date order by m.date");
    query.setParameter("sender", user_from);
    query.setParameter("receiver", user_to);
    query.setParameter("date", message.getDate());
    List<Message> list = query.getResultList();
    
    //we remove the message-parameter from the list:
    for(Message element : list){
      if(Objects.equals(element.getId(), message.getId())){
        list.remove(element);
        break;
      }
    }
    
    return list;
  }
  
}
