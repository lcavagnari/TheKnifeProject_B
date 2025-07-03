package it.uninsubria.laboratorioa;

import lombok.Getter;

import java.io.File;
import java.text.SimpleDateFormat;

@Getter
public class Constants {
    public static final File ROOT = new File("data");
    public static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss.SSSS");
}