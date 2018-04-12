package com.android.musicplayertemplate;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by rjn on 11/14/2016.
 */

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onTemClick(Songs item, int position);
    }

    private List<Songs> songList;
    private OnItemClickListener listener;
    private Context context;


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView songTitle;
        TextView artistName;
        ImageView thumbnail;

        public ViewHolder(View itemView) {
            super(itemView);
            artistName = (TextView) itemView.findViewById(R.id.tv_artistname);
            songTitle = (TextView) itemView.findViewById(R.id.tv_songsName);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
        }


    }


    public DataAdapter(List<Songs> songList, Context context, OnItemClickListener listener) {
        this.songList = songList;
        this.listener = listener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_items, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Songs songs = songList.get(position);
        holder.songTitle.setText(songs.getmSongTitle());
        holder.artistName.setText(songs.getmSongArtist());
        if (songs.getAlbumArt() != null) {
            holder.thumbnail.setImageBitmap(songs.getAlbumArt());

        } else {
            holder.thumbnail.setImageResource(R.drawable.ic_action_music);

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onTemClick(songs, position);
                System.out.println("psogition value:: "+holder.getAdapterPosition()+"position:: "+position);
            }
        });
        //  Picasso.with(context).load(helper.get(position).getSong_Image_Url()).resize(60, 60).into(holder.imageViewSong);

    }

    @Override
    public int getItemCount() {
        return songList.size();
    }


}
