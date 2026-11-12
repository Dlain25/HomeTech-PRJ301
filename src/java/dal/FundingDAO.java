package dal;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.*;
import util.OrderRules;

public final class FundingDAO extends ExtensionDAO {
    public FundingDAO() throws SQLException { super(); }
    private String table(String kind) {
        if("topup".equals(kind)) return "TopUpRequests";
        if("service".equals(kind)) return "ServiceRegistrations";
        throw new IllegalArgumentException("Loại yêu cầu không hợp lệ.");
    }
    private String key(String kind) { table(kind); return "topup".equals(kind)?"topup_id":"registration_id"; }
    public BigDecimal balance(int user) throws SQLException {
        List<Map<String,Object>> r=query("SELECT balance FROM dbo.WalletAccounts WHERE user_id=?",user);
        return r.isEmpty()?BigDecimal.ZERO:amount(r.get(0),"balance");
    }
    public List<Map<String,Object>> plans(boolean admin,int actor) throws SQLException {
        if(admin) permission(actor,"SERVICE_REVIEW");
        return query("SELECT * FROM dbo.ServicePlans "+(admin?"":"WHERE is_active=1 ")+"ORDER BY plan_id DESC");
    }
    public List<Map<String,Object>> requests(String kind,int actor,boolean admin,int page) throws SQLException {
        if(admin) permission(actor,"topup".equals(kind)?"TOPUP_REVIEW":"SERVICE_REVIEW");
        return query("SELECT x.*,x."+key(kind)+" AS target_id,u.username FROM dbo."+table(kind)+" x JOIN dbo.Users u ON u.user_id=x.user_id "+
            (admin?"":"WHERE x.user_id=? ")+"ORDER BY x."+key(kind)+" DESC OFFSET ? ROWS FETCH NEXT 30 ROWS ONLY",admin?new Object[]{(page-1)*30}:new Object[]{actor,(page-1)*30});
    }
    public long create(String kind,int user,BigDecimal requestedAmount,int plan,String nonce) throws SQLException {
        table(kind);
        require(nonce!=null&&nonce.matches("[a-fA-F0-9-]{36}"),"Mã biểu mẫu không hợp lệ. Hãy tải lại trang.","HT400");
        if("topup".equals(kind)) require(requestedAmount!=null && requestedAmount.stripTrailingZeros().scale()<=0 && requestedAmount.compareTo(new BigDecimal("10000"))>=0 && requestedAmount.compareTo(new BigDecimal("50000000"))<=0,"Số tiền nạp là số nguyên từ 10.000 đến 50.000.000đ.","HT400");
        return transaction(()->{
            activeUser(user);
            List<Map<String,Object>> existing=query("SELECT "+key(kind)+" AS id FROM dbo."+table(kind)+" WHERE user_id=? AND request_key=?",user,nonce);
            if(!existing.isEmpty()) return number(existing.get(0),"id");
            String code=("topup".equals(kind)?"HTTU":"HTSV")+UUID.randomUUID().toString().replace("-","").toUpperCase(Locale.ROOT);
            if("topup".equals(kind)) return insert("INSERT dbo.TopUpRequests(user_id,amount,transfer_code,request_key) VALUES(?,?,?,?)",user,requestedAmount,code,nonce);
            return insert("INSERT dbo.ServiceRegistrations(user_id,plan_id,plan_name,amount,duration_days,transfer_code,request_key) SELECT ?,plan_id,plan_name,price,duration_days,?,? FROM dbo.ServicePlans WHERE plan_id=? AND is_active=1",user,code,nonce,plan);
        });
    }
    public Map<String,Object> payment(String kind,int user,long id) throws SQLException {
        Map<String,Object> r=one("SELECT * FROM dbo."+table(kind)+" WHERE "+key(kind)+"=? AND user_id=?",id,user);
        r.put("target_id",id);r.put("kind",kind);return r;
    }
    public void report(String kind,int user,long id) throws SQLException {
        require(update("UPDATE dbo."+table(kind)+" SET reported_at=COALESCE(reported_at,SYSUTCDATETIME()) WHERE "+key(kind)+"=? AND user_id=? AND status='PENDING'",id,user)==1,"Yêu cầu không còn chờ xử lý hoặc không thuộc tài khoản của bạn.","HT409");
    }
    public void review(String kind,int actor,long id,boolean approve,String note,String reference,BigDecimal received,LocalDateTime time) throws SQLException {
        String t=table(kind),k=key(kind);
        transaction(()->{
            permission(actor,"topup".equals(kind)?"TOPUP_REVIEW":"SERVICE_REVIEW");
            long owner=number(one("SELECT user_id FROM dbo."+t+" WHERE "+k+"=?",id),"user_id");
            // Serializes different top-ups for one user, including initial wallet creation.
            activeUser((int)owner);
            Map<String,Object> r=one("SELECT * FROM dbo."+t+" WITH(UPDLOCK,HOLDLOCK) WHERE "+k+"=?",id);
            require("PENDING".equals(r.get("status")),"Yêu cầu đã được xử lý. Không duyệt lại.","HT409");
            if(!approve) {
                require(note!=null&&!note.isBlank(),"Nhập lý do từ chối.","HT400");
                update("UPDATE dbo."+t+" SET status='REJECTED',reviewed_by=?,reviewed_at=SYSUTCDATETIME(),review_note=? WHERE "+k+"=?",actor,note,id);
                return null;
            }
            OrderRules.sameAmount(amount(r,"amount"),received);
            require(reference!=null&&!reference.isBlank()&&reference.length()<=150 && time!=null,"Thiếu mã giao dịch hoặc thời gian nhận tiền.","HT400");
            update("INSERT dbo.BankReceipts(bank_reference,"+k+",amount,received_at,recorded_by) VALUES(?,?,?,?,?)",reference,id,received,Timestamp.valueOf(time),actor);
            if("topup".equals(kind)) {
                if(query("SELECT user_id FROM dbo.WalletAccounts WITH(UPDLOCK,HOLDLOCK) WHERE user_id=?",owner).isEmpty()) update("INSERT dbo.WalletAccounts(user_id,balance) VALUES(?,0)",owner);
                update("INSERT dbo.WalletLedger(user_id,topup_id,amount) VALUES(?,?,?)",owner,id,received);
                require(update("UPDATE dbo.WalletAccounts SET balance=balance+?,updated_at=SYSUTCDATETIME() WHERE user_id=?",received,owner)==1,"Không cập nhật được ví.","HT409");
                update("UPDATE dbo.TopUpRequests SET status='APPROVED',reviewed_by=?,reviewed_at=SYSUTCDATETIME(),review_note=? WHERE topup_id=?",actor,note,id);
            } else {
                update("UPDATE dbo.ServiceRegistrations SET status='ACTIVE',reviewed_by=?,reviewed_at=SYSUTCDATETIME(),review_note=?,starts_at=SYSUTCDATETIME(),ends_at=DATEADD(DAY,duration_days,SYSUTCDATETIME()) WHERE registration_id=?",actor,note,id);
            }
            return null;
        });
    }
    public void savePlan(int actor,int id,String name,String description,BigDecimal price,int days,boolean active) throws SQLException {
        require(name!=null&&!name.isBlank()&&name.length()<=120 && description.length()<=1000 && days>0&&days<=3650 && price!=null&&price.signum()>0&&price.compareTo(new BigDecimal("9999999999999"))<=0&&price.stripTrailingZeros().scale()<=0,"Thông tin gói không hợp lệ.","HT400");
        transaction(()->{
            permission(actor,"SERVICE_REVIEW");
            if(id==0) update("INSERT dbo.ServicePlans(plan_name,description,price,duration_days,is_active) VALUES(?,?,?,?,?)",name,description,price,days,active);
            else require(update("UPDATE dbo.ServicePlans SET plan_name=?,description=?,price=?,duration_days=?,is_active=? WHERE plan_id=?",name,description,price,days,active,id)==1,"Không tìm thấy gói.","HT404");
            return null;
        });
    }
}
