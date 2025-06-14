package tw.shawn.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import okhttp3.OkHttpClient;
import tw.shawn.util.DBUtil;

import java.util.concurrent.TimeUnit;

/**
 * Web 應用程式啟動與關閉時的監聽器，用來初始化與釋放資源。
 */
@WebListener
public class DBCleanupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        // ✅ 建立共用 OkHttpClient（可被 GPT servlet 使用）
        OkHttpClient sharedClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        context.setAttribute("sharedOkHttpClient", sharedClient);
        System.out.println("🚀 WebApp 啟動完成，已註冊共用 OkHttpClient");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        System.out.println("🧹 WebApp 正在關閉，釋放 JDBC 與 HTTP 資源...");

        // ✅ 清除 JDBC Driver
        DBUtil.cleanupDriver();

        // ✅ 清除共用 OkHttpClient 連線池
        Object clientObj = context.getAttribute("sharedOkHttpClient");
        if (clientObj instanceof OkHttpClient okHttpClient) {
            okHttpClient.connectionPool().evictAll();
            System.out.println("✅ OkHttpClient connection pool 已清除");
        }
    }
}
