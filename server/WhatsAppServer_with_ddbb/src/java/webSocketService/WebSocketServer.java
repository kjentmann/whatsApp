package webSocketService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entity.Message;
import entity.UserInfo;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import util.DateSerializerDeserializer;

@ServerEndpoint("/push")
public class WebSocketServer {
  
  private static Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateSerializerDeserializer()).create();

  //Open connections from clients:
  private static Map<Session, Integer> sessions = new HashMap<Session, Integer>();
  private static Map<Integer, Session> userInfo_ids = new HashMap<Integer, Session>();

  @OnMessage
  public void onMessage(String message, Session session)
    throws IOException, SQLException {
    System.out.println("onMessage: " + message);
    UserInfo userInfo = gson.fromJson(message, UserInfo.class);
    userInfo_ids.put(userInfo.getId(), session);
    sessions.put(session, userInfo.getId());
  }

  @OnOpen
  public void onOpen(Session session) {
    System.out.println("new session: " + session.getId());
  }

  @OnClose
  public void onClose(Session session) {
    System.out.println("closed session: " + session.getId());
    int userInfo_id = sessions.get(session);
    sessions.remove(session);
    userInfo_ids.remove(userInfo_id);
  }

  public static void push(Message message) {
    Session session = userInfo_ids.get(message.getUserReceiver().getId());
    if (session != null) {
      if (!session.isOpen()) {
        int userInfo_id = sessions.get(session);
        sessions.remove(session);
        userInfo_ids.remove(userInfo_id);
        return;
      }
      try {
        String json_message = gson.toJson(message);
        session.getBasicRemote().sendText(json_message);
        System.out.println("Sending: " + json_message + "\nto session_Id:" + session.getId());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
