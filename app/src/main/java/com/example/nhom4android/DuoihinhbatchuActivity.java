package com.example.nhom4android;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DuoihinhbatchuActivity extends AppCompatActivity {

    private static final String DATABASE_NAME = "dbduoi_hinh_bat_chu.db";
    private static final String DB_PATH_SUFFIX = "/databases/";
    private TextView textscore;
    private SQLiteDatabase database;
    private ImageButton btnmusic,btnhintrd,btnhinttc,btnhintall;
    private ImageView imgcauhoi;
    private LinearLayout buttonContainer; // Container for deceive buttons
    private LinearLayout answerButtonContainer; // Container for answer buttons
    private Cursor currentCursor;
    private List<Button> answerButtons = new ArrayList<>();
    private String correctAnswer;
    private String answerdetail;
    private String deceive;
    private int currentIndex = 1; // To keep track of the current question index
    private int score = 0; // To keep track of the current question index
    private boolean hint = false;
    private boolean flag;
    private String playerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.duoihinhbatchu);

        imgcauhoi = findViewById(R.id.imgcauhoi);
        buttonContainer = findViewById(R.id.buttonContainer);
        answerButtonContainer = findViewById(R.id.answerButtonContainer);
        textscore = findViewById(R.id.txtscore);
        btnmusic = findViewById(R.id.btnmusic);
        btnhintrd = findViewById(R.id.btnhintrd);
        btnhinttc = findViewById(R.id.btnhinttc);
        btnhintall = findViewById(R.id.btnhintall);

        saveDrawablesToInternalStorage();

        Intent intent = getIntent();
        playerName = getIntent().getStringExtra("player_name");
        currentIndex = intent.getIntExtra("current_index", 1); // Mặc định là 0 nếu không có giá trị nào được lưu
        score = intent.getIntExtra("score",100000); // Mặc định là 0 nếu không có giá trị nào được lưu
        flag = intent.getBooleanExtra("flag",false);
        // Cập nhật hình ảnh dựa trên trạng thái nhạc
        btnmusic.setImageResource(flag ? R.drawable.musicon : R.drawable.musicoff);
        textscore.setText("Điểm: " + score);

        resetDatabase();
        // Mở cơ sở dữ liệu
        database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        saveProgress(currentIndex,score);

        // Lấy dữ liệu đầu tiên và hiển thị
        loadNextQuestion();

        // Thiết lập sự kiện nhấn cho nút btnmusic
        btnmusic.setOnClickListener(v -> music());
        // Thiết lập sự kiện khi ấn nút hint random
        btnhintrd.setOnClickListener(v -> {
            // Hiển thị thông báo
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Gợi Ý!");
            builder.setMessage("Bạn có muốn sử dụng 15 điểm để mở 1 ký tự ngẫu nhiên không!.");

            // Nút "OK" để qua câu hỏi tiếp theo
            builder.setPositiveButton("OK", (dialog, which) -> {
                RandomHint();
            });
            // Nút "Hủy" để đóng hộp thoại
            builder.setNegativeButton("Hủy", (dialog, which) -> {
                dialog.cancel();
            });
            builder.show();

        });
        // Thiết lập sự kiện khi ấn nút hint tự chọn
        btnhinttc.setOnClickListener(v -> {
            if (hint) {
                // Nếu chế độ gợi ý đang bật, tắt chế độ gợi ý
                Toast.makeText(this, "Chế độ gợi ý chọn ô đã tắt", Toast.LENGTH_SHORT).show();
                hint = false;
                btnhinttc.setImageResource(R.drawable.tat_goi_y_chon); // Đổi ảnh bóng đèn tắt
            } else {
                // Nếu chế độ gợi ý đang tắt, bật chế độ gợi ý
                Toast.makeText(this, "Chế độ gợi ý chọn ô đã bật", Toast.LENGTH_SHORT).show();
                hint = true;
                btnhinttc.setImageResource(R.drawable.goi_y_chon); // Đổi ảnh bóng đèn sáng
                waitForUserClick(); // Cho phép người dùng nhấn các ô đáp án
            }
        });
        // Thiết lập sự kiện khi ấn nút hint hiển thị toàn bộ đáp án
        btnhintall.setOnClickListener(v -> {
            // Hiển thị thông báo
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Gợi Ý!");
            builder.setMessage("Bạn có muốn sử dụng 50 điểm để mở toàn bộ đáp án không!.");

            // Nút "OK" để qua câu hỏi tiếp theo
            builder.setPositiveButton("OK", (dialog, which) -> {
                showFullAnswer();
            });
            // Nút "Hủy" để đóng hộp thoại
            builder.setNegativeButton("Hủy", (dialog, which) -> {
                dialog.cancel();
            });
            builder.show();

        });

    }

    private void music(){
        if (flag) {
            Intent musicIntent = new Intent(DuoihinhbatchuActivity.this, MyService.class);
            stopService(musicIntent);
            btnmusic.setImageResource(R.drawable.musicoff);
        } else {
            Intent musicIntent = new Intent(DuoihinhbatchuActivity.this, MyService.class);
            startService(musicIntent);
            btnmusic.setImageResource(R.drawable.musicon);
        }
        flag = !flag;
    }

    private void resetDatabase() {
        // Xóa cơ sở dữ liệu hiện tại
        File dbFile = new File(getDatabasePath());  //Lấy đường dẫn của csdl hiện tại
        if (dbFile.exists()) {
            if (dbFile.delete()) {

            } else {
                Toast.makeText(this, "Không thể xóa Databases cũ", Toast.LENGTH_SHORT).show();
            }
        }

        // Sao chép cơ sở dữ liệu mới từ assets vào thư mục databases
        processCopy();
    }

    //Xử lý bất kỳ ngoại lệ nào trong sao chép csdl
    private void processCopy() {
        try {
            copyDataBaseFromAsset();
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    //Lấy đường dẫn đầy đủ đến tệp csdl trong thư mục của ứng dụng
    private String getDatabasePath() {
        return getApplicationInfo().dataDir + DB_PATH_SUFFIX + DATABASE_NAME;
    }

    // Sao chép csdl
    private void copyDataBaseFromAsset() throws IOException {
        InputStream myInput = getAssets().open(DATABASE_NAME); //Mở tệp csdl từ thư mục assets
        String outFileName = getDatabasePath(); //Lấy đường dẫn tệp
        File f = new File(getApplicationInfo().dataDir + DB_PATH_SUFFIX); //Tạo file nơi csdl sẽ được lưu trữ
        if (!f.exists()) {  //Kiểm tra xem có tồn tại
            f.mkdir(); // Tạo thư mục
        }

        //Ghi dữ liệu vào csdl ứng dụng từ đường dẫn
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush(); //Đảm bảo tất cả dữ liệu được ghi ra tệp
        myOutput.close(); //Đóng sau khi ghi xong
        myInput.close(); //Đóng sau khi đọc xong
    }

    // Lấy ảnh từ bộ nhớ trong và hiển thị
    private void loadImageFromInternalStorage(String imageName) {
        try {
            File directory = getApplicationContext().getDir("imageDir", Context.MODE_PRIVATE);
            File imageFile = new File(directory, imageName + ".jpg");

            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
                imgcauhoi.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "Ảnh không tồn tại trong thư mục mới", Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Không tìm thấy ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void addAnswerTextWatcher() {
        for (Button answerButton : answerButtons) {
            answerButton.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // Kiểm tra đáp án tự động khi người dùng nhập
                    checkAnswerWhenAllFilled();
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });
        }
    }

    private void checkAnswerWhenAllFilled() {
        // Kiểm tra nếu tất cả các nút đáp án đều đã được điền
        boolean allFilled = true;
        for (Button button : answerButtons) {
            if (button.getText().length() == 0) {
                allFilled = false;
                break;
            }
        }

        if (allFilled) {
            // Chỉ gọi checkAnswer nếu tất cả các nút đáp án đã được điền
            checkAnswer();
        }
    }


    private void checkAnswer() {
        StringBuilder userAnswerBuilder = new StringBuilder();

        // Tạo chuỗi từ tất cả các nút đáp án, bao gồm cả các nút có ký tự từ gợi ý
        for (Button button : answerButtons) {
            if (button.getText().length() > 0) {
                userAnswerBuilder.append(button.getText().toString());
            }
        }

        String userAnswer = userAnswerBuilder.toString().trim(); // Chuyển thành chuỗi từ StringBuilder

        // So sánh chuỗi của người dùng với đáp án chính xác
        if (userAnswer.equals(correctAnswer)) {
            score += 5;
            // Hiển thị thông báo chúc mừng khi người chơi trả lời đúng
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Chúc mừng!");
            builder.setMessage(answerdetail + " là đáp án chính xác! Bạn đã được cộng 5 điểm.");

            // Nút "OK" để qua câu hỏi tiếp theo
            builder.setPositiveButton("OK", (dialog, which) -> {
                textscore.setText("Điểm: " + score);
                saveProgress(currentIndex, score);
                loadNextQuestion();
            });

            builder.setCancelable(false);
            builder.show();

        } else {
            Toast.makeText(this, "Đáp án sai, vui lòng thử lại!", Toast.LENGTH_SHORT).show();

            // Reset các nút đáp án
            answerButtonContainer.removeAllViews();
            answerButtons.clear();

            // Tạo lại các nút đáp án và các nút đánh lừa
            createAnswerButtons(correctAnswer);
            createDeceiveButtons(deceive);
        }
    }

    private void loadNextQuestion() {
        // Nếu có con trỏ hiện tại, đóng nó
        if (currentCursor != null) {
            currentCursor.close();
        }
        // Xóa các nút cũ
        buttonContainer.removeAllViews();
        answerButtonContainer.removeAllViews();
        answerButtons.clear();

        // Truy vấn dữ liệu tiếp theo từ cơ sở dữ liệu ngãu nhiên
        currentCursor = database.rawQuery("SELECT * FROM tbcauhoi WHERE ID = ?", new String[]{String.valueOf(currentIndex)});
        if (currentCursor != null && currentCursor.moveToFirst()) {
            String imageName = currentCursor.getString(1); // Lấy tên ảnh
            correctAnswer = currentCursor.getString(1); // Lấy đáp án đúng (đảm bảo vị trí chính xác)
            answerdetail = currentCursor.getString(2); // Lấy đáp án chi tiết
            deceive = currentCursor.getString(3); // Lấy ký tự đánh lừa

            // Hiển thị ảnh từ cơ sở dữ liệu
            loadImageFromInternalStorage(imageName);

            // Tạo các nút đáp án
            createAnswerButtons(correctAnswer);
            // Tạo các nút đánh lừa
            createDeceiveButtons(deceive);

            // Tăng chỉ số hàng hiện tại
            currentIndex++;
        } else {
            clearProgress(); // Xóa lưu
            updateScoreToFirebase(score);
            // Hiển thị thông báo chúc mừng khi người chơi trả lời đúng
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Chúc mừng!");
            builder.setMessage("Bạn đã hoàn thành trò chơi!");
            // Nút "OK" để qua câu hỏi tiếp theo
            builder.setPositiveButton("OK", (dialog, which) -> {
                // Quay về màn hình chính
                Intent intent = new Intent(DuoihinhbatchuActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Kết thúc Activity hiện tại
            });

            builder.setCancelable(false);
            builder.show();
        }
    }

    private void createAnswerButtons(String answer) {
        answerButtonContainer.removeAllViews(); // Xóa các dòng cũ
        answerButtons.clear();

        LinearLayout currentRow = createNewRow();
        answerButtonContainer.addView(currentRow);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int totalWidth = 0;

        int buttonWidth = 100;
        int buttonHeight = 100;

        for (char c : answer.toCharArray()) {
            // Tạo nút ô đáp án
            Button answerButton = new Button(this);
            answerButton.setText(""); // Nút rỗng không có văn bản
            answerButton.setEnabled(false); // Nút đáp án không thể nhấn

            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(buttonWidth, buttonHeight);
            answerButton.setLayoutParams(buttonParams);

            answerButton.setOnClickListener(v -> {
                Button answerButton1 = (Button) v;
                // Lấy button đánh lừa từ tag và hiện nó lại
                Button associatedDeceiveButton = (Button) answerButton1.getTag();
                if (associatedDeceiveButton != null) {
                    // Khôi phục lại ký tự từ Tag
                    String character = (String) associatedDeceiveButton.getTag();
                    associatedDeceiveButton.setText(character);
                    associatedDeceiveButton.setEnabled(true);
                    answerButton1.setText(""); // Xóa văn bản trong nút đáp án
                    answerButton1.setEnabled(false); // Vô hiệu hóa nút đáp án lại
                }
            });

            currentRow.setGravity(Gravity.CENTER);

            // Kiểm tra nếu chiều rộng vượt quá chiều rộng màn hình
            if (totalWidth + buttonWidth > screenWidth - 250) {
                // Tạo một dòng mới và đặt nó vào answerButtonContainer
                currentRow = createNewRow();
                answerButtonContainer.addView(currentRow);
                totalWidth = 0; // Đặt lại tổng chiều rộng
            }

            currentRow.addView(answerButton);
            answerButtons.add(answerButton); // Thêm nút đáp án vào danh sách
            totalWidth += buttonWidth;
        }
        addAnswerTextWatcher();
    }

    private void createDeceiveButtons(String deceive) {
        buttonContainer.removeAllViews(); // Xóa các dòng cũ

        LinearLayout currentRow = createNewRow();
        buttonContainer.addView(currentRow);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int totalWidth = 0;

        int buttonWidth = 100;
        int buttonHeight = 100;

        for (char c : deceive.toCharArray()) {
            // Tạo nút đánh lừa
            Button deceiveButton = new Button(this);
            deceiveButton.setText(String.valueOf(c));

            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(buttonWidth, buttonHeight);
            deceiveButton.setLayoutParams(buttonParams);

            deceiveButton.setOnClickListener(v -> {
                if (checkanswerisfull()){
                    // Lưu ký tự vào tag trước khi xóa văn bản
                    deceiveButton.setTag(deceiveButton.getText().toString());
                    // Truyền ký tự vào các nút ô đáp án
                    fillAnswerButtons(deceiveButton.getText().toString(),deceiveButton);
                    deceiveButton.setText("");
                    deceiveButton.setEnabled(false);
                }
            });

            currentRow.setGravity(Gravity.CENTER);

            // Kiểm tra nếu chiều rộng vượt quá chiều rộng màn hình
            if (totalWidth + buttonWidth > screenWidth - 200) {
                // Tạo một dòng mới và đặt nó vào buttonContainer
                currentRow = createNewRow();
                buttonContainer.addView(currentRow);
                totalWidth = 0; // Đặt lại tổng chiều rộng
            }

            // Thêm nút đánh lừa vào dòng hiện tại
            currentRow.addView(deceiveButton);
            totalWidth += buttonWidth;
        }
    }

    private LinearLayout createNewRow() {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setOrientation(LinearLayout.HORIZONTAL);
        return row;
    }

    private void fillAnswerButtons(String character, Button deceiveButton) {
        for (Button button : answerButtons) {
            if (button.getText().toString().isEmpty()) {
                button.setText(character);
                button.setEnabled(true); // Cho phép nút đáp án có thể nhấn
                button.setTag(deceiveButton); // Lưu button đánh lừa vào tag của button đáp án
                break; // Chỉ điền vào nút đầu tiên rỗng
            }
        }
    }

    private boolean checkanswerisfull(){
        for (Button button : answerButtons) {
            if (button.getText().toString().isEmpty()) {
                return true;
            }
        }
        return false;
    }
    // Lưu tiến độ người chơi
    private void saveProgress(int index, int score) {
        // Sử dụng SharedPreferences để lưu chỉ số hiện tại
        getSharedPreferences("game_progress", MODE_PRIVATE)
                .edit()
                .putInt("current_index", index)
                .putInt("score", score)
                .apply();
    }
    // Xóa tiến độ người chơi
    private void clearProgress() {
        // Sử dụng SharedPreferences để xóa tất cả dữ liệu liên quan đến tiến độ
        getSharedPreferences("game_progress", MODE_PRIVATE)
                .edit()
                .clear() // Xóa tất cả các giá trị trong SharedPreferences
                .apply();
    }


    private void RandomHint() {
        // Tạo một danh sách các chỉ số của các nút đáp án đang rỗng
        List<Integer> emptyButtonIndices = new ArrayList<>();
        for (int i = 0; i < answerButtons.size(); i++) {
            if (answerButtons.get(i).getText().toString().isEmpty()) {
                emptyButtonIndices.add(i);
            }
        }

        if (emptyButtonIndices.isEmpty()) {
            Toast.makeText(this, "Không có ô đáp án nào rỗng để gợi ý!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (score <10){
            Toast.makeText(DuoihinhbatchuActivity.this, "không đủ điểm để dùng gợi ý này", Toast.LENGTH_SHORT).show();
        }
        else {
            score -= 15;
            textscore.setText("Điểm: " + score);
            // Chọn một chỉ số ngẫu nhiên
            int randomIndex = (int) (Math.random() * emptyButtonIndices.size());
            int buttonIndex = emptyButtonIndices.get(randomIndex);

            // Tìm ký tự ngẫu nhiên trong đáp án chính xác
            char hintChar = correctAnswer.charAt(buttonIndex);

            // Tìm và hiển thị ký tự gợi ý
            Button answerButton = answerButtons.get(buttonIndex);
            answerButton.setText(String.valueOf(hintChar));
            answerButton.setEnabled(false); // Vô hiệu hóa nút sau khi hiển thị đáp án
            removeCharacterFromDeceiveButtons(hintChar);
            saveProgress(currentIndex,score);
        }
    }

    private void waitForUserClick() {
        // Enable all answer buttons and set up click listeners for each
        for (Button answerButton : answerButtons) {
            answerButton.setEnabled(true);
            // Set up click listener to show hint when button is clicked
            answerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHint((Button) v);
                }
            });
        }
    }

    private void showHint(Button selectedButton) {
        // Lấy vị trí của button người dùng đã nhấn
        int buttonIndex = answerButtons.indexOf(selectedButton);

        // Kiểm tra nếu buttonIndex hợp lệ và button này trống
        if (buttonIndex != -1) {
            String currentText = selectedButton.getText().toString();

            // Nếu nút trống và điểm đủ để dùng gợi ý
            if (currentText.isEmpty() && score >= 20 && hint) {
                // Lấy ký tự đúng tại vị trí này trong đáp án
                char hintChar = correctAnswer.charAt(buttonIndex);
                // Tìm và hiển thị ký tự gợi ý
                selectedButton.setText(String.valueOf(hintChar));
                selectedButton.setEnabled(false); // Vô hiệu hóa nút sau khi hiển thị đáp án

                // Trừ điểm khi sử dụng gợi ý
                score -= 20;
                disableHintMode();
                hint = false;
                btnhinttc.setImageResource(R.drawable.tat_goi_y_chon); // Đổi ảnh bóng đèn tắt
                Toast.makeText(this, "Chế độ gợi ý chọn ô đã tắt", Toast.LENGTH_SHORT).show();
                // Cập nhật điểm số hiển thị
                textscore.setText("Điểm: " + score);

                // Xóa ký tự khỏi các nút đánh lừa
                removeCharacterFromDeceiveButtons(hintChar);
                saveProgress(currentIndex, score);

            } else if (!currentText.isEmpty()) {
                // Nếu nút đã có ký tự
                Toast.makeText(this, "Ô này đã có ký tự!", Toast.LENGTH_SHORT).show();
            } else if (score < 20) {
                // Nếu không đủ điểm
                Toast.makeText(this, "Không đủ điểm để dùng gợi ý này", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Nếu vị trí nút không hợp lệ
            Toast.makeText(this, "Nút này không hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }


    private void showFullAnswer() {
        if(score < 50) {
            Toast.makeText(DuoihinhbatchuActivity.this, "không đủ điểm để dùng gợi ý này", Toast.LENGTH_SHORT).show();
        }
        else{
            // Cập nhật toàn bộ các nút đáp án với ký tự đúng
            for (int i = 0; i < answerButtons.size(); i++) {
                char correctChar = correctAnswer.charAt(i); // Lấy ký tự đúng tại vị trí này
                Button answerButton = answerButtons.get(i);
                answerButton.setText(String.valueOf(correctChar)); // Hiển thị ký tự đúng trong nút đáp án
                answerButton.setEnabled(false); // Vô hiệu hóa nút sau khi hiển thị đáp án
                // Xóa ký tự khỏi các nút đánh lừa
                removeCharacterFromDeceiveButtons(correctChar);
            }
            // Giảm điểm sau khi hiển thị toàn bộ đáp án
            score -= 50;
            textscore.setText("Điểm: " + score);
            saveProgress(currentIndex,score);
        }
    }

    private void removeCharacterFromDeceiveButtons(char character) {
        // Loop through all deceive buttons to find the one with the matching character
        for (int i = 0; i < buttonContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) buttonContainer.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                Button deceiveButton = (Button) row.getChildAt(j);
                // Check if the button contains the character
                if (deceiveButton.getText().toString().equals(String.valueOf(character))) {
                    // Remove the character from the button
                    deceiveButton.setText("");
                    deceiveButton.setEnabled(false);
                    return; // Exit once the character is found and removed
                }
            }
        }
    }

    private void disableHintMode() {
        // Disable all answer buttons and reset their click listeners to default behavior
        for (Button answerButton : answerButtons) {
            answerButton.setOnClickListener(v -> {
                Button answerButton1 = (Button) v;
                // Lấy button đánh lừa từ tag và hiện nó lại
                Button associatedDeceiveButton = (Button) answerButton1.getTag();
                if (associatedDeceiveButton != null) {
                    // Khôi phục lại ký tự từ Tag
                    String character = (String) associatedDeceiveButton.getTag();
                    associatedDeceiveButton.setText(character);
                    associatedDeceiveButton.setEnabled(true);
                    answerButton1.setText(""); // Xóa văn bản trong nút đáp án
                    answerButton1.setEnabled(false); // Vô hiệu hóa nút đáp án lại
                }
            });
        }
    }

    // Cập nhật điểm số cho người chơi trong Firebase
    private void updateScoreToFirebase(int newScore) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("xephang");

        // Truy cập đến người chơi bằng tên người chơi (key chính)
        DatabaseReference playerRef = databaseReference.child(playerName);
        // Đọc điểm hiện tại từ Firebase
        playerRef.child("diem").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer currentScore = snapshot.getValue(Integer.class);
                if (currentScore != null) {
                    // Nếu điểm mới cao hơn điểm cũ, cập nhật điểm mới
                    if (newScore > currentScore) {
                        playerRef.child("diem").setValue(newScore);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DuoihinhbatchuActivity.this, "Lỗi cập nhật điểm: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Tạo danh sách các drawable IDs
    private int[] drawableIds = {
            R.drawable.aimo,
            R.drawable.bachimbaynoi,
            R.drawable.baocao,
            R.drawable.baoham,
            R.drawable.catinh,
            R.drawable.chantuong,
            R.drawable.chidiem,
            R.drawable.choandagaansoi,
            R.drawable.cobap,
            R.drawable.coloa,
            R.drawable.congbo,
            R.drawable.dongcamcongkho,
            R.drawable.giancachemthot,
            R.drawable.hanhlang,
            R.drawable.hoahau,
            R.drawable.khauxatamphat,
            R.drawable.kiemchuyen,
            R.drawable.kienthuc,
            R.drawable.lenvoixuongcho,
            R.drawable.luatsu,
            R.drawable.macarong,
            R.drawable.matma,
            R.drawable.mynhanngu,
            R.drawable.ngangu,
            R.drawable.ykien,
            R.drawable.nhatbao,
            R.drawable.obama,
            R.drawable.ongnoigabanoivit,
            R.drawable.ruatien,
            R.drawable.taihoa,
    };
    private void saveDrawablesToInternalStorage() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE);

        for (int drawableId : drawableIds) {
            // Lấy Bitmap từ drawable resource
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableId);

            // Tạo tên file cho ảnh
            String imageName = getResources().getResourceEntryName(drawableId); // Lấy tên tài nguyên

            // Tạo file trong thư mục lưu trữ ảnh
            File path = new File(directory, imageName + ".jpg");

            try (FileOutputStream fos = new FileOutputStream(path)) {
                // Nén ảnh dưới định dạng JPEG và lưu vào file
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Log.i("SaveImage", "Lưu ảnh thành công: " + imageName);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("SaveImage", "Lưu ảnh thất bại: " + imageName);
            }
        }
    }
}