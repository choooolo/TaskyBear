package usc.edu.ph.taskybear;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.text.SimpleDateFormat;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private static final int EDIT_TASK_REQUEST = 1001;  // Added locally

    private Context context;
    private List<Task> taskList;
    private TaskActionListener listener;

    public interface TaskActionListener {
        void onEdit(Task task);
        void onDelete(Task task);
        void onCategoryChange(Task task, String newCategory);
    }

    public TaskAdapter(Context context, List<Task> taskList, TaskActionListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
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
        holder.taskDate.setText(formatDateForDisplay(task.getDate()));
        holder.taskResource.setText(task.getResource());
        holder.taskType.setText(task.getType());

        holder.buttonViewTask.setOnClickListener(v -> {
            Intent intent = new Intent(context, TaskDetails.class);
            intent.putExtra("task", task);
            context.startActivity(intent);
        });

        holder.taskMenu.setOnClickListener(v -> showTaskMenu(v, task));
    }

    private String formatDateForDisplay(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
            return outputFormat.format(inputFormat.parse(date));
        } catch (Exception e) {
            e.printStackTrace();
            return date;
        }
    }

    private void showTaskMenu(View view, Task task) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.task_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_edit) {
                onEdit(task);
                return true;
            } else if (itemId == R.id.menu_delete) {
                listener.onDelete(task);
                return true;
            } else if (itemId == R.id.mark_complete) {
                listener.onCategoryChange(task, "Complete");
                return true;
            } else if (itemId == R.id.mark_review) {
                listener.onCategoryChange(task, "Review");
                return true;
            } else if (itemId == R.id.mark_progress) {
                listener.onCategoryChange(task, "Progress");
                return true;
            } else if (itemId == R.id.mark_on_hold) {
                listener.onCategoryChange(task, "On Hold");
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    private void onEdit(Task task) {
        listener.onEdit(task);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDetails, taskDate, taskResource, taskType;
        ImageView taskMenu;
        Button buttonViewTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDetails = itemView.findViewById(R.id.taskDetails);
            taskDate = itemView.findViewById(R.id.taskDate);
            taskResource = itemView.findViewById(R.id.taskResource);
            taskType = itemView.findViewById(R.id.taskType);
            taskMenu = itemView.findViewById(R.id.taskMenu);
            buttonViewTask = itemView.findViewById(R.id.buttonViewTask);
        }
    }
}