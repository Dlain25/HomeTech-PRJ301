package util;

import java.math.BigDecimal;
import java.util.Set;

public final class OrderRules {
    private OrderRules() { }
    public static boolean cancellable(String order,String payment) {
        return "PENDING".equals(order) && Set.of("PENDING","FAILED").contains(payment == null ? "" : payment);
    }
    public static String next(String action,String state,String method,String payment) {
        switch(action) {
            case "approve":
                if("PENDING".equals(state) && (("COD".equals(method) && "PENDING".equals(payment))
                    || ("BANK_TRANSFER".equals(method) && "PAID".equals(payment)))) return "CONFIRMED";
                break;
            case "ship":
                if("CONFIRMED".equals(state) && ("PAID".equals(payment)
                    || ("COD".equals(method) && "PENDING".equals(payment)))) return "SHIPPING";
                break;
            case "deliver":
                if("SHIPPING".equals(state) && ("PAID".equals(payment)
                    || ("COD".equals(method) && "PENDING".equals(payment)))) return "DELIVERED";
                break;
            case "cancel": case "cancelUser":
                if(cancellable(state,payment)) return "CANCELLED";
                break;
            default: throw new IllegalArgumentException("Thao tác không hợp lệ.");
        }
        throw new IllegalArgumentException("Trạng thái đơn/thanh toán chưa phù hợp với thao tác. Hãy tải lại trang.");
    }
    public static void sameAmount(BigDecimal expected,BigDecimal received) {
        if(expected == null || received == null || expected.signum()<=0 || expected.compareTo(received)!=0)
            throw new IllegalArgumentException("Số tiền thực nhận phải khớp số tiền yêu cầu; trường hợp lệch tiền cần đối soát riêng.");
    }
}
