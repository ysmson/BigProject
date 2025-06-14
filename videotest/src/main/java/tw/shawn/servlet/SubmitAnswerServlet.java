package tw.shawn.servlet;

import com.google.gson.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import tw.shawn.dao.AnswerDAO;
import tw.shawn.model.Answer;
import tw.shawn.util.DBUtil;

@WebServlet("/api/submitAnswerX")
public class SubmitAnswerServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            // ✅ 先取得 JSON 並解析
            String json = request.getReader().lines().collect(Collectors.joining());
            JSONObject requestObj = new JSONObject(json);

            int userId = requestObj.getInt("userId");
            String videoId = requestObj.getString("videoId");
            int attemptId = requestObj.getInt("attemptId");
            JSONArray answersArray = requestObj.getJSONArray("answers");

            int correctCount = 0; // 🔴 統計答對題數

            try (Connection conn = DBUtil.getConnection()) {
                AnswerDAO dao = new AnswerDAO(conn);

                for (int i = 0; i < answersArray.length(); i++) {
                    JSONObject obj = answersArray.getJSONObject(i);

                    boolean isCorrect = obj.getBoolean("isCorrect");
                    if (isCorrect) {
                        correctCount++;
                    }

                    Answer answer = new Answer();
                    answer.setUserId(userId);
                    answer.setQuizId(obj.getInt("quizId"));
                    answer.setSelectedOption(obj.getInt("selectedIndex"));
                    answer.setCorrect(isCorrect);
                    answer.setSource(obj.getString("source"));
                    answer.setVideoId(videoId);
                    answer.setQuestion(obj.getString("question"));
                    answer.setOption1(obj.getString("option1"));
                    answer.setOption2(obj.getString("option2"));
                    answer.setOption3(obj.getString("option3"));
                    answer.setOption4(obj.getString("option4"));
                    answer.setAnswer(obj.getString("answer"));
                    answer.setAnswerIndex(obj.getInt("answerIndex"));
                    answer.setExplanation(obj.getString("explanation"));
                    answer.setAttemptId(attemptId);
                    
                    System.out.println("儲存答案: " + answer);

                    dao.insertAnswer(answer);
                }

                // ✅ 回傳成功訊息與正確題數
                response.getWriter().write(
                    new JSONObject()
                        .put("status", "success")
                        .put("correctCount", correctCount)
                        .toString()
                );
            } catch (SQLException e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "資料庫操作失敗！");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "JSON 格式錯誤！");
        }
    }
}
