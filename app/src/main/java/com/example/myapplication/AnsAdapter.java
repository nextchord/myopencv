package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AnsAdapter extends ArrayAdapter<Result> {
    private int resourceId;
    public AnsAdapter(Context context, int resourceId, List<Result> list){
        super(context, resourceId, list);
        this.resourceId = resourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Result result = getItem(position);
        View view;
        ViewHolder viewHolder = new ViewHolder();
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,null);
            viewHolder.textId = (TextView)view.findViewById(R.id.textViewAnsId);
            viewHolder.textAns = (TextView)view.findViewById(R.id.textAnswer);
            viewHolder.textStandard = (TextView)view.findViewById(R.id.textStandard);
            viewHolder.textJudge = (TextView)view.findViewById(R.id.textJudge);
            view.setTag(viewHolder);
        }
        else{
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.textId.setText(result.getId());
        viewHolder.textAns.setText(result.getAnswer());
        viewHolder.textStandard.setText(result.getStandard());
        viewHolder.textJudge.setText(result.isRight());
        return view;
    }

    class ViewHolder{
        TextView textId;
        TextView textAns;
        TextView textStandard;
        TextView textJudge;
    }
}
