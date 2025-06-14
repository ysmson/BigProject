 package tw.shawn.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import tw.shawn.model.QuizResult;
import tw.shawn.model.QuizResultSummary;

/**
 * QuizResultDAO：負責處理 quiz_results 資料表的寫入與查詢邏輯
 */
public class QuizResultDAO {
    private final Connection conn;

    public QuizResultDAO(Connection conn) {
        this.conn = conn;
    }

    // ✅ 新增測驗結果
    public void insertQuizResult(int userId, String videoId, int correctCount, int totalCount, String source, int attemptId) throws SQLException {
    	String sql = "INSERT INTO quiz_results (user_id, video_id, correct_answers, total_questions, submitted_at, source, attempt_id) VALUES (?, ?, ?, ?, NOW(), ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        	stmt.setInt(1, userId);
        	stmt.setString(2, videoId);
        	stmt.setInt(3, correctCount);
        	stmt.setInt(4, totalCount);
        	stmt.setString(5, source);
        	stmt.setInt(6, attemptId);

            stmt.executeUpdate();
        }
    }

    // ✅ 取得某影片的最新一次結果
    public QuizResult getLatestQuizResult(int userId, String videoId) throws SQLException {
        String sql = "SELECT * FROM quiz_results WHERE user_id = ? AND video_id = ? ORDER BY submitted_at DESC LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, videoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                QuizResult result = new QuizResult();
                result.setUserId(userId);
                result.setVideoId(rs.getString("video_id"));
                result.setCorrectAnswers(rs.getInt("correct_answers"));
                result.setTotalQuestions(rs.getInt("total_questions"));
                result.setSubmittedAt(rs.getTimestamp("submitted_at").toLocalDateTime());
                result.setSource(rs.getString("source")); // ✅ 記得設
                result.setAttemptId(rs.getInt("attempt_id"));

                return result;
            }
        }
        return null;
    }

    // ✅ 查詢全部結果（依時間排序）
    public List<QuizResult> getAllResultsByUser(int userId) throws SQLException {
        List<QuizResult> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_results WHERE user_id = ? ORDER BY submitted_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                QuizResult result = new QuizResult();
                result.setUserId(userId);
                result.setVideoId(rs.getString("video_id"));
                result.setCorrectAnswers(rs.getInt("correct_answers"));
                result.setTotalQuestions(rs.getInt("total_questions"));
                result.setSubmittedAt(rs.getTimestamp("submitted_at").toLocalDateTime());
                result.setSource(rs.getString("source")); // ✅ 補這行
                result.setAttemptId(rs.getInt("attempt_id")); // ✅ 補上這行！

                list.add(result);
            }
        }
        return list;
    }

    public List<QuizResult> getQuizResultsByUser(int userId) throws SQLException {
        return getAllResultsByUser(userId);
    }
    public List<QuizResultSummary> getQuizSummaryByUser(int userId) throws SQLException {
        List<QuizResultSummary> list = new ArrayList<>();

        String sql = """
        	    SELECT 
        	        q.video_id,
        	        v.title AS video_title,
        	        CASE 
        	          WHEN COUNT(DISTINCT q.source) = 1 THEN MAX(q.source)
        	          ELSE '混合'
        	        END AS source,
        	        COUNT(*) AS total_quiz_count,
        	        SUM(q.total_questions) AS total,
        	        SUM(q.correct_answers) AS correct
        	    FROM quiz_results q
        	    JOIN video v ON q.video_id = v.video_id
        	    WHERE q.user_id = ?
        	    GROUP BY q.video_id, v.title;
        	""";


        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
            	QuizResultSummary summary = new QuizResultSummary();
            	summary.setVideoId(rs.getString("video_id"));
            	summary.setVideoTitle(rs.getString("video_title")); // ✅ 加上標題
            	summary.setSource(rs.getString("source"));
            	summary.setTotalQuizCount(rs.getInt("total_quiz_count"));
            	summary.setTotal(rs.getInt("total"));
            	summary.setCorrect(rs.getInt("correct"));
            	list.add(summary);

            }
        }

        return list;
    }
    public List<QuizResult> getResultsByUser(int userId) throws SQLException {
        return getAllResultsByUser(userId);
    }

}
