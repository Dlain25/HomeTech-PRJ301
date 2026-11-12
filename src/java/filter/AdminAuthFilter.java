package filter;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import model.User;
import util.AuthUtil;

@WebFilter(urlPatterns="/admin/*")
public class AdminAuthFilter implements Filter {
    @Override public void doFilter(ServletRequest rq,ServletResponse rp,FilterChain chain) throws IOException,ServletException {
        HttpServletRequest req=(HttpServletRequest)rq;HttpServletResponse res=(HttpServletResponse)rp;
        User user=AuthUtil.requireLogin(req,res);
        if(user==null)return;
        if(!user.isAdmin()){res.sendError(403,"Chỉ quản trị viên được truy cập.");return;}
        chain.doFilter(req,res);
    }
}
