package com.example.chat_app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chat_app.R;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<String>
{
    private Context context;
    private List<String> strings;
    private List<String> strings1;


    public CustomAdapter(Context context, List<String> strings,List<String> strings1)
    {
        super(context, R.layout.activity_home,strings);
        this.context = context;

        this.strings = new ArrayList<String>();
        this.strings = strings;

        this.strings1 = new ArrayList<String>();
        this.strings1 = strings1;
    }

    @Override
    public View getView(int position,View convertView,ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.item_chat_list, parent, false);


        TextView your_first_text_view = (TextView) rowView.findViewById(R.id.userName);
        TextView your_second_text_view = (TextView) rowView.findViewById(R.id.surname);

        ImageView is_online_dot = (ImageView) rowView.findViewById(R.id.isOnline);
        if (strings.size()>position) {
            your_first_text_view.setText(strings.get(position));
            if (strings1.get(position).equals("false"))
                your_second_text_view.setText("Çevrimdışı"); //Instead of the same value use position + 1, or something appropriate
            else{
                your_second_text_view.setText("Çevrimiçi");
                is_online_dot.setVisibility(View.VISIBLE);
            }
        }

        return rowView;
    }
}