package com.aokiji.partner.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aokiji.partner.R;
import com.aokiji.partner.listener.OnElementClickListener;
import com.aokiji.partner.models.chat.Message;
import com.aokiji.partner.widget.GlideCircleTransform;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by zhangdonghai on 2018/7/31.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> mList;
    private Context mContext;

    private OnElementClickListener onElementClickListener;

    public void setOnElementClickListener(OnElementClickListener onElementClickListener) {
        this.onElementClickListener = onElementClickListener;
    }


    public MessageAdapter(List<Message> list, Context context) {
        this.mList = list;
        this.mContext = context;
    }


    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_message, parent, false));
    }


    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = mList.get(position);
        if (message.isFromFriend()) {
            holder.friend.setVisibility(View.VISIBLE);
            holder.self.setVisibility(View.GONE);
            Glide.with(mContext)
                    .load(message.getHead())
                    .crossFade()
                    .centerCrop()
                    .error(R.drawable.ic_default_head)
                    .placeholder(R.drawable.ic_default_head)
                    .transform(new GlideCircleTransform(mContext))
                    .into(holder.ivFriendHead);
            holder.tvFriendMessage.setText(message.getMessage());
        } else {
            holder.friend.setVisibility(View.GONE);
            holder.self.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load(message.getHead())
                    .crossFade()
                    .centerCrop()
                    .error(R.drawable.ic_default_head)
                    .placeholder(R.drawable.ic_default_head)
                    .transform(new GlideCircleTransform(mContext))
                    .into(holder.ivSelfHead);
            holder.tvSelfMessage.setText(message.getMessage());
        }

        if (onElementClickListener != null) {
            holder.ivSpeak.setOnClickListener(v -> onElementClickListener.onElementClick(holder.ivSpeak, holder.getLayoutPosition()));
        }
    }


    @Override public int getItemCount() {
        return mList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout friend, self;
        ImageView ivFriendHead, ivSpeak, ivSelfHead;
        TextView tvFriendMessage, tvSelfMessage;

        public ViewHolder(View itemView) {
            super(itemView);
            friend = itemView.findViewById(R.id.ll_friend);
            self = itemView.findViewById(R.id.ll_self);
            ivFriendHead = itemView.findViewById(R.id.iv_friend_head);
            ivSpeak = itemView.findViewById(R.id.iv_speak);
            ivSelfHead = itemView.findViewById(R.id.iv_self_head);
            tvFriendMessage = itemView.findViewById(R.id.tv_friend_msg);
            tvSelfMessage = itemView.findViewById(R.id.tv_self_msg);
        }
    }

}
