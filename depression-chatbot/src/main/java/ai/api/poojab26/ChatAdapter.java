package ai.api.poojab26;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pblead26 on 24-Jun-17.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private TextView chatText;
    private List<ChatMessage> msgList;
    private Context context;

    public class LeftViewHolder extends RecyclerView.ViewHolder {
        public TextView message;

        public LeftViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.msgr);

        }
    }
    public class RightViewHolder extends RecyclerView.ViewHolder {
        public TextView message;

        public RightViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.msgr);

        }
    }


    public ChatAdapter(List<ChatMessage> msgList) {
        this.msgList = msgList;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case 0: View leftItemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.left, parent, false);
                 return new LeftViewHolder(leftItemView);
            case 2: View rightItemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right, parent, false);
                return new RightViewHolder(rightItemView);
            default:return null;

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = msgList.get(position);

        switch (holder.getItemViewType()) {
            case 0:
                LeftViewHolder leftViewHolder = (LeftViewHolder) holder;
                leftViewHolder.message.setText(chatMessage.getMessage());
                break;
            case 2:
                RightViewHolder rightViewHolder = (RightViewHolder) holder;
                rightViewHolder.message.setText(chatMessage.getMessage());
                break;
        }

    }

    public ChatMessage getItem(int position) {
        return msgList.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        ChatMessage chatMessage = getItem(position);
        Log.d("TAG", String.valueOf(chatMessage.rightSide));
        if(chatMessage.rightSide){
            return 2;
        }else
            return 0;

    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }
}
