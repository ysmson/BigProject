package tw.shawn.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import tw.shawn.dao.QuizResultDAO;
import tw.shawn.model.QuizResult;
import tw.shawn.model.QuizResultSummary;
import tw.shawn.util.DBUtil;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

/**
 * QuizHistoryServlet：查詢使用者歷史測驗結果清單
 * 對應路徑 /api/quizHistory?userId=xxx
 */
@WebServlet("/api/quizHistory")
public class QuizHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        try {
            int userId = Integer.parseInt(req.getParameter("userId"));

            try (Connection conn = DBUtil.getConnection()) {
                QuizResultDAO dao = new QuizResultDAO(conn);
                List<QuizResultSummary> resultList = dao.getQuizSummaryByUser(userId);

                JsonArray jsonArr = new JsonArray();
                for (QuizResultSummary result : resultList) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("videoId", result.getVideoId());
                    obj.addProperty("source", result.getSource());
                    obj.addProperty("totalQuizCount", result.getTotalQuizCount());
                    obj.addProperty("total", result.getTotal());
                    obj.addProperty("correct", result.getCorrect());

                    jsonArr.add(obj);
                }

                resp.getWriter().write(jsonArr.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\": \"伺服器錯誤\"}");
        }
    }
}
