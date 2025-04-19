package usc.edu.ph.taskybear;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;



public class ToDoActivity extends AppCompatActivity {
    // Class variables (declare here)
    ImageButton openDialogButton;
    LinearLayout taskListContainer;
    LayoutInflater inflater;
    RecyclerView recyclerView;
    TaskAdapter adapter;
    ArrayList<Task> taskList;
    Button[] buttons;
    Button selectedButton;
    private ImageView homebtn, shelfbtn,profilebtn, schedbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        homebtn = findViewById(R.id.homebtn);
        shelfbtn = findViewById(R.id.shelfbtn);
        profilebtn = findViewById(R.id.profilebtn);
        schedbtn = findViewById(R.id.schedbtn);

        openDialogButton = findViewById(R.id.addTaskButton);
        taskListContainer = findViewById(R.id.tasklistcontainer);
        recyclerView = findViewById(R.id.taskRecyclerView);

        homebtn.setOnClickListener(v -> {
            startActivity(new Intent(ToDoActivity.this, HomeActivity.class));
        });

        shelfbtn.setOnClickListener(v -> {
            startActivity(new Intent(ToDoActivity.this, ShelfActivity.class));
        });
        profilebtn.setOnClickListener(v -> {
            startActivity(new Intent(ToDoActivity.this, ProfileActivity.class));
        });
        schedbtn.setOnClickListener(v -> {
            startActivity(new Intent(ToDoActivity.this, ScheduleActivity.class));
        });



        inflater = LayoutInflater.from(this);
        openDialogButton = findViewById(R.id.addTaskButton);
        taskListContainer = findViewById(R.id.tasklistcontainer);
        ImageView homebtn = findViewById(R.id.homebtn);
        ImageView shelfbtn = findViewById(R.id.shelfbtn);
        shelfbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ToDoActivity.this, ShelfActivity.class);
                startActivity(intent);
            }
        });


        recyclerView = findViewById(R.id.taskRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        buttons = new Button[] {
                findViewById(R.id.completetodolist),
                findViewById(R.id.reviewtodolist),
                findViewById(R.id.progresstodolist),
                findViewById(R.id.onholdtodolist)
        };

        for (Button btn : buttons) {
            btn.setOnClickListener(v -> {
                for (Button b : buttons) {
                    b.setSelected(false);
                }

                v.setSelected(true);
                selectedButton = (Button) v;
            });
        }
        adapter = new TaskAdapter(this, taskList, new TaskAdapter.TaskActionListener() {
            @Override
            public void onEdit(Task task) {
                int position = taskList.indexOf(task);
                if (position != -1) {
                    showEditTaskDialog(task, position);
                }
            }


            @Override
            public void onDelete(Task task) {
                // delete button
                taskList.remove(task);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCategoryChange(Task task, String newCategory) {
                //change category code here
                task.setCategory(newCategory);
                adapter.notifyDataSetChanged();
            }
        });
        recyclerView.setAdapter(adapter);

        openDialogButton.setOnClickListener(v -> showAddTaskDialog());
    }

    private void showAddTaskDialog() {
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText taskNameInput = dialogView.findViewById(R.id.taskNameInput);
        Spinner resourceSpinner = dialogView.findViewById(R.id.resourceSpinner);
        EditText taskdetails = dialogView.findViewById(R.id.taskDetails);
        Button dueDateButton = dialogView.findViewById(R.id.dueDateButton);
        Button backButton = dialogView.findViewById(R.id.backButton);
        Button doneButton = dialogView.findViewById(R.id.doneButton);

       //Make this dynamic based on the shelf
        ArrayList<String> resources = new ArrayList<>();
        resources.add("None");
        resources.add("Resoource 1");
        resources.add("Resource 2");
        ArrayAdapter<String> resourceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, resources);
        resourceSpinner.setAdapter(resourceAdapter);


        final Calendar calendar = Calendar.getInstance();
        final String[] selectedDate = {""};
        dueDateButton.setOnClickListener(view -> {
            DatePickerDialog datePicker = new DatePickerDialog(ToDoActivity.this,
                    (DatePicker view1, int year, int month, int dayOfMonth) -> {
                        selectedDate[0] = dayOfMonth + " " + getMonthName(month) + " " + year;
                        dueDateButton.setText(selectedDate[0]);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        backButton.setOnClickListener(view -> dialog.dismiss());

        doneButton.setOnClickListener(view -> {
            String taskName = taskNameInput.getText().toString();
            String taskDetailText = taskdetails.getText().toString();
            String date = selectedDate[0];
            String selectedResource = resourceSpinner.getSelectedItem().toString();


            Task newTask = new Task(taskName, taskDetailText, date, selectedResource, "Progress");
            taskList.add(newTask);


            adapter.notifyItemInserted(taskList.size() - 1);

            dialog.dismiss();
        });
    }

    private void showEditTaskDialog(Task task, int position) {
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText taskNameInput = dialogView.findViewById(R.id.taskNameInput);
        Spinner resourceSpinner = dialogView.findViewById(R.id.resourceSpinner);
        EditText taskDetailsInput = dialogView.findViewById(R.id.taskDetails);
        Button dueDateButton = dialogView.findViewById(R.id.dueDateButton);
        Button backButton = dialogView.findViewById(R.id.backButton);
        Button doneButton = dialogView.findViewById(R.id.doneButton);

        taskNameInput.setText(task.getTitle());
        taskDetailsInput.setText(task.getDetails());
        dueDateButton.setText(task.getDate());

        ArrayList<String> resources = new ArrayList<>();
        resources.add("None");
        resources.add("Resoource 1");
        resources.add("Resource 2");
        ArrayAdapter<String> resourceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, resources);
        resourceSpinner.setAdapter(resourceAdapter);

        int resourceIndex = resources.indexOf(task.getResource());
        if (resourceIndex >= 0) {
            resourceSpinner.setSelection(resourceIndex);
        }

        final Calendar calendar = Calendar.getInstance();
        final String[] selectedDate = {task.getDate()};

        dueDateButton.setOnClickListener(view -> {
            DatePickerDialog datePicker = new DatePickerDialog(ToDoActivity.this,
                    (DatePicker view1, int year, int month, int dayOfMonth) -> {
                        selectedDate[0] = dayOfMonth + " " + getMonthName(month) + " " + year;
                        dueDateButton.setText(selectedDate[0]);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        backButton.setOnClickListener(view -> dialog.dismiss());

        doneButton.setOnClickListener(view -> {
            task.setTitle(taskNameInput.getText().toString());
            task.setDetails(taskDetailsInput.getText().toString());
            task.setDate(selectedDate[0]);
            task.setResource(resourceSpinner.getSelectedItem().toString());

            adapter.notifyItemChanged(position);
            dialog.dismiss();
        });
    }



    private String getMonthName(int month) {
        return new java.text.DateFormatSymbols().getMonths()[month];
    }
}