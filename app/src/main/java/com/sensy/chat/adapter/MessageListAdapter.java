package com.sensy.chat.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sensy.chat.R;
import com.sensy.chat.bean.MessageBean;

import java.util.ArrayList;

import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.PhotoPreview;

/**
 * Created by Sensy on 2018/1/4.
 */

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder>{
    private ArrayList<MessageBean> messageList;

    private static final int IS_MESSAGE = 1;//标识：文本消息
    private static final int IS_PICTURE = 2;//标识：图片
    private static final int IS_TXT = 3;//标识：文本文件
    private static final int LEFT = 1;
    private static final int RIGHT = 2;

    public void setData(ArrayList<MessageBean> messageList) {
        this.messageList = messageList;
        notifyDataSetChanged();//内容改变时，刷新
    }

    @Override
    public int getItemViewType(int position) {//判断消息显示位置
        return messageList.get(position).getLocate();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        int messageType = getMessageType(position);
        switch (itemViewType){
            case LEFT:
                holder.leftLayout.setVisibility(View.VISIBLE);
                holder.rightLayout.setVisibility(View.GONE);
                switch (messageType) {
                case IS_MESSAGE:
                case IS_TXT:
                    holder.message1.setText(messageList.get(position).getData());
                    break;
                case IS_PICTURE:
                    String html = "<img src='" + messageList.get(position).getData() + "'/>";
                    CharSequence result = Html.fromHtml(html, new Html.ImageGetter() {
                        @Override
                        public Drawable getDrawable(String s) {
                            //获取资源ID
                            int resId=Integer.parseInt(s);
                            //装载图像资源
                            Drawable drawable= getDrawable(s);
                            //设置图像按原始大小显示
                            drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
                            return drawable;
                        }
                    }, null);
                    holder.message1.setText(result);

                }

                holder.name1.setText(messageList.get(position).getName());
                holder.time1.setText(messageList.get(position).getTime());
                break;
            case RIGHT:
                holder.rightLayout.setVisibility(View.VISIBLE);
                holder.leftLayout.setVisibility(View.GONE);
                switch (messageType) {
                    case IS_MESSAGE:
                    case IS_TXT:
                        holder.message2.setText(messageList.get(position).getData());
                        break;

                }
                holder.name2.setText(messageList.get(position).getName());
                holder.time2.setText(messageList.get(position).getTime());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftLayout, rightLayout;

        private TextView message1;
        private TextView name1,time1;
        private TextView message2;
        private TextView name2,time2;

        public ViewHolder(View view) {
            super(view);
            leftLayout = (LinearLayout) view.findViewById(R.id.left_layout);
            rightLayout = (LinearLayout) view.findViewById(R.id.right_layout);
            message1 = (TextView) itemView.findViewById(R.id.tv_message1);
            name1 = (TextView) itemView.findViewById(R.id.tv_name1);
            time1 = (TextView) itemView.findViewById(R.id.tv_time1);
            message2 = (TextView) itemView.findViewById(R.id.tv_message2);
            name2 = (TextView) itemView.findViewById(R.id.tv_name2);
            time2 = (TextView) itemView.findViewById(R.id.tv_time2);
        }
    }

    private int getMessageType(int position) {
        return messageList.get(position).getMessageType();
    }

}
