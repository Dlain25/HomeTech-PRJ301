package util;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.net.URI;
import java.util.Properties;

public final class MailService {
    private MailService() { }
    public static boolean ready() {
        try { settings();return true; }catch(RuntimeException e){return false;}
    }
    private static Properties settings() {
        String host=FeatureConfig.required("HOMETECH_SMTP_HOST");
        String port=FeatureConfig.required("HOMETECH_SMTP_PORT");
        int parsed=Integer.parseInt(port);if(parsed<1||parsed>65535)throw new IllegalArgumentException("SMTP port");
        FeatureConfig.required("HOMETECH_SMTP_USER");FeatureConfig.required("HOMETECH_SMTP_PASSWORD");FeatureConfig.required("HOMETECH_MAIL_FROM");
        baseUrl();
        String mode=FeatureConfig.get("HOMETECH_SMTP_MODE");
        if(mode.isEmpty())mode="STARTTLS";
        if(!mode.equals("STARTTLS")&&!mode.equals("SSL"))throw new IllegalArgumentException("SMTP mode");
        Properties p=new Properties();p.setProperty("mail.smtp.host",host);p.setProperty("mail.smtp.port",port);
        p.setProperty("mail.smtp.auth","true");p.setProperty("mail.smtp.ssl.checkserveridentity","true");
        p.setProperty("mail.smtp.connectiontimeout","5000");p.setProperty("mail.smtp.timeout","10000");p.setProperty("mail.smtp.writetimeout","10000");
        if(mode.equals("SSL"))p.setProperty("mail.smtp.ssl.enable","true");
        else{p.setProperty("mail.smtp.starttls.enable","true");p.setProperty("mail.smtp.starttls.required","true");}
        return p;
    }
    private static String baseUrl() {
        String base=FeatureConfig.required("HOMETECH_APP_BASE_URL").replaceAll("/+$","");
        URI u=URI.create(base);
        boolean local="localhost".equals(u.getHost())||"127.0.0.1".equals(u.getHost());
        if(u.getHost()==null||u.getUserInfo()!=null||u.getQuery()!=null||u.getFragment()!=null
            ||!("https".equals(u.getScheme()) || (local&&"http".equals(u.getScheme()))))throw new IllegalArgumentException("Invalid public app URL");
        return base;
    }
    public static void sendReset(String email,String token) throws MessagingException {
        if(!ResetTokenUtil.validFormat(token))throw new IllegalArgumentException("Invalid token");
        Properties p=settings();
        String user=FeatureConfig.required("HOMETECH_SMTP_USER"),password=FeatureConfig.required("HOMETECH_SMTP_PASSWORD");
        Session session=Session.getInstance(p,new Authenticator(){@Override protected PasswordAuthentication getPasswordAuthentication(){return new PasswordAuthentication(user,password);}});
        MimeMessage message=new MimeMessage(session);
        message.setFrom(new InternetAddress(FeatureConfig.required("HOMETECH_MAIL_FROM"),true));
        message.setRecipient(Message.RecipientType.TO,new InternetAddress(email,true));
        message.setSubject("Đặt lại mật khẩu HomeTech","UTF-8");
        message.setText("Bạn đã yêu cầu đặt lại mật khẩu HomeTech. Liên kết có hiệu lực 15 phút và chỉ dùng một lần:\n\n"+baseUrl()+"/reset-password?token="+token+"\n\nNếu không yêu cầu, bạn có thể bỏ qua email này. Không chia sẻ liên kết cho người khác.","UTF-8");
        Transport.send(message);
    }
}
