package com.example.nhom4android;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ThemActivity extends AppCompatActivity {
    EditText edtid,edtimg,edtdapan,edtdanhlua;
    Button btnthem,btnsua,btnxoa,btnds,btnselect_img;
    ListView lsvds;
    ArrayList<String> mylist;
    ArrayAdapter<String> myadapter;
    SQLiteDatabase mydatabase;
    private static final int PICK_IMAGE_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.them);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        edtid = findViewById(R.id.edtid);
        edtimg = findViewById(R.id.edtdapan);
        edtdapan = findViewById(R.id.edtdapanct);
        edtdanhlua = findViewById(R.id.edtdanhlua);
        btnthem = findViewById(R.id.btnthem);
        btnsua = findViewById(R.id.btnsua);
        btnxoa = findViewById(R.id.btnxoa);
        btnds = findViewById(R.id.btnds);
        btnselect_img = findViewById(R.id.btnuploadimg);
        lsvds = findViewById(R.id.lsvds);

        mydatabase =openOrCreateDatabase("dbduoi_hinh_bat_chu.db",MODE_PRIVATE,null);

        mylist = new ArrayList<>();
        myadapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,mylist);
        lsvds.setAdapter(myadapter);
        btnds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mylist.clear();
                Cursor c =mydatabase.query("tbcauhoi",null,null,null,null,null,"ID DESC");
                c.moveToNext();
                String data = "";
                while (c.isAfterLast() == false){
                    data = c.getString(0) + " - " + c.getString(1) + " - " + c.getString(2) + " - " + c.getString(3);
                    c.moveToNext();
                    mylist.add(data);
                }
                c.close();
                myadapter.notifyDataSetChanged();
            }
        });

        // Xử lý sự kiện khi chọn item trong ListView
        lsvds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                String[] data = selectedItem.split(" - "); // Cắt chuỗi dữ liệu từ item

                if (data.length >= 4) {
                    edtid.setText(data[0].trim());
                    edtimg.setText(data[1].trim());
                    edtdapan.setText(data[2].trim());
                    edtdanhlua.setText(data[3].trim());
                }
            }
        });

        btnthem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tenanh = edtimg.getText().toString();
                String dapanct = edtdapan.getText().toString();
                String danhlua = edtdanhlua.getText().toString();
                int newId = getMaxId() + 1; // Lấy ID lớn nhất và cộng thêm 1
                ContentValues myvalue = new ContentValues();
                myvalue.put("ID", newId); // Thêm ID mới
                myvalue.put("image",tenanh);
                myvalue.put("dapan",dapanct);
                myvalue.put("danhlua",danhlua);
                String msg = "";
                if(mydatabase.insert("tbcauhoi",null,myvalue) == -1){
                    msg = "Thêm thất bại";
                }else {
                    msg = "Thêm thành công";
                    edtimg.setText("");
                    edtdapan.setText("");
                    edtdanhlua.setText("");
                }
                Toast.makeText(ThemActivity.this,msg,Toast.LENGTH_SHORT).show();
            }
        });
        btnsua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = Integer.parseInt(edtid.getText().toString());
                String tenanh = edtimg.getText().toString();
                String dapanct = edtdapan.getText().toString();
                String danhlua = edtdanhlua.getText().toString();
                ContentValues myvalue = new ContentValues();
                myvalue.put("image",tenanh);
                myvalue.put("dapan",dapanct);
                myvalue.put("danhlua",danhlua);
                // Update the record with the specified `msp`
                int n = mydatabase.update("tbcauhoi", myvalue, "id = ?", new String[]{String.valueOf(id)});

                if (n > 0) {
                    Toast.makeText(ThemActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ThemActivity.this, "Không có bản ghi nào được cập nhật", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnxoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = Integer.parseInt(edtid.getText().toString());
                // Lấy tên ảnh từ cơ sở dữ liệu dựa trên ID
                String imageName = getImageNameFromDatabase(id);

                int n = mydatabase.delete("tbcauhoi", "ID = ?", new String[]{String.valueOf(id)});

                String msg = "";
                if (n > 0) {
                    // Nếu xóa bản ghi thành công, xóa ảnh từ bộ nhớ trong
                    if (imageName != null && !imageName.isEmpty()) {
                        deleteImageFromInternalStorage(imageName);
                    }
                    msg = n + " Bản ghi bị xóa";
                } else {
                    msg = "Không có bản ghi nào được xóa";
                }
                Toast.makeText(ThemActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });


        // Chọn ảnh từ Gallery
        btnselect_img.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*"); // Chỉ cho phép chọn ảnh
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
    }
    // Xử lý kết quả trả về sau khi người dùng chọn ảnh từ Gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // Lấy bitmap từ Uri của ảnh đã chọn
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                // Lưu ảnh vào bộ nhớ trong với tên nhập từ EditText
                String imageName = edtimg.getText().toString();
                if (!imageName.isEmpty()) {
                    saveImageToInternalStorage(bitmap, imageName);
                } else {
                    Toast.makeText(this, "Hãy nhập tên ảnh", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Lưu ảnh vào bộ nhớ trong
    private void saveImageToInternalStorage(Bitmap bitmap, String imageName) {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        // Đường dẫn tới thư mục lưu trữ ảnh
        File directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE);
        // Tạo file ảnh với tên đã nhập
        File path = new File(directory, imageName + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            // Nén ảnh dưới định dạng JPEG và lưu vào file
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Toast.makeText(this, "Lưu ảnh thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getImageNameFromDatabase(int id) {
        String imageName = null;
        String[] columns = {"image"};

        Cursor cursor = mydatabase.query("tbcauhoi", columns, "ID = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow("image");
                    imageName = cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        return imageName;
    }



    private void deleteImageFromInternalStorage(String imageName) {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        // Đường dẫn tới thư mục lưu trữ ảnh
        File directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE);
        // Tạo file ảnh với tên đã nhập
        File file = new File(directory, imageName + ".jpg");

        if (file.exists()) {
            if (file.delete()) {
                Toast.makeText(this, "Xóa ảnh thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không thể xóa ảnh", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Ảnh không tồn tại", Toast.LENGTH_SHORT).show();
        }
    }

    private int getMaxId() {
        int maxId = 0;
        Cursor cursor = mydatabase.rawQuery("SELECT MAX(ID) FROM tbcauhoi", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                maxId = cursor.getInt(0);
            }
            cursor.close();
        }
        return maxId;
    }

}