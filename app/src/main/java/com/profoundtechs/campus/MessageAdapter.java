package com.profoundtechs.campus;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.List;

/**
 * Created by HP on 4/23/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    //Firebase
    private DatabaseReference mMessagesDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    //Variables
    private List<Messages> mMessageList;

    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_single_message,parent,false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, final int i){
        //Getting the id of the current user
        mAuth = FirebaseAuth.getInstance();
        final String currentUserId = mAuth.getCurrentUser().getUid();

        //Messages
        final Messages c = mMessageList.get(i);

        //Getting each component of the message
        final String fromUser = c.getFrom();
        final long time = c.getTime();
        final String messageSeen = c.getSeen();
        final String messageType = c.getType();
        if (!messageType.equals("tempo")){
            //This displays the actual messages with the time of the message and the delivery status
            if (messageType.equals("date")){
                //This displays the day of the messages only when the day changes
                final String messageDate= DateFormat.getDateInstance(DateFormat.SHORT).format(time);
                viewHolder.tvSingleMessageDate.setText(messageDate);
                viewHolder.tvSingleMessageSentBody.setVisibility(View.INVISIBLE);
                viewHolder.tvSingleMessageSentTime.setVisibility(View.INVISIBLE);
                viewHolder.tvSingleMessageReceivedBody.setVisibility(View.INVISIBLE);
                viewHolder.tvSingleMessageReceivedTime.setVisibility(View.INVISIBLE);
                viewHolder.tvSingleMessageDate.setVisibility(View.VISIBLE);
            } else {
                if (fromUser.equals(currentUserId)){

                    viewHolder.tvSingleMessageSentBody.setText(c.getMessage());
                    final String messageTime= DateFormat.getTimeInstance(DateFormat.SHORT).format(time);
                    viewHolder.tvSingleMessageSentTime.setText(messageTime);

                    viewHolder.tvSingleMessageSentBody.setVisibility(View.VISIBLE);
                    viewHolder.tvSingleMessageSentTime.setVisibility(View.VISIBLE);
                    viewHolder.tvSingleMessageReceivedBody.setVisibility(View.INVISIBLE);
                    viewHolder.tvSingleMessageReceivedTime.setVisibility(View.INVISIBLE);
                    viewHolder.tvSingleMessageDate.setVisibility(View.INVISIBLE);

                    //Change the message seen status
                    if (messageSeen.equals("true")){
                        viewHolder.ivDeliveryStatus.setImageResource(R.drawable.ic_message_delivered);
                    } else {
                        viewHolder.ivDeliveryStatus.setImageResource(R.drawable.ic_message_unsent);
                    }
                } else {
                    viewHolder.tvSingleMessageReceivedBody.setText(c.getMessage());
                    final String messageTime= DateFormat.getTimeInstance(DateFormat.SHORT).format(time);
                    viewHolder.tvSingleMessageReceivedTime.setText(messageTime);

                    viewHolder.tvSingleMessageSentBody.setVisibility(View.INVISIBLE);
                    viewHolder.tvSingleMessageSentTime.setVisibility(View.INVISIBLE);
                    viewHolder.tvSingleMessageReceivedBody.setVisibility(View.VISIBLE);
                    viewHolder.tvSingleMessageReceivedTime.setVisibility(View.VISIBLE);
                    viewHolder.tvSingleMessageDate.setVisibility(View.INVISIBLE);
                }
            }
        }

    }

    @Override
    public int getItemCount (){
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView tvSingleMessageSentBody;
        public TextView tvSingleMessageSentTime;
        public TextView tvSingleMessageReceivedBody;
        public TextView tvSingleMessageReceivedTime;
        public TextView tvSingleMessageDate;
        public ImageView ivDeliveryStatus;

        public MessageViewHolder(View view){
            super(view);

            tvSingleMessageSentBody = (TextView) view.findViewById(R.id.tvSingleMessageSentBody);
            tvSingleMessageSentTime = (TextView) view.findViewById(R.id.tvSingleMessageSentTime);
            tvSingleMessageReceivedBody = (TextView) view.findViewById(R.id.tvSingleMessageReceivedBody);
            tvSingleMessageReceivedTime = (TextView) view.findViewById(R.id.tvSingleMessageReceivedTime);
            tvSingleMessageDate = (TextView) view.findViewById(R.id.tvSingleMessageDate);
            ivDeliveryStatus = (ImageView)view.findViewById(R.id.ivDeliveryStatus);
        }
    }
}
