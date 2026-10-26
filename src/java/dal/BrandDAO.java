package dal;
import java.sql.*;
import java.util.*;
import model.Brand;

public class BrandDAO {
    private final PublicConnectionProvider connections;
    public BrandDAO() { this(PublicDatabase::open); }
    public BrandDAO(PublicConnectionProvider connections) { this.connections = connections; }
    public List<Brand> findActive() throws SQLException {
        List<Brand> items = new ArrayList<>();
        try (Connection c = connections.open();
             PreparedStatement s = c.prepareStatement("SELECT brand_id, brand_name FROM dbo.Brands WHERE is_active = 1 ORDER BY brand_name")) {
            s.setQueryTimeout(10);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    Brand item = new Brand();
                    item.setBrandId(r.getInt("brand_id")); item.setBrandName(r.getString("brand_name")); items.add(item);
                }
            }
        }
        return items;
    }
}

