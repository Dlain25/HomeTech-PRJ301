package integration;

import dal.*;
import model.*;
import util.PasswordUtil;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/** Exercises real DAOs in one rollback-only SQL Server transaction.
 * Only generated test records are changed. Identity sequences may advance.
 * The test driver is never compiled into the application WAR. */
public final class DatabaseIntegrationChecks {
    private static int checks;
    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        checks++;
    }
    public static void main(String[] args) throws Exception {
        try {
            run();
        } catch (SQLException e) {
            System.err.println("FAIL: SQLState="+e.getSQLState()+", code="+e.getErrorCode());
            System.exit(1);
        }
    }
    private static void run() throws Exception {
        String oldUrl = System.getProperty("hometech.db.url");
        List<Driver> original = Collections.list(DriverManager.getDrivers());
        try (Connection real = DBContext.getConnection()) {
            // DBContext loads the JDBC driver on the first connection.
            original = Collections.list(DriverManager.getDrivers());
            Map<String, Long> before = counts(real);
            real.setAutoCommit(false);
            Driver testDriver = new RollbackDriver(real);
            for (Driver driver : original) DriverManager.deregisterDriver(driver);
            DriverManager.registerDriver(testDriver);
            System.setProperty("hometech.db.url", "jdbc:sqlserver:rollback-integration-test");
            try {
                exercise(real);
            } finally {
                real.rollback();
                DriverManager.deregisterDriver(testDriver);
                for (Driver driver : original) DriverManager.registerDriver(driver);
                if (oldUrl == null) System.clearProperty("hometech.db.url");
                else System.setProperty("hometech.db.url", oldUrl);
            }
            check(before.equals(counts(real)), "Rollback restores table row counts");
            real.rollback();
        }
        System.out.println("PASS: "+checks+" SQL Server integration checks; test rows rolled back.");
    }
    private static Map<String,Long> counts(Connection c) throws SQLException {
        Map<String,Long> result = new TreeMap<>();
        for (String table : List.of("Users","Categories","Products","ProductVariants","Inventory",
                "ProductImages","Carts","CartItems","Orders","OrderItems","Payments")) {
            try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT_BIG(*) FROM dbo."+table)) {
                r.next(); result.put(table,r.getLong(1));
            }
        }
        return result;
    }
    private static void exercise(Connection real) throws Exception {
        String key = "it_"+UUID.randomUUID().toString().replace("-", "").substring(0,16);
        String secret = UUID.randomUUID().toString();
        User u = new User();
        u.setUsername(key); u.setEmail(key+"@example.invalid");
        u.setFullName("Kiểm thử tích hợp"); u.setPhone("0900000000");
        u.setPasswordHash(PasswordUtil.hash(secret));
        UserDAO users = new UserDAO();
        int userId = users.insertCustomer(u);
        check(userId > 0, "Register customer");
        check(users.existsByUsername(key) && users.existsByEmail(u.getEmail()), "Duplicate account lookup");
        check(PasswordUtil.verify(secret, users.findByUsername(key).getPasswordHash()), "Login hash verification");
        check(!PasswordUtil.verify(secret+"x", users.findByUsername(key).getPasswordHash()), "Reject wrong password");
        check(users.updateProfile(userId,"Khách thử nghiệm",u.getEmail(),u.getPhone()), "Profile update");
        check(users.findById(userId).getFullName().equals("Khách thử nghiệm"), "Unicode profile roundtrip");
        check(!users.existsByEmailExcept(u.getEmail(),userId), "Own email accepted");
        check(users.findByEmail(u.getEmail()) != null && !users.search(key).isEmpty(), "Email and admin user search");
        String nextSecret = UUID.randomUUID().toString();
        check(users.updatePassword(userId,PasswordUtil.hash(nextSecret)), "Password update");
        check(PasswordUtil.verify(nextSecret,users.findById(userId).getPasswordHash()), "New password usable");
        check(users.updateStatus(userId,"LOCKED") && users.findById(userId).isLocked(), "Admin lock account");
        check(users.updateStatus(userId,"ACTIVE"), "Admin unlock account");
        check(users.updateRole(userId,1) && users.findById(userId).isAdmin(), "Admin role change");
        check(users.updateRole(userId,2), "Restore customer test role");

        CategoryDAO categories = new CategoryDAO();
        Category category = new Category(); category.setCategoryName(key); category.setDescription("Danh mục thử"); category.setActive(true);
        categories.insert(category);
        category = categories.getAll().stream().filter(c -> key.equals(c.getCategoryName())).findFirst().orElseThrow();
        int categoryId = category.getCategoryId();
        check(categories.findActive().stream().anyMatch(c -> c.getCategoryId()==categoryId), "Admin category visible in catalog");
        category.setDescription("Đã cập nhật"); categories.update(category);
        check(categories.getById(categoryId).getDescription().equals("Đã cập nhật"), "Category update");
        int brandId = new BrandDAO().findActive().get(0).getBrandId();
        try (AdminProductDAO admin = new AdminProductDAO()) {
            int productId = admin.addProduct(categoryId,brandId,key,"Sản phẩm kiểm thử","Mô tả",new BigDecimal("1234567"),10,12,"/assets/images/product-placeholder.svg");
            ProductDAO catalog = new ProductDAO();
            Product p = catalog.findActive(productId);
            check(p != null && p.getProductName().equals("Sản phẩm kiểm thử"), "Admin product visible in public detail");
            ProductVariant variant = catalog.variants(productId).get(0);
            int variantId = variant.getVariantId();
            check(variant.getQuantityAvailable()==10 && !catalog.images(productId).isEmpty(), "Variant inventory and image created");
            check(admin.updateProduct(productId,categoryId,brandId,key,"Sản phẩm đã sửa","Mô tả",new BigDecimal("1234567"),24), "Update product");
            check(admin.updateVariant(productId,variantId,key+"-STD","Tiêu chuẩn","Trắng",new BigDecimal("1200000"),true), "Update variant price");
            check(catalog.findActive(productId).getDisplayPrice().compareTo(new BigDecimal("1200000"))==0, "Public price follows admin variant price");
            check(admin.updateStock(productId,variantId,8) && catalog.variants(productId).get(0).getQuantityAvailable()==8,"Stock update visible");
            admin.setPrimaryImage(productId,"assets/images/product-placeholder.svg");
            check(catalog.images(productId).size()==2,"Image history retained");
            check(admin.setProductStatus(productId,false) && catalog.findActive(productId)==null,"Hide inactive product");
            admin.setProductStatus(productId,true);
            category.setActive(false); categories.update(category);
            check(catalog.findActive(productId)==null,"Hidden category hides product");
            category.setActive(true); categories.update(category);
            try (CartDAO cart = new CartDAO(); OrderDAO orders = new OrderDAO()) {
                check(cart.addItem(userId,variantId,2)==null,"Add catalog variant to cart");
                CartItem item = cart.getItems(userId).get(0);
                check(item.getQuantity()==2 && item.getLineTotal().compareTo(new BigDecimal("2400000"))==0,"Cart price from database");
                check(cart.addItem(userId,variantId,Integer.MAX_VALUE)!=null,"Reject overflowing cart quantity");
                check(cart.updateQuantity(userId,item.getCartItemId(),-1)!=null && cart.getItems(userId).size()==1,"Invalid quantity does not delete cart item");
                check(cart.updateQuantity(userId,item.getCartItemId(),3)==null,"Update cart quantity");
                check(cart.updateQuantity(userId,item.getCartItemId(),9)!=null,"Reject quantity above stock");
                check(!cart.removeItem(-1,item.getCartItemId()),"Cart ownership enforced");
                long orderId = orders.createOrder(userId,"Khách thử nghiệm","0900000000","Địa chỉ kiểm thử","","COD");
                Order order = orders.findByIdAndUser(orderId,userId);
                check(order != null && order.getItems().size()==1 && order.getTotalAmount().compareTo(new BigDecimal("3600000"))==0,"Order, items, computed totals");
                check("COD".equals(order.getPaymentMethod()) && "PENDING".equals(order.getPaymentStatus()),"Payment record");
                check(cart.getItems(userId).isEmpty() && catalog.variants(productId).get(0).getQuantityAvailable()==5,"Checkout clears cart and decrements stock");
                check(orders.findByUser(userId).size()==1 && orders.findByIdAndUser(orderId,-1)==null,"Order history and ownership");
                orders.cancelOrder(orderId,userId);
                check("CANCELLED".equals(orders.findByIdAndUser(orderId,userId).getOrderStatus()) && catalog.variants(productId).get(0).getQuantityAvailable()==8,"Cancel restores stock");
                check("FAILED".equals(orders.findByIdAndUser(orderId,userId).getPaymentStatus()),"Cancel payment status");
                check(cart.addItem(userId,variantId,1)==null,"Cart reusable after checkout");
                check(cart.removeItem(userId,cart.getItems(userId).get(0).getCartItemId()),"Remove cart item");
            }
            check(admin.getById(productId)!=null && !admin.getAll().isEmpty() && !admin.getCategories().isEmpty() && !admin.getBrands().isEmpty(),"Admin product reads");
        }
        DashboardDAO dashboard = new DashboardDAO();
        check(dashboard.getTotalProducts()>0 && dashboard.getTotalUsers()>0 && dashboard.getTotalOrders()>0,"Dashboard counts");
        check(dashboard.getTotalRevenue()!=null && dashboard.getMonthlyRevenue()!=null && dashboard.getTopSellingProducts()!=null,"Dashboard revenue queries");
    }
    private static final class RollbackDriver implements Driver {
        private final Connection real;
        RollbackDriver(Connection real) { this.real=real; }
        @Override public Connection connect(String url, Properties info) throws SQLException {
            if (!acceptsURL(url)) return null;
            Savepoint boundary=real.setSavepoint();
            return (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},(p,m,a)->{
                switch(m.getName()) {
                    case "close", "commit", "setAutoCommit": return null;
                    case "getAutoCommit": return false;
                    case "rollback": if(a==null || a.length==0) { real.rollback(boundary); return null; } break;
                }
                try { return m.invoke(real,a); } catch(InvocationTargetException e) { throw e.getCause(); }
            });
        }
        @Override public boolean acceptsURL(String url) { return "jdbc:sqlserver:rollback-integration-test".equals(url); }
        @Override public DriverPropertyInfo[] getPropertyInfo(String u,Properties p) { return new DriverPropertyInfo[0]; }
        @Override public int getMajorVersion() { return 1; }
        @Override public int getMinorVersion() { return 0; }
        @Override public boolean jdbcCompliant() { return false; }
        @Override public Logger getParentLogger() { return Logger.getGlobal(); }
    }
}
