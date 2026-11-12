package util;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;

/** Independent from DBContext: does not change the working database configuration. */
public final class FeatureConfig {
    private FeatureConfig() { }
    public static String get(String key) {
        String value=System.getProperty(key);
        if(value==null) value=System.getenv(key);
        if(value!=null&&!value.isBlank()) return value.trim();
        String configured=System.getProperty("HOMETECH_FEATURE_CONFIG",System.getenv("HOMETECH_FEATURE_CONFIG"));
        Path path=configured==null?Path.of(System.getProperty("user.home"),".hometech","features.properties"):Path.of(configured);
        if(configured==null&&!Files.exists(path)&&System.getenv("USERPROFILE")!=null)
            path=Path.of(System.getenv("USERPROFILE"),".hometech","features.properties");
        Properties p=new Properties();
        if(Files.exists(path)) try(Reader r=Files.newBufferedReader(path,StandardCharsets.UTF_8)){p.load(r);}
        catch(IOException e){throw new IllegalStateException("Không đọc được cấu hình tính năng.");}
        value=p.getProperty(key);return value==null?"":value.trim();
    }
    public static String required(String key) {
        String value=get(key);if(value.isBlank())throw new IllegalStateException("Chưa cấu hình "+key);return value;
    }
    public static boolean bankReady() {
        try { VietQrUtil.fromEnvironment(new java.math.BigDecimal("10000"),"HTCHECK"); return true; }
        catch(RuntimeException e){return false;}
    }
}
