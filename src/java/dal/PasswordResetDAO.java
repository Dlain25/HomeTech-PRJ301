package dal;

import java.sql.*;
import util.PasswordUtil;
import util.ResetTokenUtil;
import util.ValidationUtil;

/** DB operations only; SMTP and HTTP rate limits belong outside this DAO. */
public final class PasswordResetDAO extends BaseDAO {
    public PasswordResetDAO() throws SQLException { super(); }
    public static final class Delivery {
        private final String email;
        private final String token;
        Delivery(String email, String token) { this.email=email; this.token=token; }
        public String getEmail() { return email; }
        public String getToken() { return token; }
        // No toString(): never accidentally log the reset URL/token.
    }

    /** null means nonexistent/locked user or cooldown. Public response stays identical. */
    public Delivery issue(String email) throws SQLException {
        if (ValidationUtil.validateEmail(email)!=null) return null;
        if (!connection.getAutoCommit()) throw new SQLException("Use a dedicated DAO connection");
        connection.setAutoCommit(false);
        try {
            int userId;
            String canonicalEmail;
            try (PreparedStatement ps=connection.prepareStatement(
                    "SELECT user_id,email FROM dbo.Users WITH (UPDLOCK,HOLDLOCK) WHERE email=? AND status='ACTIVE'")) {
                ps.setString(1,email.trim());
                try (ResultSet rs=ps.executeQuery()) {
                    if (!rs.next()) { connection.commit(); return null; }
                    userId=rs.getInt(1); canonicalEmail=rs.getString(2);
                }
            }
            try (PreparedStatement ps=connection.prepareStatement(
                    "SELECT COUNT(*) FROM dbo.PasswordResetTokens WHERE user_id=? "
                    + "AND created_at>DATEADD(MINUTE,-1,SYSUTCDATETIME())")) {
                ps.setInt(1,userId);
                try (ResultSet rs=ps.executeQuery()) {
                    rs.next(); if (rs.getInt(1)>0) { connection.commit(); return null; }
                }
            }
            invalidateAll(userId);
            String raw=ResetTokenUtil.generate();
            try (PreparedStatement ps=connection.prepareStatement(
                    "INSERT dbo.PasswordResetTokens(user_id,token_hash,expires_at) "
                    + "VALUES(?,?,DATEADD(MINUTE,15,SYSUTCDATETIME()))")) {
                ps.setInt(1,userId); ps.setString(2,ResetTokenUtil.sha256(raw)); ps.executeUpdate();
            }
            connection.commit();
            return new Delivery(canonicalEmail,raw);
        } catch (SQLException | RuntimeException e) {
            try { connection.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
            throw e;
        } finally { connection.setAutoCommit(true); }
    }

    /** Atomically changes password + consumes token + revokes other reset tokens/sessions. */
    public boolean consume(String rawToken, String password) throws SQLException {
        if (!ResetTokenUtil.validFormat(rawToken)) return false;
        String error=ValidationUtil.validatePassword(password);
        if (error!=null) throw new IllegalArgumentException(error);
        String digest=ResetTokenUtil.sha256(rawToken);
        int userId;
        try (PreparedStatement ps=connection.prepareStatement(
                "SELECT user_id FROM dbo.PasswordResetTokens WHERE token_hash=?")) {
            ps.setString(1,digest);
            try (ResultSet rs=ps.executeQuery()) { if (!rs.next()) return false; userId=rs.getInt(1); }
        }
        // Compute expensive password hash outside DB locks; validity is rechecked under locks.
        String newHash=PasswordUtil.hash(password);
        if (!connection.getAutoCommit()) throw new SQLException("Use a dedicated DAO connection");
        connection.setAutoCommit(false);
        try {
            // Lock in the same Users -> tokens order as issue().
            try (PreparedStatement ps=connection.prepareStatement(
                    "SELECT username FROM dbo.Users WITH (UPDLOCK,HOLDLOCK) WHERE user_id=? AND status='ACTIVE'")) {
                ps.setInt(1,userId);
                try (ResultSet rs=ps.executeQuery()) {
                    if (!rs.next()) { connection.commit(); return false; }
                    error=ValidationUtil.validatePassword(password,rs.getString(1));
                    if (error!=null) throw new IllegalArgumentException(error);
                }
            }
            try (PreparedStatement ps=connection.prepareStatement(
                    "UPDATE dbo.PasswordResetTokens SET used_at=SYSUTCDATETIME() "
                    + "WHERE user_id=? AND token_hash=? AND used_at IS NULL AND expires_at>SYSUTCDATETIME()")) {
                ps.setInt(1,userId); ps.setString(2,digest);
                if (ps.executeUpdate()!=1) { connection.rollback(); return false; }
            }
            try (PreparedStatement ps=connection.prepareStatement(
                    "UPDATE dbo.Users SET password_hash=?,auth_version=auth_version+1,updated_at=SYSUTCDATETIME() WHERE user_id=?")) {
                ps.setString(1,newHash); ps.setInt(2,userId); ps.executeUpdate();
            }
            invalidateAll(userId);
            connection.commit();
            return true;
        } catch (SQLException | RuntimeException e) {
            try { connection.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
            throw e;
        } finally { connection.setAutoCommit(true); }
    }
    /** Call after a failed mail delivery; do not log rawToken. */
    public void revoke(String rawToken) throws SQLException {
        if (!ResetTokenUtil.validFormat(rawToken)) return;
        try (PreparedStatement ps=connection.prepareStatement(
                "UPDATE dbo.PasswordResetTokens SET used_at=SYSUTCDATETIME() WHERE token_hash=? AND used_at IS NULL")) {
            ps.setString(1,ResetTokenUtil.sha256(rawToken)); ps.executeUpdate();
        }
    }
    private void invalidateAll(int userId) throws SQLException {
        try (PreparedStatement ps=connection.prepareStatement(
                "UPDATE dbo.PasswordResetTokens SET used_at=SYSUTCDATETIME() WHERE user_id=? AND used_at IS NULL")) {
            ps.setInt(1,userId); ps.executeUpdate();
        }
    }
}
