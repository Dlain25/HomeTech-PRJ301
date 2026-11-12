package controller;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.*;
import dal.PasswordResetDAO;
import util.*;

@WebServlet(urlPatterns={"/forgot-password","/reset-password"})
public class PasswordRecoveryServlet extends HttpServlet {
    private final RateLimiter limiter=new RateLimiter();
    private ThreadPoolExecutor mailQueue;
    @Override public void init() {
        mailQueue=new ThreadPoolExecutor(1,1,0,TimeUnit.SECONDS,new ArrayBlockingQueue<>(20),r->{Thread t=new Thread(r,"hometech-reset-mail");t.setDaemon(true);return t;});
    }
    @Override public void destroy(){if(mailQueue!=null)mailQueue.shutdownNow();}
    private void show(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        res.setHeader("Cache-Control","no-store");res.setHeader("Referrer-Policy","no-referrer");
        boolean reset="/reset-password".equals(req.getServletPath());req.setAttribute("resetMode",reset);req.setAttribute("mailReady",MailService.ready());
        req.getRequestDispatcher("/WEB-INF/views/extension/password.jsp").forward(req,res);
    }
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        if("/reset-password".equals(req.getServletPath())) {
            String token=req.getParameter("token");
            if(token!=null) {
                if(!ResetTokenUtil.validFormat(token)){req.setAttribute("error","Liên kết không hợp lệ.");show(req,res);return;}
                // Remove the bearer token from the visible URL; GET never consumes it.
                req.getSession().setAttribute("resetToken",token);
                res.setHeader("Referrer-Policy","no-referrer");res.setHeader("Cache-Control","no-store");
                res.sendRedirect(req.getContextPath()+"/reset-password");return;
            }
            if(req.getSession().getAttribute("resetToken")==null)req.setAttribute("error","Mở liên kết đặt lại mật khẩu trong email để tiếp tục.");
        }
        show(req,res);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        boolean reset="/reset-password".equals(req.getServletPath());
        if(!reset) {
            String email=req.getParameter("email");email=email==null?"":email.trim();
            boolean allowed=limiter.allow("ip:"+req.getRemoteAddr(),5,900000);
            if(allowed&&ValidationUtil.validateEmail(email)==null&&MailService.ready()
                &&limiter.allow("email:"+email.toLowerCase(Locale.ROOT),3,900000)) {
                final String target=email;
                try {mailQueue.execute(()->send(target));} catch(RejectedExecutionException e){getServletContext().log("Password recovery queue busy");}
            }
            req.setAttribute("success","Nếu email hợp lệ và đã đăng ký, bạn sẽ nhận được liên kết đặt lại mật khẩu. Vui lòng kiểm tra cả thư rác.");
            show(req,res);return;
        }
        if(!limiter.allow("reset:"+req.getRemoteAddr(),15,900000)){res.setStatus(429);req.setAttribute("error","Bạn thử quá nhiều lần. Vui lòng thử lại sau 15 phút.");show(req,res);return;}
        String password=req.getParameter("password"),confirm=req.getParameter("confirmPassword");
        String check=ValidationUtil.validatePassword(password);
        if(check!=null||!password.equals(confirm)){req.setAttribute("error",check==null?"Mật khẩu nhập lại không khớp.":check);show(req,res);return;}
        String token=(String)req.getSession().getAttribute("resetToken");
        try(PasswordResetDAO dao=new PasswordResetDAO()) {
            if(dao.consume(token,password)) {
                req.getSession().invalidate();res.sendRedirect(req.getContextPath()+"/login?passwordChanged=1");return;
            }
            req.setAttribute("error","Liên kết không hợp lệ, hết hạn hoặc đã sử dụng. Hãy gửi yêu cầu mới.");
        } catch(IllegalArgumentException e){req.setAttribute("error",e.getMessage());}
        catch(SQLException e){getServletContext().log("Reset failed; state="+e.getSQLState());req.setAttribute("error","Chưa thể đổi mật khẩu. Vui lòng thử lại.");}
        show(req,res);
    }
    private void send(String email) {
        PasswordResetDAO.Delivery delivery=null;
        try {
            try(PasswordResetDAO dao=new PasswordResetDAO()){delivery=dao.issue(email);}
            if(delivery!=null)MailService.sendReset(delivery.getEmail(),delivery.getToken());
        } catch(Exception e) {
            // Do not log addresses, raw tokens, reset URLs or mail credentials.
            getServletContext().log("Password recovery delivery failed. Check SMTP and database configuration.");
            if(delivery!=null)try(PasswordResetDAO dao=new PasswordResetDAO()){dao.revoke(delivery.getToken());}catch(SQLException ignored){}
        }
    }
}
