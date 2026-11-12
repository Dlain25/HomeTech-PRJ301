package filter;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import util.ResetTokenUtil;

@WebFilter(urlPatterns="/*",dispatcherTypes=DispatcherType.REQUEST)
public class RequestSecurityFilter implements Filter {
    @Override public void doFilter(ServletRequest rq,ServletResponse rp,FilterChain chain) throws IOException,ServletException {
        HttpServletRequest req=(HttpServletRequest)rq;HttpServletResponse res=(HttpServletResponse)rp;
        req.setCharacterEncoding("UTF-8");res.setCharacterEncoding("UTF-8");
        res.setHeader("X-Content-Type-Options","nosniff");res.setHeader("X-Frame-Options","SAMEORIGIN");
        // Servlet path is decoded and excludes path parameters such as ;jsessionid.
        String path=req.getServletPath();
        if(path.startsWith("/assets/")){chain.doFilter(req,res);return;}
        if(path.startsWith("/views/")&&!path.startsWith("/views/error/")){res.sendError(404);return;}
        HttpSession session=req.getSession();
        synchronized(session){if(session.getAttribute("csrfToken")==null)session.setAttribute("csrfToken",ResetTokenUtil.generate());}
        if("POST".equals(req.getMethod())) {
            String sent=req.getParameter("csrfToken"),expected=(String)session.getAttribute("csrfToken");
            if(sent==null||!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),sent.getBytes(StandardCharsets.UTF_8))) {
                res.sendError(403,"Phiên biểu mẫu không hợp lệ. Hãy tải lại trang và thử lại.");return;
            }
        }
        chain.doFilter(req,res);
    }
}
