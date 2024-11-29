package com.example.nhom4android;

public class xephang {
    private String ten;
    private int diem;

    public xephang(){

    }

    public  xephang(String ten,int diem){
        this.ten = ten;
        this.diem = diem;
    }

    public String getTen() {
        return ten;
    }
    public void setTen(String ten) {
        this.ten = ten;
    }
    public int getDiem() {
        return diem;
    }
    public void setDiem(int diem) {
        this.diem = diem;
    }

    @Override
    public String toString(){
        return ten + ", Điểm: " + diem;
    }
}
