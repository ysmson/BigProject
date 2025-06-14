package tw.shawn.servlet;

import com.google.gson.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import tw.shawn.dao.AnswerDAO;
import tw.shawn.dao.QuizDAO;
import tw.shawn.model.Answer;
import tw.shawn.model.Quiz;
import tw.shawn.util.DBUtil;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import tw.shawn.dao.QuizResultDAO;
import tw.shawn.model.QuizResult;

// ✅ 註冊為 Servlet，對應路徑為 /api/QuizStatsServlet
@WebServlet("/api/QuizStatsServlet")
public class QuizStatsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String videoId = req.getParameter("videoId");
        int userId = Integer.parseInt(req.getParameter("userId"));
        String source = req.getParameter("source"); // 可為 null

        try (Connection conn = DBUtil.getConnection()) {
            String sql = """
                SELECT 
                    SUM(total_questions) AS total, 
                    SUM(correct_answers) AS correct 
                FROM quiz_results 
                WHERE user_id = ? AND video_id = ?
            """;

            if (source != null && !source.isEmpty()) {
                sql += " AND source = ?";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, videoId);
                if (source != null && !source.isEmpty()) {
                    stmt.setString(3, source);
                }

                ResultSet rs = stmt.executeQuery();
                JsonObject json = new JsonObject();

                if (rs.next()) {
                    int total = rs.getInt("total");
                    int correct = rs.getInt("correct");

                    json.addProperty("total", total);
                    json.addProperty("correct", correct);
                    json.addProperty("accuracy", total > 0
                        ? String.format("%.2f", (double) correct / total * 100)
                        : "N/A");
                } else {
                    json.addProperty("total", 0);
                    json.addProperty("correct", 0);
                    json.addProperty("accuracy", "N/A");
                }

                resp.getWriter().write(new Gson().toJson(json));
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\": \"無法載入統計資料\"}");
        }
    }
}
