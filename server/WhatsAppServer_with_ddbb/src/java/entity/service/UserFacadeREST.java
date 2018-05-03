/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity.service;

import entity.User;
import entity.UserInfo;
import java.util.ArrayList;
import java.util.List;
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

/**
 *
 * @author juan luis
 */
@Stateless
@Path("entity.user")
public class UserFacadeREST extends AbstractFacade<User> {

  @PersistenceContext(unitName = "WhatsAppServerPU")
  private EntityManager em;

  public UserFacadeREST() {
    super(User.class);
  }

  @POST
  @Override
  @Consumes({"application/xml", "application/json"})
  public void create(User entity) {
    super.create(entity);
  }

  @PUT
  @Path("{id}")
  @Consumes({"application/xml", "application/json"})
  public void edit(@PathParam("id") Integer id, User entity) {
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
  public User find(@PathParam("id") Integer id) {
    return super.find(id);
  }

  @GET
  @Override
  @Produces({"application/xml", "application/json"})
  public List<User> findAll() {
    return super.findAll();
  }

  @GET
  @Path("{from}/{to}")
  @Produces({"application/xml", "application/json"})
  public List<User> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
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
  @Produces({"application/xml", "application/json"})
  public UserInfo createUser_return_UserInfo(User entity) {
    System.out.println("executing createUser_return_UserInfo");
    //login must be unique
    Query query = em.createQuery("select u from User u where u.login=:login");
    query.setParameter("login", entity.getLogin());
    List list = query.getResultList();
    if (list.isEmpty()) {
      em.persist(entity.getUserInfo());
      //flush necessary to retrieve (here update) the assigned id to UserInfo
      em.flush();
      em.persist(entity);
      return entity.getUserInfo();
    } else {
      UserInfo bad_registration = new UserInfo(-1);
      return bad_registration;
    }
  }

  @POST
  @Path("login")
  @Consumes({"application/xml", "application/json"})
  @Produces({"application/xml", "application/json"})
  public UserInfo loginUser(User user) {
    System.out.println("login: " + user.getLogin() + ", password: " + user.getPassword());
    Query query = em.createQuery("select u from User u where u.login=:login AND u.password=:password");
    query.setParameter("login", user.getLogin());
    query.setParameter("password", user.getPassword());
    try {
      return ((User) query.getSingleResult()).getUserInfo();
    } catch (Exception e) {
      UserInfo bad_login = new UserInfo(-1);
      return bad_login;
    }
  }

}
