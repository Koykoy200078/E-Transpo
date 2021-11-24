package etranspo.ph.Adapter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import etranspo.ph.R;

import java.util.List;

import etranspo.ph.Entity.History;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    // Variables
    private List<History> historyList;
    private Context context;

    public HistoryAdapter(List<History> historyList) {
        this.historyList = historyList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = null;

        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.context.setText((position + 1) + ". " + historyList.get(position).getContext());
        holder.date.setText(historyList.get(position).getDate());
        holder.copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", historyList.get(position).getContext());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView context, date;
        public ImageView search, copy, share;

        public ViewHolder(View itemView) {
            super(itemView);
            context = itemView.findViewById(R.id.contextTextView);
            date = itemView.findViewById(R.id.dateTextView);
            copy = itemView.findViewById(R.id.copyImageView);
        }
    }

}
