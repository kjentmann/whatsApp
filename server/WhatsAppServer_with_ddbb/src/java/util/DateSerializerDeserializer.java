package util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by Juan Luis
 */

public class DateSerializerDeserializer implements JsonDeserializer<Date>, JsonSerializer<Date> {

  private static final String[] DATE_FORMATS = new String[]{
    "yyyy-MM-dd'T'HH:mm:ssZ",
    "yyyy-MM-dd'T'HH:mm:ss"
  };


  @Override
  public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    for (String format : DATE_FORMATS) {
      try {
        System.out.println("received date to deserialize: "+json.getAsString());
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = sdf.parse(json.getAsString());
        System.out.println("translated to server local hour: "+date.toString());
        return date;
      } catch (ParseException e) {
        e.printStackTrace();
        System.out.println("Unparseable date: " + json.getAsString());
        System.out.println("error parsing at offset: "+e.getErrorOffset());
      }
    }
    //to process Date if it is as Long:
    try {
      return new Date(json.getAsLong());
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Unparseable date: " + json.getAsString());
    }

    throw new JsonParseException("Unparseable date: \"" + json.getAsString()
        + "\". Supported formats: " + Arrays.toString(DATE_FORMATS));
  }

  @Override
  public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context) {
    //se envia al servidor de BBDD con formato de time Zone:
    String format = "yyyy-MM-dd'T'HH:mm:ssZ";
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    String date_string = sdf.format(date);
    
    //for the template, example: yyyy-MM-dd'T'HH:mm:ss+0200
    //we add the colon punctuation at the time Zone offse,
    //example: yyyy-MM-dd'T'HH:mm:ss+02:00
    //this only has to be done to send a date to the DDBB,
    //from Java client to Java server it is not necessary:
    
    date_string = add_colon_toZformat(date_string);
    
    System.out.println("date to be sent formatted as: "+date_string);
    return new JsonPrimitive(date_string);
  }

  private String add_colon_toZformat(String date){
    String date_tail = date.substring(date.length()-2,date.length());
    date = date.substring(0,date.length()-2);
    return date +":"+ date_tail;
  }
}
