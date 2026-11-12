import java.math.BigDecimal;
import java.time.*;
import java.util.HashSet;
import util.*;

public class CoreChecks {
    private static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    private static void rejects(Runnable r) {
        try { r.run(); } catch (IllegalArgumentException | ArithmeticException e) { return; }
        throw new AssertionError("Expected invalid input to be rejected");
    }
    public static void main(String[] args) {
        String qr=VietQrUtil.imageUrl("970415","123456789","CONG TY HOME TECH", "compact2",new BigDecimal("10000.00"),"HTOD123");
        check(qr.contains("amount=10000&addInfo=HTOD123&accountName=CONG+TY+HOME+TECH"));
        rejects(()->VietQrUtil.imageUrl("970415","123","A","compact2",new BigDecimal("10000.01"),"HTOD1"));
        rejects(()->VietQrUtil.imageUrl("970415","123","A","compact2",BigDecimal.ZERO,"HTOD1"));
        rejects(()->VietQrUtil.imageUrl("970415","123","A","compact2",new BigDecimal("10000000000000"),"HTOD1"));
        rejects(()->VietQrUtil.imageUrl("970415","123/evil","A","compact2",BigDecimal.ONE,"HTOD1"));
        rejects(()->VietQrUtil.imageUrl("970415","123","A","compact2",BigDecimal.ONE,"HTOD1&amount=1"));
        HashSet<String> values=new HashSet<>();
        for(int i=0;i<1000;i++) {
            String t=ResetTokenUtil.generate();
            check(ResetTokenUtil.validFormat(t)); check(values.add(t));
            check(ResetTokenUtil.sha256(t).matches("[0-9a-f]{64}"));
            check(ResetTokenUtil.sha256(t).equals(ResetTokenUtil.sha256(t)));
        }
        rejects(()->ResetTokenUtil.sha256("bad"));
        // 18:00 UTC Nov 6 is 01:00 Nov 7 in Vietnam: tomorrow is Nov 8.
        Clock c=Clock.fixed(Instant.parse("2026-11-06T18:00:00Z"), ZoneOffset.UTC);
        check(DeliveryDateUtil.parse("2026-11-08","AM",c).equals(LocalDate.of(2026,11,8)));
        check(DeliveryDateUtil.parse("2026-11-21","ANY",c).equals(LocalDate.of(2026,11,21)));
        rejects(()->DeliveryDateUtil.parse("2026-11-07","AM",c));
        rejects(()->DeliveryDateUtil.parse("2026-11-22","AM",c));
        rejects(()->DeliveryDateUtil.parse("2026-11-08","BAD",c));
        System.out.println("CoreChecks passed (QR validation, token generation, delivery dates).");
    }
}
