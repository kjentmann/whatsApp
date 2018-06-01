/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.upc.whatsapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import edu.upc.whatsapp.R;
import entity.UserInfo;

/**
 *
 * @author upcnet
 */
public class MyAdapter_users extends BaseAdapter {

    Context context;
    public List<UserInfo> users;
    private List<Integer> newMessages;
    public MyAdapter_users(Context context, List<UserInfo> users) {
      this.context = context;
      this.users = users;
      this.newMessages=newMessages;


    }

    public int getCount() {
      return users.size();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = LayoutInflater.from(context).inflate(R.layout.row_twotextviews, parent, false);

      }
        UserInfo user= (UserInfo)getItem(position);
        TextView firstName = (TextView) ((LinearLayout) convertView).findViewById(R.id.row_twotextviews_name);
        firstName.setText(user.getName());
        TextView latsName = (TextView) ((LinearLayout) convertView).findViewById(R.id.row_twotextviews_surname);
        latsName.setText(user.getSurname());
      //...

        /*Future
        if (newMessages.contains(user.getId())){
            firstName.setTextColor(Color.rgb(255,102,0));
            firstName.setText("! " + user.getName());
        }*/

      return convertView;
    }

    public Object getItem(int arg0) {
      return users.get(arg0);
    }

    public long getItemId(int arg0) {
      return users.get(arg0).getId();
    }
  }
