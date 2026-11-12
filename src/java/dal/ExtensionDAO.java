package dal;

import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

/** Request-scoped DAO: never share a connection between servlet requests. */
public class ExtensionDAO extends BaseDAO {
    public ExtensionDAO() throws SQLException { super(); }
    @FunctionalInterface protected interface Task<T> { T run() throws SQLException; }
    protected <T> T transaction(Task<T> task) throws SQLException {
        if (!connection.getAutoCommit()) throw new SQLException("Nested transaction is not supported");
        connection.setAutoCommit(false);
        try { T result=task.run(); connection.commit(); return result; }
        catch (SQLException | RuntimeException e) {
            try { connection.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
            throw e;
        } finally { connection.setAutoCommit(true); }
    }
    private void bind(PreparedStatement ps,Object... args) throws SQLException {
        for(int i=0;i<args.length;i++) ps.setObject(i+1,args[i]);
    }
    protected List<Map<String,Object>> query(String sql,Object... args) throws SQLException {
        List<Map<String,Object>> rows=new ArrayList<>();
        try(PreparedStatement ps=connection.prepareStatement(sql)) {
            bind(ps,args);
            try(ResultSet rs=ps.executeQuery()) {
                ResultSetMetaData md=rs.getMetaData();
                while(rs.next()) {
                    Map<String,Object> row=new LinkedHashMap<>();
                    for(int i=1;i<=md.getColumnCount();i++) row.put(md.getColumnLabel(i),rs.getObject(i));
                    // Keep original UTC timestamp for business rules; expose Vietnam display separately.
                    for(String key:new ArrayList<>(row.keySet())) if(row.get(key) instanceof Timestamp) {
                        row.put(key+"_text",((Timestamp)row.get(key)).toLocalDateTime().plusHours(7)
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }
    protected Map<String,Object> one(String sql,Object... args) throws SQLException {
        List<Map<String,Object>> rows=query(sql,args);
        require(rows.size()==1,"Không tìm thấy dữ liệu hoặc dữ liệu không duy nhất.","HT404");
        return rows.get(0);
    }
    protected int update(String sql,Object... args) throws SQLException {
        try(PreparedStatement ps=connection.prepareStatement(sql)) { bind(ps,args); return ps.executeUpdate(); }
    }
    protected long insert(String sql,Object... args) throws SQLException {
        try(PreparedStatement ps=connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            bind(ps,args); require(ps.executeUpdate()==1,"Không thể tạo yêu cầu.","HT409");
            try(ResultSet rs=ps.getGeneratedKeys()) {
                if(rs.next()) return rs.getLong(1); throw new SQLException("Missing generated key");
            }
        }
    }
    protected void permission(int actor,String code) throws SQLException {
        PaymentReviewDAO.requirePermission(connection,actor,code);
    }
    protected void activeUser(int user) throws SQLException {
        require(!query("SELECT user_id FROM dbo.Users WITH (UPDLOCK,HOLDLOCK) WHERE user_id=? AND status='ACTIVE'",user).isEmpty(),
            "Tài khoản không khả dụng.","HT403");
    }
    public Set<String> permissions(int user) throws SQLException {
        Set<String> result=new HashSet<>();
        for(Map<String,Object> r:query("SELECT a.permission_code FROM dbo.AdminPermissions a JOIN dbo.Users u ON u.user_id=a.user_id JOIN dbo.Roles r ON r.role_id=u.role_id WHERE u.user_id=? AND u.status='ACTIVE' AND r.role_name='ADMIN'",user))
            result.add((String)r.get("permission_code"));
        return result;
    }
    public static void require(boolean condition,String message,String state) throws SQLException {
        if(!condition) throw new SQLException(message,state);
    }
    protected long number(Map<String,Object> r,String field) { return ((Number)r.get(field)).longValue(); }
    protected BigDecimal amount(Map<String,Object> r,String field) { return (BigDecimal)r.get(field); }
}
