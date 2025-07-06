package it.uninsubria.laboratorioa.utils;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.text.SimpleDateFormat;

@Getter
@UtilityClass
public class Constants {
    public static final File ROOT = new File("data");
    public static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss.SSSS");
}