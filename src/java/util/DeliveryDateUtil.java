package util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

public final class DeliveryDateUtil {
    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private DeliveryDateUtil() { }
    // Suggested project rule: tomorrow through the following 14 days, inclusive.
    public static LocalDate parse(String date, String slot, Clock clock) {
        LocalDate today = LocalDate.now(clock.withZone(ZONE));
        LocalDate chosen = LocalDate.parse(date);
        if (chosen.isBefore(today.plusDays(1)) || chosen.isAfter(today.plusDays(14)))
            throw new IllegalArgumentException("Chọn ngày nhận từ ngày mai đến 14 ngày tới");
        if (slot == null || !Set.of("AM", "PM", "ANY").contains(slot))
            throw new IllegalArgumentException("Khung giờ không hợp lệ");
        return chosen;
    }
}
