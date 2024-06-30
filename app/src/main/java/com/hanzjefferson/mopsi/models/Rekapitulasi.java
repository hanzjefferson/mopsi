package com.hanzjefferson.mopsi.models;

import java.util.Map;

public class Rekapitulasi {
    public int id, role_id;
    public int class_id;
    public String unique;
    public String full_name;
    public String nick_name;
    public String address;
    public long birth;
    public String phone_num;
    public String gender;
    public Object others;
    public Map<String, Poin[]> poin;
    public Map<String, Map<String, Kehadiran[]>> kehadiran;
}
