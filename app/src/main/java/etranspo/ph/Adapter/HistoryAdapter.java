package etranspo.ph.Adapter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import etranspo.ph.Entity.History;
import etranspo.ph.R;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    // Variables
    private final List<History> historyList;
    private Context context;

    public HistoryAdapter(List<History> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v;

        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.context.setText("Successfully Paid ₱25 to: " + historyList.get(position).getContext());
        holder.date.setText(historyList.get(position).getDate());
        holder.copy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", historyList.get(position).getContext());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(context, "Logs Copy", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView context, date;
        public ImageView copy;

        public ViewHolder(View itemView) {
            super(itemView);
            context = itemView.findViewById(R.id.contextTextView);
            date = itemView.findViewById(R.id.dateTextView);
            copy = itemView.findViewById(R.id.copyImageView);
        }
    }

}
