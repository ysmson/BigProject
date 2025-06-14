package tw.shawn.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import tw.shawn.dao.AnswerDAO;
import tw.shawn.dao.QuizResultDAO;
import tw.shawn.model.Answer;
import tw.shawn.model.QuizResult;
import tw.shawn.util.DBUtil;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@WebServlet("/api/quizHistoryDetail")
public class QuizHistoryDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        try {
            int userId = Integer.parseInt(req.getParameter("userId"));

            try (Connection conn = DBUtil.getConnection()) {
                QuizResultDAO resultDAO = new QuizResultDAO(conn);
                AnswerDAO answerDAO = new AnswerDAO(conn);

                List<QuizResult> results = resultDAO.getResultsByUser(userId);
                System.out.println("✅ 取得測驗結果數量: " + results.size());

                JsonArray jsonArr = new JsonArray();

                for (QuizResult result : results) {
                    List<Answer> answers = answerDAO.getAnswersByUserAndAttemptId(
                        userId, result.getVideoId(), result.getAttemptId()
                    );
                    System.out.println("📘 取得回答數量（videoId=" + result.getVideoId()
                            + ", attemptId=" + result.getAttemptId() + "）: " + answers.size());

                    JsonObject record = new JsonObject();
                    record.addProperty("videoTitle", answerDAO.getVideoTitle(result.getVideoId()));
                    record.addProperty("submittedAt", result.getSubmittedAt().toString());
                    record.addProperty("videoId", result.getVideoId());
                    record.addProperty("source", result.getSource());
                    record.addProperty("total", result.getTotalQuestions());
                    record.addProperty("correct", result.getCorrectAnswers());

                    JsonArray answerArr = new JsonArray();
                    for (Answer a : answers) {
                        JsonObject ans = new JsonObject();
                        ans.addProperty("question", a.getQuestion());
                        ans.addProperty("selectedAnswer", a.getOptionTextByIndex(a.getSelectedOption()));
                        ans.addProperty("correctAnswer", a.getAnswer());
                        ans.addProperty("isCorrect", a.isCorrect());
                        ans.addProperty("explanation", a.getExplanation());
                        ans.addProperty("option1", a.getOption1());
                        ans.addProperty("option2", a.getOption2());
                        ans.addProperty("option3", a.getOption3());
                        ans.addProperty("option4", a.getOption4());
                        ans.addProperty("correctIndex", a.getAnswerIndex());
                        ans.addProperty("selectedIndex", a.getSelectedOption());
                        answerArr.add(ans);
                    }

                    record.add("answers", answerArr);
                    jsonArr.add(record);
                }

                resp.getWriter().write(jsonArr.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            JsonObject error = new JsonObject();
            error.addProperty("error", "伺服器處理錯誤：" + e.getMessage());
            resp.getWriter().write(error.toString());
        }
    }
}
