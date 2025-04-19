package usc.edu.ph.taskybear;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

public class ShelfActivity extends AppCompatActivity {
    private LinearLayout booksContainer;
    private static final int PDF_PICKER_REQUEST = 101;
    private static final int IMAGE_PICKER_REQUEST = 102;
    private static final int STORAGE_PERMISSION_CODE = 100;

    private Uri selectedPdfUri;
    private Uri selectedImageUri;
    private ImageView bookCoverPreview;
    private ImageView todobtn,homebtn, profilebtn,schedbtn;


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelf);
            todobtn = findViewById(R.id.todobtn);
            homebtn = findViewById(R.id.homebtn);
            profilebtn = findViewById(R.id.profilebtn);
            schedbtn = findViewById(R.id.schedbtn);

            booksContainer = findViewById(R.id.booksContainer);
        CardView uploadCard = (CardView) booksContainer.getChildAt(0);
        LinearLayout uploadFromDeviceLayout = uploadCard.findViewById(R.id.uploadFromDeviceLayout);

        uploadFromDeviceLayout.setOnClickListener(v -> {
            checkStoragePermission();
            showUploadDialog();
        });
            todobtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ShelfActivity.this, ToDoActivity.class);
                    startActivity(intent);
                }
            });
            schedbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ShelfActivity.this, ScheduleActivity.class);
                    startActivity(intent);
                }
            });
            homebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ShelfActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            });
            profilebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ShelfActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }
            });



        }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
        homebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShelfActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
        todobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShelfActivity.this, ToDoActivity.class);
                startActivity(intent);
            }
        });
//        schedbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(ShelfActivity.this, ScheduleActivity.class);
//                startActivity(intent);
//            }
//        });
//        profilebtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(ToDoActivity.this, ProfileActivity.class);
//                startActivity(intent);
//            }
//        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission needed to access files", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showUploadDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_upload_book);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        bookCoverPreview = dialog.findViewById(R.id.bookCoverPreview);
        EditText bookTitleInput = dialog.findViewById(R.id.bookTitleInput);
        Button chooseFileBtn = dialog.findViewById(R.id.chooseFileBtn);
        Button doneBtn = dialog.findViewById(R.id.doneBtn);

        // Pick cover image
        bookCoverPreview.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICKER_REQUEST);
        });

        // Pick PDF file
        chooseFileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            startActivityForResult(intent, PDF_PICKER_REQUEST);
        });

        // Done button logic
        doneBtn.setOnClickListener(v -> {
            String title = bookTitleInput.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a book title", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedPdfUri == null) {
                Toast.makeText(this, "Please select a PDF file", Toast.LENGTH_SHORT).show();
                return;
            }

            addBookToShelf(title, selectedImageUri, selectedPdfUri);
            selectedImageUri = null;
            selectedPdfUri = null;
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            if (requestCode == PDF_PICKER_REQUEST) {
                selectedPdfUri = uri;
                Toast.makeText(this, "PDF selected", Toast.LENGTH_SHORT).show();
            } else if (requestCode == IMAGE_PICKER_REQUEST) {
                selectedImageUri = uri;
                if (bookCoverPreview != null) {
                    Glide.with(this).load(uri).into(bookCoverPreview);
                }
            }
        }
    }

    private void addBookToShelf(String title, Uri imageUri, Uri pdfUri) {
        View bookCardView = LayoutInflater.from(this).inflate(R.layout.item_book_card, booksContainer, false);

        EditText bookTitle = bookCardView.findViewById(R.id.bookTitle);
        ImageView bookCover = bookCardView.findViewById(R.id.bookCover);
        ImageButton editTitleBtn = bookCardView.findViewById(R.id.editTitleBtn);
        ImageButton deleteBookBtn = bookCardView.findViewById(R.id.deleteBookBtn);
        Button openBookBtn = bookCardView.findViewById(R.id.openBookBtn);

        bookTitle.setText(title);

        if (imageUri != null) {
            Glide.with(this).load(imageUri).into(bookCover);
        } else {
            bookCover.setImageResource(R.drawable.upload_icon);
        }

        editTitleBtn.setOnClickListener(v -> {
            if (bookTitle.isEnabled()) {
                bookTitle.setEnabled(false);
                editTitleBtn.setImageResource(R.drawable.edit_icon);
            } else {
                bookTitle.setEnabled(true);
                bookTitle.requestFocus();
                editTitleBtn.setImageResource(R.drawable.check_icon);
            }
        });

        deleteBookBtn.setOnClickListener(v -> booksContainer.removeView(bookCardView));

        openBookBtn.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(pdfUri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No PDF reader installed", Toast.LENGTH_SHORT).show();
            }
        });

        booksContainer.addView(bookCardView, booksContainer.getChildCount() - 1);
    }
}
