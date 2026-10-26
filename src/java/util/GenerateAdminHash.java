
package util;

import java.io.Console;
import java.util.Arrays;

public class GenerateAdminHash {

    public static void main(String[] args) {
        Console console = System.console();

        if (console == null) {
            System.err.println(
                "Hay chay chuong trinh trong terminal de nhap mat khau an."
            );
            return;
        }

        char[] password = console.readPassword("Nhap mat khau Admin: ");

        if (password == null || password.length == 0) {
            System.err.println("Mat khau khong duoc de trong.");
            return;
        }

        try {
            System.out.println(PasswordUtil.hash(new String(password)));
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
