package dal;
import java.sql.*;
import java.time.*;
import java.util.*;

public final class ReportDAO extends ExtensionDAO {
    public ReportDAO() throws SQLException { super(); }
    public Map<String,Object> load(int actor,LocalDate day,int year) throws SQLException {
        permission(actor,"REPORT_VIEW");
        Map<String,Object> out=new HashMap<>();
        java.sql.Date selected=java.sql.Date.valueOf(day),start=java.sql.Date.valueOf(day.withDayOfMonth(1)),end=java.sql.Date.valueOf(day.withDayOfMonth(1).plusMonths(1));
        out.put("work",one("SELECT COUNT_BIG(*) AS scheduled,COALESCE(SUM(CASE WHEN order_status IN('PENDING','CONFIRMED','SHIPPING') THEN 1 ELSE 0 END),0) AS remaining,COALESCE(SUM(CASE WHEN order_status='DELIVERED' THEN 1 ELSE 0 END),0) AS completed,COALESCE(SUM(CASE WHEN order_status='CANCELLED' THEN 1 ELSE 0 END),0) AS cancelled FROM dbo.Orders WHERE desired_delivery_date=?",selected));
        out.put("overdue",one("SELECT COUNT_BIG(*) AS total FROM dbo.Orders WHERE desired_delivery_date<? AND order_status IN('PENDING','CONFIRMED','SHIPPING')",selected));
        out.put("unscheduled",one("SELECT COUNT_BIG(*) AS total FROM dbo.Orders WHERE desired_delivery_date IS NULL AND order_status IN('PENDING','CONFIRMED','SHIPPING')"));
        out.put("legacy",one("SELECT COUNT_BIG(*) AS total FROM dbo.Orders WHERE order_status='DELIVERED' AND delivered_at IS NULL"));
        out.put("tasks",query("SELECT TOP 100 order_id,recipient_name,recipient_phone,delivery_slot,order_status FROM dbo.Orders WHERE desired_delivery_date=? AND order_status IN('PENDING','CONFIRMED','SHIPPING') ORDER BY delivery_slot,order_id",selected));
        // Calendar CTE fills days with zero; one row per day, never join payments to order items.
        out.put("daily",query("WITH days AS (SELECT CAST(? AS DATE) AS d UNION ALL SELECT DATEADD(DAY,1,d) FROM days WHERE DATEADD(DAY,1,d)<?) SELECT d AS period,COALESCE(SUM(e.recognized_orders),0) AS orders,COALESCE(SUM(e.gross_revenue),0) AS gross,COALESCE(SUM(e.refunds),0) AS refunds,COALESCE(SUM(e.gross_revenue-e.refunds),0) AS net FROM days LEFT JOIN dbo.vw_HTRevenueEvents e ON e.business_date=days.d GROUP BY d ORDER BY d",start,end));
        out.put("monthly",query("WITH months AS (SELECT 1 AS m UNION ALL SELECT m+1 FROM months WHERE m<12) SELECT m AS period,COALESCE(SUM(e.recognized_orders),0) AS orders,COALESCE(SUM(e.gross_revenue),0) AS gross,COALESCE(SUM(e.refunds),0) AS refunds,COALESCE(SUM(e.gross_revenue-e.refunds),0) AS net FROM months LEFT JOIN dbo.vw_HTRevenueEvents e ON e.business_date>=DATEFROMPARTS(?,m,1) AND e.business_date<DATEADD(MONTH,1,DATEFROMPARTS(?,m,1)) GROUP BY m ORDER BY m",year,year));
        out.put("cash",query("SELECT source,COALESCE(SUM(cash_in),0) AS incoming,COALESCE(SUM(cash_out),0) AS outgoing FROM dbo.vw_HTCashEvents WHERE business_date>=? AND business_date<? GROUP BY source",start,end));
        LocalDateTime utcStart=day.atStartOfDay().minusHours(7);
        out.put("created",one("SELECT COUNT_BIG(*) AS total FROM dbo.Orders WHERE created_at>=? AND created_at<?",Timestamp.valueOf(utcStart),Timestamp.valueOf(utcStart.plusDays(1))));
        return out;
    }
}
