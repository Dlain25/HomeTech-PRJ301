package util;

import java.util.Set;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{4,30}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^(0|\\+84)[0-9]{9}$");
    private static final Set<String> WEAK_PASSWORDS = Set.of(
            "12345678", "123456789", "11111111", "password", "password1",
            "qwerty123", "abc12345", "matkhau123");

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String validateUsername(String username) {
        if (isBlank(username)) {
            return "Vui lòng nhập tên đăng nhập";
        }
        if (!USERNAME.matcher(username.trim()).matches()) {
            return "Tên đăng nhập gồm 4-30 ký tự: chữ, số hoặc dấu gạch dưới";
        }
        return null;
    }

    public static String validatePassword(String password) {
        if (isBlank(password)) {
            return "Vui lòng nhập mật khẩu";
        }
        if (password.length() < 8 || password.length() > 64) {
            return "Mật khẩu phải từ 8 đến 64 ký tự";
        }
        if (password.contains(" ")) {
            return "Mật khẩu không được chứa khoảng trắng";
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) {
            return "Mật khẩu phải có cả chữ và số";
        }
        if (WEAK_PASSWORDS.contains(password.toLowerCase())) {
            return "Mật khẩu quá dễ đoán";
        }
        return null;
    }

    
    public static String validatePassword(String password, String username) {
        String error = validatePassword(password);
        if (error != null) {
            return error;
        }
        if (username != null && password.equalsIgnoreCase(username.trim())) {
            return "Mật khẩu không được trùng tên đăng nhập";
        }
        return null;
    }

    public static String validateFullName(String fullName) {
        if (isBlank(fullName)) {
            return "Vui lòng nhập họ tên";
        }
        if (fullName.trim().length() > 120) {
            return "Họ tên tối đa 120 ký tự";
        }
        return null;
    }

    public static String validateEmail(String email) {
        if (isBlank(email)) {
            return "Vui lòng nhập email";
        }
        String e = email.trim();
        if (e.length() > 254 || !EMAIL.matcher(e).matches()) {
            return "Email không hợp lệ";
        }
        return null;
    }

    public static String validatePhone(String phone) {
        if (isBlank(phone)) {
            return "Vui lòng nhập số điện thoại";
        }
        if (!PHONE.matcher(phone.trim()).matches()) {
            return "Số điện thoại không hợp lệ (ví dụ 0912345678)";
        }
        return null;
    }
}