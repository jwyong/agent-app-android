package io.github.rockerhieu.emojicon;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.github.rockerhieu.emojicon.EmojiconGridView.OnEmojiconClickedListener;
import io.github.rockerhieu.emojicon.emoji.Emojicon;

class EmojiAdapter extends ArrayAdapter<Emojicon> {

    OnEmojiconClickedListener emojiClickListener;
    public EmojiAdapter(Context context, List<Emojicon> data) {
        super(context, R.layout.emojicon_item, data);
    }

    public EmojiAdapter(Context context, Emojicon[] data) {
        super(context, R.layout.emojicon_item, data);
    }

    public void setEmojiClickListener(OnEmojiconClickedListener listener){
    	this.emojiClickListener = listener;
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

//        View view = convertView;
//
//        if (view == null) {
//            view = View.inflate(getContext(), R.layout.emojicon_item, null);
//            ViewHolder holder = new ViewHolder();
//            view.setTag(new ViewHolder(view, this.mUseSystemDefault));
//        }
//
//        if (null != getItem(position)) {
//            Emojicon emoji = this.getItem(position);
//            ViewHolder holder = (ViewHolder) view.getTag();
//            holder.icon.setText(emoji.getEmoji());
//        }
//
//        return view;

        View v = convertView;
        if (v == null) {
            v = View.inflate(getContext(), R.layout.emojicon_item, null);
            ViewHolder holder = new ViewHolder();
            holder.icon = v.findViewById(R.id.emojicon_icon);
            v.setTag(holder);
        }
        Emojicon emoji = getItem(position);
        ViewHolder holder = (ViewHolder) v.getTag();
        holder.icon.setText(emoji.getEmoji());
        holder.icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				emojiClickListener.onEmojiconClicked(getItem(position));
			}
		});
        return v;
    }

    class ViewHolder {

//        EmojiconTextView icon;
//
//        public ViewHolder(View view, Boolean useSystemDefault) {
//            this.icon = (EmojiconTextView) view.findViewById(R.id.emojicon_icon);
//            this.icon.setUseSystemDefault(useSystemDefault);
//        }

        TextView icon;
    }
}