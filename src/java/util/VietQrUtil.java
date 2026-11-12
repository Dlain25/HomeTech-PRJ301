package util;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Builds a QR image URL only. It never confirms money receipt. */
public final class VietQrUtil {
    private VietQrUtil() { }

    public static String imageUrl(String bankId, String accountNo, String accountName,
            String template, BigDecimal amount, String transferCode) {
        require(bankId != null && bankId.matches("[A-Za-z0-9]{2,30}"), "Invalid bank ID");
        require(accountNo != null && accountNo.matches("[A-Za-z0-9]{1,19}"), "Invalid account number");
        require(accountName != null && !accountName.isBlank() && accountName.length() <= 100,
                "Invalid account name");
        require(template != null && template.matches("[A-Za-z0-9_-]{1,100}"), "Invalid template");
        require(amount != null && amount.signum() > 0
                && amount.compareTo(new BigDecimal("9999999999999")) <= 0,
                "QR amount must be positive with at most 13 digits");
        // toBigIntegerExact rejects fractional VND instead of silently rounding.
        String value = amount.toBigIntegerExact().toString();
        require(transferCode != null && transferCode.matches("[A-Z0-9]{1,50}"), "Invalid transfer code");
        return "https://img.vietqr.io/image/" + bankId + "-" + accountNo + "-" + template
                + ".svg?amount=" + value + "&addInfo=" + encode(transferCode)
                + "&accountName=" + encode(accountName);
    }

    public static String fromEnvironment(BigDecimal amount, String transferCode) {
        return imageUrl(env("HOMETECH_BANK_ID"), env("HOMETECH_BANK_ACCOUNT"),
                env("HOMETECH_BANK_NAME"), env("HOMETECH_QR_TEMPLATE"), amount, transferCode);
    }
    private static String env(String key) {
        return FeatureConfig.required(key);
    }
    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
