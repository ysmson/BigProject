package tw.shawn.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import tw.shawn.dao.QuizResultDAO;
import tw.shawn.model.QuizResultSummary;
import tw.shawn.util.DBUtil;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

/**
 * QuizSummaryServlet：回傳使用者對每部影片的整體測驗統計資料
 * 對應路徑 /api/quizSummary?userId=xxx
 */
@WebServlet("/api/quizSummary")
public class QuizSummaryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        try {
            int userId = Integer.parseInt(req.getParameter("userId"));
            try (Connection conn = DBUtil.getConnection()) {
                QuizResultDAO dao = new QuizResultDAO(conn);
                List<QuizResultSummary> list = dao.getQuizSummaryByUser(userId); // ✅ 修正名稱

                JsonArray arr = new JsonArray();
                for (QuizResultSummary q : list) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("title", q.getVideoTitle()); // ✅ 加上影片名稱欄位
                    obj.addProperty("videoId", q.getVideoId());
                    obj.addProperty("source", q.getSource());
                    obj.addProperty("totalQuizCount", q.getTotalQuizCount());
                    obj.addProperty("totalQuestions", q.getTotal());
                    obj.addProperty("correctAnswers", q.getCorrect());
                    arr.add(obj);
                }

                resp.getWriter().write(arr.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"統計資料查詢失敗\"}");
        }
    }
}
//         } catch (NumberFormatException e) {