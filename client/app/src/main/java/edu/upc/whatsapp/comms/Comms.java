package edu.upc.whatsapp.comms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

/**
 * Created by juanluis.
 */
public interface Comms {


     // String WhatsApp_server = "192.168.0.53:22223/whatsappServer";          //Local network server
   // String WhatsApp_server = "10.0.2.2:22223/whatsappServer";                 //For emulator use only

     String WhatsApp_server = "mads.dnsdynamic.com:62987/whatsappServer";       // Remote raspberry pi server @Norway
     String url_rpc = "http://"+WhatsApp_server+"/rpc";
     String ENDPOINT = "ws://"+WhatsApp_server+"/push";
     Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateSerializerDeserializer()).create();
}
