package com.example.chatui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatui.Message;
import com.example.chatui.R;
import com.example.chatui.util.MessageInfoConstant;
import com.example.chatui.util.TimeUtil;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    //自己构造的MessageAdapter继承自库RecyclerView.Adapter，用来放MessageAdapter.ViewHolder
    private Context context;
    private List<Message> messageList;
    static class ViewHolder extends RecyclerView.ViewHolder{
        //我自己的ViewHolder继承自RecyclerView.ViewHolder
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMessageText;
        TextView rightMessageText;
        TextView leftMessageTime;
        TextView rightMessageTime;
        ImageView leftMessageImage;
        ImageView rightMessageImage;

        public ViewHolder(View view){
            super(view);
            leftMessageTime=(TextView)view.findViewById(R.id.message_item_receive_time);
            rightMessageTime=(TextView)view.findViewById(R.id.message_item_send_time);
            leftLayout=(LinearLayout)view.findViewById(R.id.message_item_receive);
            rightLayout=(LinearLayout)view.findViewById(R.id.message_item_send);
            leftMessageText=(TextView)view.findViewById(R.id.message_text_left);
            rightMessageText=(TextView)view.findViewById(R.id.message_text_right);
            leftMessageImage=(ImageView)view.findViewById(R.id.message_image_left);
            rightMessageImage=(ImageView)view.findViewById(R.id.message_image_right);
        }
    }

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }
    //我的ViewHolder必须要重写3个方法
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(context==null){
            context=parent.getContext();
        }
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message=messageList.get(position);
//        显示时间的逻辑
//        5min之内的消息之间不显示时间
        if(position!=0){
            Message last_message=messageList.get(position-1);
            TimeUtil timeUtil=new TimeUtil();
            if(timeUtil.getMinute(message.getMessageTime())-timeUtil.getMinute(last_message.getMessageTime())<5){
                holder.leftMessageTime.setVisibility(View.GONE);
                holder.rightMessageTime.setVisibility(View.GONE);
            }
        }

        switch (message.getMessageType()){
            case MessageInfoConstant.Message_Type_Received:
                holder.leftLayout.setVisibility(View.VISIBLE);
                holder.rightLayout.setVisibility(View.GONE);
                switch (message.getMessageContentType()){
                    case MessageInfoConstant.Message_Content_Text:
                        holder.leftMessageImage.setVisibility(View.GONE);
                        holder.leftMessageText.setText(message.getContent());
                        holder.leftMessageTime.setText(message.getMessageTime());
                        break;
                    case MessageInfoConstant.Message_Content_Image:
                        Glide.with(context).load(message.getImageUri()).into(holder.leftMessageImage);
                        holder.leftMessageText.setVisibility(View.GONE);
                        holder.leftMessageTime.setText(message.getMessageTime());
                        break;
                        default:break;
                }
                break;
            case MessageInfoConstant.Message_Type_Send:
                holder.leftLayout.setVisibility(View.GONE);
                holder.rightLayout.setVisibility(View.VISIBLE);
                switch (message.getMessageContentType()){
                    case MessageInfoConstant.Message_Content_Text:
                        holder.rightMessageImage.setVisibility(View.GONE);
                        holder.rightMessageText.setText(message.getContent());
                        holder.rightMessageTime.setText(message.getMessageTime());
                        break;
                    case MessageInfoConstant.Message_Content_Image:
                        if(message.getImageUri()!=null){
                            Glide.with(context).load(message.getImageUri()).into(holder.rightMessageImage);
                        }else if(message.getFilepath()!=null){
                            Glide.with(context).load(message.getFilepath()).into(holder.rightMessageImage);
                        }
                        holder.rightMessageText.setVisibility(View.GONE);
                        holder.rightMessageTime.setText(message.getMessageTime());
                        break;
                        default:break;
                }
                break;
                default:
                    break;
        }

        }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
