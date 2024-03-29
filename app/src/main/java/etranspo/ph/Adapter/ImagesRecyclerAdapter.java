package etranspo.ph.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import etranspo.ph.Entity.ImagesList;
import etranspo.ph.R;

public class ImagesRecyclerAdapter extends RecyclerView.Adapter<ImagesRecyclerAdapter.MyViewHolder>
{
    private final List<ImagesList> imagesLists;
    Context context;

    public ImagesRecyclerAdapter(List<ImagesList> imagesLists, Context context)
    {
        this.imagesLists = imagesLists;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_image_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position)
    {
        Glide.with(context).load(imagesLists.get(position).getImageUrl()).into(holder.imageView);
        holder.imageView.setOnClickListener(v ->
        {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            assert firebaseUser != null;
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("imageUrl", imagesLists.get(position).getImageUrl());
            userReference.updateChildren(hashMap);
        });
    }

    @Override
    public int getItemCount()
    {
        return imagesLists.size();
    }

    static  class MyViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView imageView;
        MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            imageView = itemView.findViewById(R.id.profileImage);

        }
    }
}