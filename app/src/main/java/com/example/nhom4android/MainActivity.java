package com.example.nhom4android;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    int tieptuc = 0;
    ImageButton btnmusic;
    Boolean flag = true;
    String correctPassword = "n4android"; // Mật khẩu đúng
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Kiểm tra xem đã có tên người chơi chưa
        SharedPreferences preferences = getSharedPreferences("player_data", MODE_PRIVATE);
        String playerName = preferences.getString("player_name", null);

        if (playerName == null) {
            // Nếu chưa có tên, yêu cầu nhập tên
            promptForName();
        }


        // Khởi động dịch vụ phát nhạc ngay khi MainActivity được tạo
        Intent musicIntent = new Intent(MainActivity.this, MyService.class);
        startService(musicIntent);
        int savedIndex = getSharedPreferences("game_progress", MODE_PRIVATE)
                .getInt("current_index", -1);
        int score = getSharedPreferences("game_progress", MODE_PRIVATE)
                .getInt("score", -1);
        Button btnbatdau = findViewById(R.id.btnbatdau);
        btnbatdau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,DuoihinhbatchuActivity.class);
                intent.putExtra("player_name", playerName); // Truyền tên qua Intent
                intent.putExtra("current_index", "");
                intent.putExtra("score", "");
                intent.putExtra("flag", flag);
                startActivity(intent);
            }
        });
        Button btntieptuc = findViewById(R.id.btntieptuc);

        if (savedIndex != -1) {
            btntieptuc.setVisibility(View.VISIBLE);
            btntieptuc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, DuoihinhbatchuActivity.class);
                    intent.putExtra("player_name", playerName); // Truyền tên qua Intent
                    intent.putExtra("current_index", savedIndex);
                    intent.putExtra("score", score);
                    intent.putExtra("flag", flag);
                    startActivity(intent);
                }
            });
        } else {
            btntieptuc.setVisibility(View.GONE);
        }

        btnmusic = findViewById(R.id.btnmusic);
        btnmusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag) {
                    Intent musicIntent = new Intent(MainActivity.this, MyService.class);
                    stopService(musicIntent);
                    btnmusic.setImageResource(R.drawable.musicoff);
                } else {
                    Intent musicIntent = new Intent(MainActivity.this, MyService.class);
                    startService(musicIntent);
                    btnmusic.setImageResource(R.drawable.musicon);
                }
                flag = !flag;
            }
        });
        Button btnadmin = findViewById(R.id.btnadmin);
        btnadmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPasswordDialog();
            }
        });

        Button btnxh = findViewById(R.id.btnxephang);
        btnxh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,XephangActivity.class);
                intent.putExtra("player_name", playerName); // Truyền tên qua Intent
                startActivity(intent);
            }
        });
    }

    private void showPasswordDialog() {
        // Tạo một hộp thoại với ô nhập mật khẩu
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập mật khẩu quản trị");

        // Tạo EditText cho việc nhập mật khẩu
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Nút "OK" kiểm tra mật khẩu
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredPassword = input.getText().toString();
                if (enteredPassword.equals(correctPassword)) {
                    // Mật khẩu đúng, chuyển sang Activity khác
                    Intent intent = new Intent(MainActivity.this, ThemActivity.class);
                    startActivity(intent);
                } else {
                    // Mật khẩu sai, hiển thị thông báo lỗi
                    Toast.makeText(MainActivity.this, "Mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Nút "Hủy" để đóng hộp thoại
        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void promptForName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập tên của bạn");

        // Tạo một EditText để nhập tên
        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String playerName = input.getText().toString().trim();
            if (!playerName.isEmpty()) {
                checkNameInFirebaseRealtimeDatabase(playerName);
            } else {
                // Nếu người chơi không nhập tên, yêu cầu nhập lại
                Toast.makeText(this, "Vui lòng nhập tên hợp lệ!", Toast.LENGTH_SHORT).show();
                promptForName(); // Hiển thị lại hộp thoại nếu tên không hợp lệ
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void checkNameInFirebaseRealtimeDatabase(String playerName){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        databaseReference.child("xephang/ten").equalTo(playerName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Nếu tên đã tồn tại, thông báo cho người dùng
                    Toast.makeText(MainActivity.this, "Tên này đã tồn tại. Vui lòng chọn tên khác!", Toast.LENGTH_SHORT).show();
                    promptForName(); // Hiển thị lại hộp thoại nhập tên
                } else {
                    // Tên chưa tồn tại, lưu vào Firebase và SharedPreferences
                    savePlayerNameToFirebase(playerName);
                    savePlayerNameToSharedPreferences(playerName);
                    Toast.makeText(MainActivity.this, "Tên đã được lưu thành công!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    // Hàm lưu tên vào SharedPreferences
    private void savePlayerNameToSharedPreferences(String name) {
        SharedPreferences preferences = getSharedPreferences("player_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("player_name", name);
        editor.apply();
    }
    // Hàm lưu tên và điểm vào Firebase
    private void savePlayerNameToFirebase(String playerName) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("xephang");

        // Sử dụng tên người chơi làm khóa chính
        DatabaseReference playerRef = databaseReference.child(playerName);

        // Lưu tên người chơi và điểm (mặc định là 0) vào Firebase
        playerRef.setValue(new xephang(playerName, 0));
    }
}