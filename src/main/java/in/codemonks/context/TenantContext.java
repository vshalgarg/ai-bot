package in.codemonks.context;

public class TenantContext {
    private static final ThreadLocal<String> tenantHolder = new ThreadLocal<>();

    public static String getTenantId() {
        return tenantHolder.get();
    }
    public static void setTenantId(String tenantId) {
        tenantHolder.set(tenantId);
    }
}
