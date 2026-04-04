package kkennib.net.mining.util;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtil {
    public static String getCurrentDateTime() {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String formattedDate = dateFormat.format(currentDate);
        return formattedDate;
    }

    public static Date getCurrentDateTimeKRFromLocalDateTime() {
        // 한국 시간을 지정합니다.
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        LocalDateTime now = LocalDateTime.now(zoneId);
        return Timestamp.valueOf(now);
    }


    public static String getCurrentDateTimeKR() {
        // 한국 시간을 지정합니다.
        ZoneId zoneId = ZoneId.of("Asia/Seoul");

        // 현재 날짜와 시간을 한국 시간으로 가져옵니다.
        LocalDateTime now = LocalDateTime.now(zoneId);

        // 출력 형식을 지정합니다.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return formatter.format(now);
    }

    public static String convertUtc2Kr(String utctime) {
        Timestamp timestamp = Timestamp.valueOf(utctime);

        // 한국 시간대 (KST) 설정
        TimeZone kstTimeZone = TimeZone.getTimeZone("Asia/Seoul");

        // SimpleDateFormat을 이용하여 원하는 형식 설정
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        dateFormat.setTimeZone(kstTimeZone);

        // Timestamp를 한국 시간대로 변환하여 출력
        String formattedDate = dateFormat.format(timestamp);
        return formattedDate;
    }
}
