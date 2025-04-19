package usc.edu.ph.taskybear;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> taskList;

    public interface TaskActionListener {
        void onEdit(Task task);
        void onDelete(Task task);
        void onCategoryChange(Task task, String newCategory);
    }

    private TaskActionListener listener;

    public TaskAdapter(Context context, List<Task> taskList, TaskActionListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDetails, taskDate, taskResource;
        ImageView taskMenu;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDetails = itemView.findViewById(R.id.taskDetails);
            taskDate = itemView.findViewById(R.id.taskDate);
            taskResource = itemView.findViewById(R.id.taskResource);
            taskMenu = itemView.findViewById(R.id.taskMenu);
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_item_layout, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.taskTitle.setText(task.getTitle());
        holder.taskDetails.setText(task.getDetails());
        holder.taskDate.setText(task.getDate());
        holder.taskResource.setText(task.getResource());

        holder.taskMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.taskMenu);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.task_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_edit) {
                    listener.onEdit(task);
                    return true;
                } else if (item.getItemId() == R.id.menu_delete) {
                    listener.onDelete(task);
                    return true;
                } else if (item.getItemId() == R.id.mark_complete) {
                    listener.onCategoryChange(task, "Complete");
                    return true;
                } else if (item.getItemId() == R.id.mark_review) {
                    listener.onCategoryChange(task, "Review");
                    return true;
                } else if (item.getItemId() == R.id.mark_progress) {
                    listener.onCategoryChange(task, "Progress");
                    return true;
                } else if (item.getItemId() == R.id.mark_on_hold) {
                    listener.onCategoryChange(task, "On Hold");
                    return true;
                } else {
                    return false;
                }
            });



            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}