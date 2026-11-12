package dal;

import java.sql.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import util.OrderRules;

public final class WorkflowDAO extends ExtensionDAO {
    public WorkflowDAO() throws SQLException { super(); }
    private Map<String,Object> lockOrder(long id) throws SQLException {
        return one("SELECT * FROM dbo.Orders WITH(UPDLOCK,HOLDLOCK) WHERE order_id=?",id);
    }
    private Map<String,Object> lockPayment(long id) throws SQLException {
        List<Map<String,Object>> rows=query("SELECT * FROM dbo.Payments WITH(UPDLOCK,HOLDLOCK) WHERE order_id=?",id);
        require(rows.size()==1,"Đơn cần đúng một khoản thanh toán; vui lòng đối soát dữ liệu cũ.","HT409");
        return rows.get(0);
    }
    public List<Map<String,Object>> orders(int actor,java.sql.Date day,String status,int page) throws SQLException {
        Set<String> perms=permissions(actor);
        require(perms.contains("ORDER_REVIEW")||perms.contains("PAYMENT_REVIEW"),"Bạn chưa được cấp quyền duyệt đơn hoặc thanh toán.","HT403");
        String sql="SELECT o.*,u.username,p.payment_id,p.payment_method,p.payment_status,p.reported_at,p.transfer_code,"+
            "(SELECT COUNT(*) FROM dbo.Payments x WHERE x.order_id=o.order_id) AS payment_count "+
            "FROM dbo.Orders o JOIN dbo.Users u ON u.user_id=o.user_id "+
            "OUTER APPLY (SELECT TOP 1 * FROM dbo.Payments x WHERE x.order_id=o.order_id ORDER BY x.payment_id DESC) p "+
            "WHERE (? IS NULL OR o.desired_delivery_date=?) AND (?='' OR o.order_status=?) "+
            "ORDER BY o.order_id DESC OFFSET ? ROWS FETCH NEXT 30 ROWS ONLY";
        return query(sql,day,day,status,status,(page-1)*30);
    }
    public List<Map<String,Object>> items(int actor,long id) throws SQLException {
        Set<String> p=permissions(actor);
        require(p.contains("ORDER_REVIEW")||p.contains("PAYMENT_REVIEW"),"Chưa có quyền xem đơn.","HT403");
        return query("SELECT * FROM dbo.OrderItems WHERE order_id=? ORDER BY order_item_id",id);
    }
    public void orderAction(int actor,long id,String action,BigDecimal received) throws SQLException {
        transaction(()->{
            if(!"cancelUser".equals(action)) permission(actor,"cod".equals(action)?"PAYMENT_REVIEW":"ORDER_REVIEW");
            else activeUser(actor);
            Map<String,Object> o=lockOrder(id),p=lockPayment(id);
            if("cancelUser".equals(action)) require(number(o,"user_id")==actor,"Không tìm thấy đơn của bạn.","HT404");
            String state=(String)o.get("order_status"), pay=(String)p.get("payment_status"),method=(String)p.get("payment_method");
            require(amount(o,"total_amount").compareTo(amount(p,"amount"))==0,"Dữ liệu tiền đơn không khớp.","HT409");
            if("cod".equals(action)) {
                require("COD".equals(method) && Set.of("SHIPPING","DELIVERED").contains(state),"Chỉ ghi nhận COD khi đang giao hoặc đã giao.","HT409");
                if("PAID".equals(pay)) return null;
                require("PENDING".equals(pay),"Thanh toán không còn chờ xử lý.","HT409");
                OrderRules.sameAmount(amount(p,"amount"),received);
                update("UPDATE dbo.Payments SET payment_status='PAID',paid_at=SYSUTCDATETIME(),confirmed_by=?,confirmed_at=SYSUTCDATETIME() WHERE payment_id=?",actor,number(p,"payment_id"));
                return null;
            }
            String next=OrderRules.next(action,state,method,pay);
            require(update("UPDATE dbo.Orders SET order_status=?,updated_at=SYSUTCDATETIME() WHERE order_id=? AND order_status=?",next,id,state)==1,"Đơn đã thay đổi.","HT409");
            if("CONFIRMED".equals(next)) update("UPDATE dbo.Orders SET approved_by=?,approved_at=SYSUTCDATETIME() WHERE order_id=?",actor,id);
            if("DELIVERED".equals(next)) update("UPDATE dbo.Orders SET delivered_at=SYSUTCDATETIME() WHERE order_id=?",id);
            if("CANCELLED".equals(next)) {
                restock(id);
                update("UPDATE dbo.Payments SET payment_status='FAILED' WHERE order_id=? AND payment_status='PENDING'",id);
            }
            update("INSERT dbo.OrderStatusHistory(order_id,from_status,to_status,actor_id,note) VALUES(?,?,?,?,?)",id,state,next,actor,action);
            return null;
        });
    }
    private void restock(long orderId) throws SQLException {
        // Ascending variant order is shared with checkout; never restore a missing inventory row silently.
        for(Map<String,Object> item:query("SELECT variant_id,quantity FROM dbo.OrderItems WHERE order_id=? ORDER BY variant_id",orderId)) {
            require(update("UPDATE dbo.Inventory SET quantity_available=quantity_available+?,updated_at=SYSUTCDATETIME() WHERE variant_id=?",number(item,"quantity"),number(item,"variant_id"))==1,"Thiếu dữ liệu tồn kho; chưa thể nhập lại hàng.","HT409");
        }
    }
    public Map<String,Object> orderPayment(int user,long order) throws SQLException {
        return transaction(()->{
            Map<String,Object> o=lockOrder(order);
            require(number(o,"user_id")==user,"Không tìm thấy đơn của bạn.","HT404");
            Map<String,Object> p=lockPayment(order);
            require("BANK_TRANSFER".equals(p.get("payment_method")),"Đơn không dùng chuyển khoản.","HT409");
            require(!"CANCELLED".equals(o.get("order_status")),"Đơn đã hủy.","HT409");
            require(amount(o,"total_amount").compareTo(amount(p,"amount"))==0,"Số tiền thanh toán không khớp.","HT409");
            if(p.get("transfer_code")==null && "PENDING".equals(p.get("payment_status"))) {
                String code="HTOD"+order;
                update("UPDATE dbo.Payments SET transfer_code=? WHERE payment_id=? AND transfer_code IS NULL",code,number(p,"payment_id"));
                p.put("transfer_code",code);
            }
            p.put("status",p.get("payment_status")); p.put("target_id",order); p.put("kind","order");
            return p;
        });
    }
    public void reportOrder(int user,long order) throws SQLException {
        transaction(()->{
            activeUser(user); Map<String,Object> o=lockOrder(order),p=lockPayment(order);
            require(number(o,"user_id")==user,"Không tìm thấy đơn của bạn.","HT404");
            require(!"CANCELLED".equals(o.get("order_status")) && "BANK_TRANSFER".equals(p.get("payment_method")) && "PENDING".equals(p.get("payment_status")),"Thanh toán không còn chờ xác nhận.","HT409");
            update("UPDATE dbo.Payments SET reported_at=COALESCE(reported_at,SYSUTCDATETIME()) WHERE payment_id=?",number(p,"payment_id"));return null;
        });
    }
    public List<Map<String,Object>> returns(int actor,boolean admin,int page) throws SQLException {
        if(admin) permission(actor,"RETURN_REVIEW");
        return query("SELECT r.*,o.recipient_name,o.order_status,u.username FROM dbo.ReturnRequests r JOIN dbo.Orders o ON o.order_id=r.order_id JOIN dbo.Users u ON u.user_id=r.user_id "+
            (admin?"":"WHERE r.user_id=? ")+"ORDER BY r.return_id DESC OFFSET ? ROWS FETCH NEXT 30 ROWS ONLY",admin?new Object[]{(page-1)*30}:new Object[]{actor,(page-1)*30});
    }
    public void requestReturn(int user,long order,String reason) throws SQLException {
        require(reason!=null && !reason.isBlank() && reason.length()<=1000,"Nhập lý do hoàn hàng (tối đa 1.000 ký tự).","HT400");
        transaction(()->{
            activeUser(user); Map<String,Object> o=lockOrder(order),p=lockPayment(order);
            require(number(o,"user_id")==user,"Không tìm thấy đơn của bạn.","HT404");
            require("DELIVERED".equals(o.get("order_status")) && "PAID".equals(p.get("payment_status")),"Chỉ yêu cầu hoàn đơn đã giao và thanh toán.","HT409");
            require(!query("SELECT order_id FROM dbo.Orders WHERE order_id=? AND delivered_at IS NOT NULL AND delivered_at<=SYSUTCDATETIME() AND delivered_at>=DATEADD(DAY,-7,SYSUTCDATETIME())",order).isEmpty(),"Đơn thiếu ngày giao hoặc đã quá 7 ngày yêu cầu hoàn.","HT409");
            require(amount(o,"total_amount").compareTo(amount(p,"amount"))==0 && amount(o,"total_amount").signum()>0,"Số tiền cần đối soát.","HT409");
            if(!query("SELECT return_id FROM dbo.ReturnRequests WHERE order_id=?",order).isEmpty()) return null;
            update("INSERT dbo.ReturnRequests(order_id,user_id,reason,refund_amount) VALUES(?,?,?,?)",order,user,reason,amount(o,"total_amount"));return null;
        });
    }
    public void reviewReturn(int admin,long returnId,String action,String note,boolean restock,String reference) throws SQLException {
        transaction(()->{
            permission(admin,"RETURN_REVIEW");
            long order=number(one("SELECT order_id FROM dbo.ReturnRequests WHERE return_id=?",returnId),"order_id");
            Map<String,Object> o=lockOrder(order),p=lockPayment(order);
            Map<String,Object> r=one("SELECT * FROM dbo.ReturnRequests WITH(UPDLOCK,HOLDLOCK) WHERE return_id=?",returnId);
            String state=(String)r.get("status");
            if("approve".equals(action)||"reject".equals(action)) {
                require("REQUESTED".equals(state),"Yêu cầu đã được xử lý.","HT409");
                require(!"reject".equals(action)||!note.isBlank(),"Cần nhập lý do từ chối.","HT400");
                update("UPDATE dbo.ReturnRequests SET status=?,reviewed_by=?,reviewed_at=SYSUTCDATETIME(),review_note=? WHERE return_id=?","approve".equals(action)?"APPROVED":"REJECTED",admin,note,returnId);
            } else if("receive".equals(action)) {
                require("APPROVED".equals(state),"Chỉ nhận hàng sau khi đã duyệt; không được nhập kho lần hai.","HT409");
                if(restock) restock(order);
                update("UPDATE dbo.ReturnRequests SET status='RECEIVED',received_by=?,received_at=SYSUTCDATETIME(),restocked=? WHERE return_id=?",admin,restock,returnId);
            } else if("refund".equals(action)) {
                require("RECEIVED".equals(state)&&"PAID".equals(p.get("payment_status")),"Cần nhận hàng trước khi xác nhận hoàn tiền.","HT409");
                require(reference!=null&&!reference.isBlank()&&reference.length()<=150,"Nhập mã giao dịch hoàn tiền thực tế.","HT400");
                OrderRules.sameAmount(amount(p,"amount"),amount(r,"refund_amount"));
                OrderRules.sameAmount(amount(o,"total_amount"),amount(r,"refund_amount"));
                update("UPDATE dbo.ReturnRequests SET status='REFUNDED',refunded_by=?,refunded_at=SYSUTCDATETIME(),refund_reference=? WHERE return_id=?",admin,reference,returnId);
                update("UPDATE dbo.Payments SET payment_status='REFUNDED' WHERE payment_id=?",number(p,"payment_id"));
            } else throw new IllegalArgumentException("Thao tác hoàn hàng không hợp lệ.");
            return null;
        });
    }
}
