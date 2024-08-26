package com.example.tennis_padel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {

    private List<User> teacherList;
    private OnTeacherClickListener onTeacherClickListener;
    private Context context;

    public TeacherAdapter(List<User> teacherList, OnTeacherClickListener onTeacherClickListener, Context context) {
        this.teacherList = teacherList;
        this.onTeacherClickListener = onTeacherClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        User teacher = teacherList.get(position);

        holder.teacherName.setText(teacher.getName());
        holder.teacherLastName.setText(teacher.getLastName());

        Glide.with(context)
                .load(teacher.getProfilePicture())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    public interface OnTeacherClickListener {
        void onTeacherClick(User teacher);
    }

    class TeacherViewHolder extends RecyclerView.ViewHolder {

        private MaterialTextView teacherName, teacherLastName;
        private ImageView imageView;

        public TeacherViewHolder(@NonNull View itemView) {
            super(itemView);
            teacherName = itemView.findViewById(R.id.teacher_name);
            teacherLastName = itemView.findViewById(R.id.teacher_lastname);
            imageView = itemView.findViewById(R.id.imageSearch);

            itemView.setOnClickListener(v -> {
                if (onTeacherClickListener != null) {
                    onTeacherClickListener.onTeacherClick(teacherList.get(getAdapterPosition()));
                }
            });
        }
    }
}
