package com.example.chat_app.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chat_app.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends ArrayAdapter<String>
{
    private Context context;
    private List<String> strings; //message
    private List<String> strings1; //messagetype
    private List<String> userList; //userList
    private List<String> allTimeStamps=new ArrayList<>();


    public ChatAdapter(Context context, List<String> strings,List<String> strings1,List<String> userList)
    {
        super(context, R.layout.activity_chat,strings);
        this.context = context;

        this.strings = new ArrayList<String>();
        this.strings = strings;

        this.strings1 = new ArrayList<String>();
        this.strings1 = strings1;

        this.userList = new ArrayList<String>();
        this.userList = userList;
    }

    @Override
    public View getView(int position,View convertView,ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        if (strings.size()>position) {
            if (FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(userList.get(position))){
                View rowView = inflater.inflate(R.layout.chat_message_me, parent, false);
                TextView your_first_text_view = (TextView) rowView.findViewById(R.id.text_gchat_message_me);
                TextView your_second_text_view = (TextView) rowView.findViewById(R.id.text_gchat_timestamp_me);
                your_first_text_view.setText(strings.get(position));
                your_second_text_view.setText(strings1.get(position));
                allTimeStamps.add(strings1.get(position));
                if (position!=0 && strings1.get(position).equals(strings1.get(position-1))){
                    position++;
                    rowView.setVisibility(View.GONE);
                }
                return rowView;
            }
            else{
                View rowView = inflater.inflate(R.layout.chat_message_other, parent, false);
                TextView your_first_text_view = (TextView) rowView.findViewById(R.id.text_gchat_message_other);
                TextView your_second_text_view = (TextView) rowView.findViewById(R.id.text_gchat_timestamp_other);
                your_first_text_view.setText(strings.get(position));
                your_second_text_view.setText(strings1.get(position));
                allTimeStamps.add(strings1.get(position));
                if (position!=0 && strings1.get(position).equals(strings1.get(position-1))){
                    rowView.setVisibility(View.GONE);
                }
                return rowView;
            }

        }

        return null;

    }
}