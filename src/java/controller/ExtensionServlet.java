package controller;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import dal.*;
import model.User;
import util.*;

@WebServlet(urlPatterns={"/payment/qr","/payment/report","/wallet","/services","/returns","/admin/orders","/admin/topups","/admin/services","/admin/returns","/admin/reports"})
public class ExtensionServlet extends HttpServlet {
    private static String value(HttpServletRequest r,String key) {String v=r.getParameter(key);return v==null?"":v.trim();}
    private static long id(HttpServletRequest r,String key) {
        long n=Long.parseLong(value(r,key));if(n<=0)throw new IllegalArgumentException("Mã yêu cầu không hợp lệ.");return n;
    }
    private static BigDecimal money(HttpServletRequest r) {
        String raw=value(r,"amount");
        if(!raw.matches("[0-9]{1,13}(\\.0{1,2})?"))throw new IllegalArgumentException("Nhập số tiền nguyên VND, không có dấu phân cách hàng nghìn.");
        return new BigDecimal(raw);
    }
    private static int page(HttpServletRequest r) {
        try{return Math.min(100000,Math.max(1,Integer.parseInt(value(r,"page"))));}catch(NumberFormatException e){return 1;}
    }
    private static LocalDateTime received(HttpServletRequest r) {
        LocalDateTime local=LocalDateTime.parse(value(r,"receivedAt"));
        LocalDateTime utc=local.minusHours(7);
        if(utc.isAfter(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(5)) || utc.getYear()<2000)
            throw new IllegalArgumentException("Thời điểm nhận tiền không hợp lệ (nhập giờ Việt Nam).");
        return utc;
    }
    private void context(HttpServletRequest r,User user) throws SQLException {
        try(ExtensionDAO dao=new ExtensionDAO()){r.setAttribute("permissions",dao.permissions(user.getUserId()));}
        r.setAttribute("pageNumber",page(r));r.setAttribute("requestKey",UUID.randomUUID().toString());
        r.setAttribute("bankReady",FeatureConfig.bankReady());
        r.setAttribute("nowUtc",Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
        r.setAttribute("receivedDefault",LocalDateTime.now(DeliveryDateUtil.ZONE).withSecond(0).withNano(0));
    }
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        User user=AuthUtil.requireLogin(req,res);if(user==null)return;
        res.setHeader("Cache-Control","no-store");
        String route=req.getServletPath(),view="";
        try {
            context(req,user);
            if("/payment/qr".equals(route)) {
                String kind=value(req,"kind");long target=id(req,"id");Map<String,Object> p;
                if("order".equals(kind))try(WorkflowDAO dao=new WorkflowDAO()){p=dao.orderPayment(user.getUserId(),target);}
                else try(FundingDAO dao=new FundingDAO()){p=dao.payment(kind,user.getUserId(),target);}
                req.setAttribute("payment",p);
                if("PENDING".equals(p.get("status"))) {
                    try {req.setAttribute("qrUrl",VietQrUtil.fromEnvironment((BigDecimal)p.get("amount"),(String)p.get("transfer_code")));}
                    catch(RuntimeException e){req.setAttribute("configError","Chuyển khoản chưa được cấu hình. Vui lòng liên hệ cửa hàng.");}
                }
                req.setAttribute("bankId",FeatureConfig.get("HOMETECH_BANK_ID"));
                req.setAttribute("bankAccount",FeatureConfig.get("HOMETECH_BANK_ACCOUNT"));
                req.setAttribute("bankName",FeatureConfig.get("HOMETECH_BANK_NAME"));
                res.setHeader("Referrer-Policy","no-referrer");view="qr";
            } else if("/admin/orders".equals(route)) {
                String status=value(req,"status"),day=value(req,"day");
                if(!Set.of("","PENDING","CONFIRMED","SHIPPING","DELIVERED","CANCELLED").contains(status))throw new IllegalArgumentException("Trạng thái lọc không hợp lệ.");
                try(WorkflowDAO dao=new WorkflowDAO()) {
                    req.setAttribute("rows",dao.orders(user.getUserId(),day.isEmpty()?null:java.sql.Date.valueOf(LocalDate.parse(day)),status,page(req)));
                    if(!value(req,"detail").isEmpty())req.setAttribute("detailItems",dao.items(user.getUserId(),id(req,"detail")));
                }
                req.setAttribute("day",day);req.setAttribute("status",status);view="admin-orders";
            } else if(route.endsWith("/returns")) {
                boolean admin=route.startsWith("/admin/");
                try(WorkflowDAO dao=new WorkflowDAO()){req.setAttribute("rows",dao.returns(user.getUserId(),admin,page(req)));}
                req.setAttribute("adminMode",admin);view="returns";
            } else if("/admin/reports".equals(route)) {
                LocalDate day=value(req,"day").isEmpty()?LocalDate.now(DeliveryDateUtil.ZONE):LocalDate.parse(value(req,"day"));
                int year=value(req,"year").isEmpty()?day.getYear():Integer.parseInt(value(req,"year"));
                if(year<2000||year>2100||day.getYear()<2000||day.getYear()>2100)throw new IllegalArgumentException("Chọn năm từ 2000 đến 2100.");
                try(ReportDAO dao=new ReportDAO()){for(Map.Entry<String,Object> e:dao.load(user.getUserId(),day,year).entrySet())req.setAttribute(e.getKey(),e.getValue());}
                req.setAttribute("day",day);req.setAttribute("year",year);view="reports";
            } else {
                boolean admin=route.startsWith("/admin/"),service=route.endsWith("/services");
                String kind=service?"service":"topup";
                try(FundingDAO dao=new FundingDAO()) {
                    req.setAttribute("rows",dao.requests(kind,user.getUserId(),admin,page(req)));
                    if(service)req.setAttribute("plans",dao.plans(admin,user.getUserId()));
                    if(!admin&&!service)req.setAttribute("balance",dao.balance(user.getUserId()));
                }
                req.setAttribute("adminMode",admin);req.setAttribute("kind",kind);view="funding";
            }
            req.getRequestDispatcher("/WEB-INF/views/extension/"+view+".jsp").forward(req,res);
        } catch(SQLException e) {error(req,res,e);}
        catch(IllegalArgumentException | DateTimeException e){res.setStatus(400);req.setAttribute("message","Dữ liệu yêu cầu không hợp lệ. Vui lòng kiểm tra ngày, mã và số tiền.");req.getRequestDispatcher("/WEB-INF/views/extension/error.jsp").forward(req,res);}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        User user=AuthUtil.requireLogin(req,res);if(user==null)return;
        String route=req.getServletPath(),action=value(req,"action"),redirect=route;
        try {
            String note=value(req,"note");if(note.length()>500)throw new IllegalArgumentException("Ghi chú tối đa 500 ký tự.");
            if("/payment/report".equals(route)) {
                String kind=value(req,"kind");long target=id(req,"id");
                if("order".equals(kind))try(WorkflowDAO dao=new WorkflowDAO()){dao.reportOrder(user.getUserId(),target);}
                else try(FundingDAO dao=new FundingDAO()){dao.report(kind,user.getUserId(),target);}
                redirect="/payment/qr?kind="+kind+"&id="+target;
            } else if("/admin/orders".equals(route)) {
                long order=id(req,"id");
                if("paid".equals(action)) {
                    if(!"yes".equals(value(req,"verified")))throw new IllegalArgumentException("Cần xác nhận đã đối chiếu giao dịch ngân hàng.");
                    try(PaymentReviewDAO dao=new PaymentReviewDAO()){dao.confirmBankPayment(order,user.getUserId(),value(req,"reference"),money(req),received(req));}
                } else try(WorkflowDAO dao=new WorkflowDAO()){dao.orderAction(user.getUserId(),order,action,"cod".equals(action)?money(req):null);}
            } else if("/returns".equals(route)) {
                try(WorkflowDAO dao=new WorkflowDAO()){dao.requestReturn(user.getUserId(),id(req,"orderId"),value(req,"reason"));}
            } else if("/admin/returns".equals(route)) {
                if("refund".equals(action)&&!"yes".equals(value(req,"verified")))throw new IllegalArgumentException("Xác nhận đã hoàn tiền thực tế trước khi cập nhật.");
                try(WorkflowDAO dao=new WorkflowDAO()){dao.reviewReturn(user.getUserId(),id(req,"id"),action,note,"yes".equals(value(req,"restock")),value(req,"reference"));}
            } else if("/wallet".equals(route)||"/services".equals(route)) {
                if(!FeatureConfig.bankReady())throw new IllegalArgumentException("Cửa hàng chưa cấu hình chuyển khoản. Chưa thể tạo yêu cầu trả tiền.");
                String kind="/wallet".equals(route)?"topup":"service";long target;
                try(FundingDAO dao=new FundingDAO()){target=dao.create(kind,user.getUserId(),"topup".equals(kind)?money(req):null,"service".equals(kind)?Math.toIntExact(id(req,"planId")):0,value(req,"requestKey"));}
                redirect="/payment/qr?kind="+kind+"&id="+target;
            } else if("/admin/services".equals(route)&&"savePlan".equals(action)) {
                try(FundingDAO dao=new FundingDAO()){dao.savePlan(user.getUserId(),value(req,"planId").isEmpty()?0:Integer.parseInt(value(req,"planId")),value(req,"planName"),value(req,"description"),money(req),Integer.parseInt(value(req,"days")),"yes".equals(value(req,"active")));}
            } else if("/admin/topups".equals(route)||"/admin/services".equals(route)) {
                if(!Set.of("approve","reject").contains(action))throw new IllegalArgumentException("Thao tác không hợp lệ.");
                boolean approve="approve".equals(action);
                if(approve&&!"yes".equals(value(req,"verified")))throw new IllegalArgumentException("Cần đối chiếu giao dịch thực nhận trước khi duyệt.");
                try(FundingDAO dao=new FundingDAO()){dao.review(route.endsWith("topups")?"topup":"service",user.getUserId(),id(req,"id"),approve,note,value(req,"reference"),approve?money(req):null,approve?received(req):null);}
            } else {res.sendError(400);return;}
            req.getSession().setAttribute("flashSuccess","Đã ghi nhận thao tác. Trạng thái hiện tại được hiển thị bên dưới.");
        } catch(SQLException e) {
            if("HT403".equals(e.getSQLState())||"HT404".equals(e.getSQLState())){error(req,res,e);return;}
            String msg=businessMessage(e);req.getSession().setAttribute("flashError",msg);
            getServletContext().log("HomeTech action failed; state="+e.getSQLState()+", code="+e.getErrorCode());
        } catch(IllegalArgumentException | DateTimeException | ArithmeticException e) {
            req.getSession().setAttribute("flashError",e instanceof NumberFormatException||e instanceof DateTimeException?"Kiểm tra lại mã, ngày giờ và số tiền đã nhập.":e.getMessage());
        }
        if("/payment/report".equals(redirect))redirect="/orders";
        res.sendRedirect(req.getContextPath()+redirect);
    }
    private static String businessMessage(SQLException e) {
        if(e.getErrorCode()==2601||e.getErrorCode()==2627)return "Mã giao dịch hoặc yêu cầu này đã được sử dụng. Hãy tải lại trang và kiểm tra.";
        if(e.getSQLState()!=null&&e.getSQLState().startsWith("HT"))return e.getMessage();
        return "Chưa thể xử lý dữ liệu. Kiểm tra kết nối và các script SQL mở rộng, rồi thử lại.";
    }
    private void error(HttpServletRequest req,HttpServletResponse res,SQLException e) throws ServletException,IOException {
        res.setStatus("HT403".equals(e.getSQLState())?403:"HT404".equals(e.getSQLState())?404:503);
        req.setAttribute("message",businessMessage(e));
        getServletContext().log("HomeTech page failed; state="+e.getSQLState()+", code="+e.getErrorCode());
        req.getRequestDispatcher("/WEB-INF/views/extension/error.jsp").forward(req,res);
    }
}
