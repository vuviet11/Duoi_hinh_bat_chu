package com.example.nhom4android;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

import java.util.ArrayList;

public class XephangActivity extends AppCompatActivity {
    ListView lsvxh;
    TextView txtxephang;
    ArrayList<xephang> mylist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.xephang);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtxephang = findViewById(R.id.txtxephang);
        lsvxh = findViewById(R.id.lsvxephang);
        mylist = new ArrayList<>();

        // Lấy tên người chơi từ một nguồn nào đó, ví dụ từ Intent
        String playerName = getIntent().getStringExtra("player_name");

        // Đọc dữ liệu và lấy thứ hạng của người chơi
        readDataAndRankPlayer(playerName);
    }

    private void readDataAndRankPlayer(String playerName){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("xephang");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mylist.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    xephang xh = snapshot1.getValue(xephang.class);
                    if (xh != null) {
                        mylist.add(xh);
                    }
                }

                // Sắp xếp danh sách theo điểm từ cao đến thấp
                mylist.sort((x1, x2) -> Integer.compare(x2.getDiem(), x1.getDiem()));

                // Cập nhật thứ hạng cho từng mục trong danh sách
                ArrayList<String> rankedList = new ArrayList<>();  // Danh sách có chứa thứ hạng
                for (int i = 0; i < mylist.size(); i++) {
                    xephang xh = mylist.get(i);
                    // Tạo chuỗi với thứ hạng, tên và điểm
                    String rankText = "Rank " + (i + 1) + " - Tên: " + xh.getTen() + " - Điểm: " + xh.getDiem();
                    rankedList.add(rankText);
                }

                // Tìm vị trí của người chơi trong danh sách xếp hạng
                int rank = -1;
                xephang playerXephang = null; // Biến lưu thông tin người chơi
                for (int i = 0; i < mylist.size(); i++) {
                    if (mylist.get(i).getTen().equals(playerName)) {
                        rank = i + 1; // Thứ hạng là chỉ số + 1
                        playerXephang = mylist.get(i); // Lưu thông tin người chơi
                        break;
                    }
                }

                if (rank != -1) {
                    txtxephang.setText("Rank " + rank + ": " + playerXephang.getTen() +", " + playerXephang.getDiem() );
                } else {
                    // Người chơi không có trong danh sách
                    Toast.makeText(XephangActivity.this, "Không tìm thấy người chơi!", Toast.LENGTH_SHORT).show();
                }

                // Cập nhật ListView với danh sách đã có thứ hạng
                ArrayAdapter<String> rankedAdapter = new ArrayAdapter<>(XephangActivity.this, android.R.layout.simple_list_item_1, rankedList);
                lsvxh.setAdapter(rankedAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}