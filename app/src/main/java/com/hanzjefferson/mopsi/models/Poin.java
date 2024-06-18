package com.hanzjefferson.mopsi.models;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Poin {
    public String keterangan;
    public int bobot;
    public String tanggal = new SimpleDateFormat("YYYY-MM-dd").format(new Date());
}
