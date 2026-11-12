package util;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import dal.UserDAO;
import model.User;

public final class AuthUtil {
    private AuthUtil() { }
    public static User getUser(HttpServletRequest req) {
        HttpSession s=req.getSession(false); Object u=s==null?null:s.getAttribute("user");
        return u instanceof User?(User)u:null;
    }
    public static User requireLogin(HttpServletRequest req,HttpServletResponse res) throws IOException {
        User user=getUser(req);
        if(user!=null) try(UserDAO dao=new UserDAO()) {
            User fresh=dao.getById(user.getUserId());
            Object version=req.getSession().getAttribute("authVersionAtLogin");
            if(fresh==null||!"ACTIVE".equals(fresh.getStatus())||!(version instanceof Integer)||((Integer)version)!=fresh.getAuthVersion()) {
                req.getSession().invalidate();user=null;
            } else {user=fresh;user.setPasswordHash(null);req.getSession().setAttribute("user",user);}
        } catch(SQLException e){res.sendError(503,"Chưa thể kiểm tra tài khoản. Vui lòng thử lại.");return null;}
        if(user==null) {
            if("GET".equals(req.getMethod())) {
                String next=req.getRequestURI().substring(req.getContextPath().length());
                if(req.getQueryString()!=null)next+="?"+req.getQueryString();
                req.getSession().setAttribute("redirectAfterLogin",next);
            }
            res.sendRedirect(req.getContextPath()+"/login");
        }
        return user;
    }
}
