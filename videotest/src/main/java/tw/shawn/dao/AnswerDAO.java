	package tw.shawn.dao;
	
	import java.sql.*;
	import java.util.*;
	import java.time.LocalDateTime;
	import tw.shawn.model.Answer;
	
	public class AnswerDAO {
	
	    private final Connection conn;
	
	    public AnswerDAO(Connection conn) {
	        this.conn = conn;
	    }

	
	    public static class AnswerRecord {
	        public int quizId;
	        public int selectedIndex;
	        public int correctIndex;
	        public String question;
	        public String option1, option2, option3, option4;
	        public String source;
	        public String answer;
	        public String explanation;
	    }
	
	    public static class AttemptGroup {
	        public int attemptId;
	        public LocalDateTime submittedAt;
	        public List<AnswerRecord> questions;
	    }
	
	    public List<AnswerRecord> getAnswersByUser(int userId, String videoId) throws SQLException {
	        List<AnswerRecord> list = new ArrayList<>();
	        String sql = "SELECT a.quiz_id, a.selected_option, a.source, " +
	                "COALESCE(a.answer_index, q.correct_index, -1) AS correct_index, " +
	                "COALESCE(a.question, q.question) AS question, " +
	                "COALESCE(a.option1, q.option1) AS option1, " +
	                "COALESCE(a.option2, q.option2) AS option2, " +
	                "COALESCE(a.option3, q.option3) AS option3, " +
	                "COALESCE(a.option4, q.option4) AS option4, " +
	                "COALESCE(a.explanation, q.explanation) AS explanation, " +
	                "a.answer AS answer_text FROM answer a LEFT JOIN quiz q ON a.quiz_id = q.id " +
	                "WHERE a.user_id = ? AND a.video_id = ? ORDER BY a.id ASC";
	
	        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	            stmt.setInt(1, userId);
	            stmt.setString(2, videoId);
	            try (ResultSet rs = stmt.executeQuery()) {
	                while (rs.next()) {
	                    AnswerRecord r = new AnswerRecord();
	                    r.quizId = rs.getInt("quiz_id");
	                    r.selectedIndex = rs.getInt("selected_option");
	                    r.correctIndex = rs.getInt("correct_index");
	                    r.question = rs.getString("question");
	                    r.option1 = rs.getString("option1");
	                    r.option2 = rs.getString("option2");
	                    r.option3 = rs.getString("option3");
	                    r.option4 = rs.getString("option4");
	                    r.source = rs.getString("source");
	                    r.answer = rs.getString("answer_text");
	                    r.explanation = rs.getString("explanation");
	                    list.add(r);
	                }
	            }
	        }
	        return list;
	    }
	
	    public void insertAnswer(Answer answer) throws SQLException {
	        String sql = "INSERT INTO answer (user_id, quiz_id, selected_option, is_correct, source, " +
	                "created_at, answered_at, question, option1, option2, option3, option4, video_id, " +
	                "answer, answer_index, explanation, attempt_id) " +
	                "VALUES (?, ?, ?, ?, ?, NOW(), NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	        try (PreparedStatement ps = conn.prepareStatement(sql)) {
	            ps.setInt(1, answer.getUserId());
	            ps.setInt(2, answer.getQuizId());
	            ps.setInt(3, answer.getSelectedOption());
	            ps.setBoolean(4, answer.isCorrect());
	            ps.setString(5, answer.getSource());
	            ps.setString(6, answer.getQuestion());
	            ps.setString(7, answer.getOption1());
	            ps.setString(8, answer.getOption2());
	            ps.setString(9, answer.getOption3());
	            ps.setString(10, answer.getOption4());
	            ps.setString(11, answer.getVideoId());
	            ps.setString(12, answer.getAnswer());
	            ps.setInt(13, answer.getAnswerIndex());
	            ps.setString(14, answer.getExplanation());
	            ps.setInt(15, answer.getAttemptId()); // ✅ 新增：attempt_id 寫入
	            ps.executeUpdate();
	        }
	    }
	
	
	    public void deleteAnswersByUser(int userId, String videoId) throws SQLException {
	        String sql = "DELETE FROM answer WHERE user_id = ? AND video_id = ?";
	        try (PreparedStatement ps = conn.prepareStatement(sql)) {
	            ps.setInt(1, userId);
	            ps.setString(2, videoId);
	            ps.executeUpdate();
	        }
	    }
	
	    public List<AttemptGroup> getAnswersGroupedByAttempt(int userId, String videoId) throws SQLException {
	        List<AttemptGroup> result = new ArrayList<>();
	        String sql = """
	            SELECT a.attempt_id, a.submitted_at, a.selected_option,
	                   q.id AS quiz_id, q.question, q.correct_index,
	                   q.option1, q.option2, q.option3, q.option4
	            FROM answer a
	            JOIN quiz q ON a.quiz_id = q.id
	            WHERE a.user_id = ? AND a.video_id = ?
	            ORDER BY a.attempt_id, q.id;
	        """;
	
	        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	            stmt.setInt(1, userId);
	            stmt.setString(2, videoId);
	            ResultSet rs = stmt.executeQuery();
	
	            Map<Integer, AttemptGroup> groupMap = new LinkedHashMap<>();
	            while (rs.next()) {
	                int attemptId = rs.getInt("attempt_id");
	
	                AttemptGroup group = groupMap.computeIfAbsent(attemptId, k -> {
	                    AttemptGroup g = new AttemptGroup();
	                    g.attemptId = attemptId;
	                    try {
	                        g.submittedAt = rs.getTimestamp("submitted_at").toLocalDateTime();
	                    } catch (SQLException e) {
	                        e.printStackTrace();
	                    }
	                    g.questions = new ArrayList<>();
	                    return g;
	                });
	
	                AnswerRecord q = new AnswerRecord();
	                q.quizId = rs.getInt("quiz_id");
	                q.question = rs.getString("question");
	                q.correctIndex = rs.getInt("correct_index");
	                q.selectedIndex = rs.getInt("selected_option");
	                q.option1 = rs.getString("option1");
	                q.option2 = rs.getString("option2");
	                q.option3 = rs.getString("option3");
	                q.option4 = rs.getString("option4");
	
	                group.questions.add(q);
	            }
	            result.addAll(groupMap.values());
	        }
	        return result;
	    }
	
	    public Map<String, List<AttemptGroup>> getAnswersGroupedByUserAcrossVideos(int userId) throws SQLException {
	        String sql = """
	            SELECT a.video_id, a.attempt_id, a.submitted_at, a.selected_option,
	                   COALESCE(q.id, -1) AS quiz_id,
	                   COALESCE(q.question, a.question) AS question,
	                   COALESCE(q.correct_index, a.answer_index, -1) AS correct_index,
	                   COALESCE(q.option1, a.option1) AS option1,
	                   COALESCE(q.option2, a.option2) AS option2,
	                   COALESCE(q.option3, a.option3) AS option3,
	                   COALESCE(q.option4, a.option4) AS option4,
	                   a.explanation
	            FROM answer a
	            LEFT JOIN quiz q ON a.quiz_id = q.id
	            WHERE a.user_id = ?
	            ORDER BY a.video_id, a.attempt_id, a.id;
	        """;
	
	        Map<String, Map<Integer, AttemptGroup>> groupedMap = new LinkedHashMap<>();
	
	        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	            stmt.setInt(1, userId);
	            ResultSet rs = stmt.executeQuery();
	
	            while (rs.next()) {
	                String videoId = rs.getString("video_id");
	                int attemptId = rs.getInt("attempt_id");
	
	                groupedMap.putIfAbsent(videoId, new LinkedHashMap<>());
	                Map<Integer, AttemptGroup> attemptMap = groupedMap.get(videoId);
	
	                AttemptGroup group = attemptMap.computeIfAbsent(attemptId, k -> {
	                    AttemptGroup g = new AttemptGroup();
	                    g.attemptId = attemptId;
	                    try {
	                        g.submittedAt = rs.getTimestamp("submitted_at").toLocalDateTime();
	                    } catch (SQLException e) {
	                        e.printStackTrace();
	                    }
	                    g.questions = new ArrayList<>();
	                    return g;
	                });
	
	                AnswerRecord q = new AnswerRecord();
	                q.quizId = rs.getInt("quiz_id");
	                q.question = rs.getString("question");
	                q.correctIndex = rs.getInt("correct_index");
	                q.selectedIndex = rs.getInt("selected_option");
	                q.option1 = rs.getString("option1");
	                q.option2 = rs.getString("option2");
	                q.option3 = rs.getString("option3");
	                q.option4 = rs.getString("option4");
	                q.explanation = rs.getString("explanation");
	
	                group.questions.add(q);
	            }
	        }
	
	        Map<String, List<AttemptGroup>> result = new LinkedHashMap<>();
	        for (Map.Entry<String, Map<Integer, AttemptGroup>> entry : groupedMap.entrySet()) {
	            result.put(entry.getKey(), new ArrayList<>(entry.getValue().values()));
	        }
	        return result;
	    }
	
	
	    public String getVideoTitle(String videoId) throws SQLException {
	        String sql = "SELECT title FROM video WHERE video_id = ?";
	        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	            stmt.setString(1, videoId);
	            ResultSet rs = stmt.executeQuery();
	            if (rs.next()) {
	                return rs.getString("title");
	            }
	        }
	        return "未知影片";
	    }
	    public class AnswerGroupDTO {
	        public String videoId;
	        public String videoTitle;
	        public List<AnswerDAO.AttemptGroup> attempts;
	    }
	    public List<Answer> getAnswersByUserAndSubmittedAt(int userId, String videoId, LocalDateTime submittedAt) throws SQLException {
	        List<Answer> answers = new ArrayList<>();
	        String sql = "SELECT * FROM answer WHERE user_id = ? AND video_id = ? AND answered_at = ? ORDER BY id ASC";
	
	        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	            stmt.setInt(1, userId);
	            stmt.setString(2, videoId);
	            stmt.setTimestamp(3, Timestamp.valueOf(submittedAt));
	
	            ResultSet rs = stmt.executeQuery();
	            while (rs.next()) {
	                Answer a = new Answer();
	                a.setId(rs.getInt("id"));
	                a.setUserId(rs.getInt("user_id"));
	                a.setQuizId(rs.getInt("quiz_id"));
	                a.setSelectedOption(rs.getInt("selected_option"));
	                a.setCorrect(rs.getBoolean("is_correct")); // ✅ 資料表中的實際欄位名稱
	                a.setVideoId(rs.getString("video_id"));
	                a.setAnsweredAt(rs.getTimestamp("answered_at").toLocalDateTime());
	                a.setSource(rs.getString("source"));
	                a.setQuestion(rs.getString("question"));
	                a.setOption1(rs.getString("option1"));
	                a.setOption2(rs.getString("option2"));
	                a.setOption3(rs.getString("option3"));
	                a.setOption4(rs.getString("option4"));
	                a.setAnswer(rs.getString("answer"));
	                a.setAnswerIndex(rs.getInt("answer_index"));
	                a.setExplanation(rs.getString("explanation"));
	                answers.add(a);
	            }
	        }
	
	        return answers;
	    }
	    public List<Answer> getAnswersByUserAndAttemptId(int userId, String videoId, int attemptId) throws SQLException {
	        List<Answer> answers = new ArrayList<>();
	        
	        String sql = """
	            SELECT a.*, 
	                   COALESCE(a.question, q.question) AS question,
	                   COALESCE(a.option1, q.option1) AS option1,
	                   COALESCE(a.option2, q.option2) AS option2,
	                   COALESCE(a.option3, q.option3) AS option3,
	                   COALESCE(a.option4, q.option4) AS option4,
	                   COALESCE(a.explanation, q.explanation) AS explanation,
	                   COALESCE(a.answer, '') AS answer,
	                   COALESCE(a.answer_index, q.correct_index) AS answer_index
	            FROM answer a
	            LEFT JOIN quiz q ON a.quiz_id = q.id
	            WHERE a.user_id = ? AND a.video_id = ? AND a.attempt_id = ?
	            ORDER BY a.id ASC
	        """;
	
	        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	            stmt.setInt(1, userId);
	            stmt.setString(2, videoId);
	            stmt.setInt(3, attemptId);
	
	            ResultSet rs = stmt.executeQuery();
	            while (rs.next()) {
	                Answer a = new Answer();
	                a.setId(rs.getInt("id"));
	                a.setUserId(rs.getInt("user_id"));
	                a.setQuizId(rs.getInt("quiz_id"));
	                a.setSelectedOption(rs.getInt("selected_option"));
	                a.setCorrect(rs.getBoolean("is_correct"));
	                a.setVideoId(rs.getString("video_id"));
	                a.setAnsweredAt(rs.getTimestamp("answered_at").toLocalDateTime());
	                a.setSource(rs.getString("source"));
	                a.setAttemptId(rs.getInt("attempt_id"));
	
	                // 👇 補齊完整題目內容
	                a.setQuestion(rs.getString("question"));
	                a.setOption1(rs.getString("option1"));
	                a.setOption2(rs.getString("option2"));
	                a.setOption3(rs.getString("option3"));
	                a.setOption4(rs.getString("option4"));
	                a.setAnswer(rs.getString("answer"));
	                a.setAnswerIndex(rs.getInt("answer_index"));
	                a.setExplanation(rs.getString("explanation"));
	
	                answers.add(a);
	            }
	        }
	
	        return answers;
	    }
	    public int countCorrectAnswers(int userId, String videoId, int attemptId) throws SQLException {
	        String sql = """
	            SELECT COUNT(*) AS correct_count
	            FROM answer
	            WHERE user_id = ? AND video_id = ? AND attempt_id = ? AND is_correct = true
	        """;
	
	        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	            stmt.setInt(1, userId);
	            stmt.setString(2, videoId);
	            stmt.setInt(3, attemptId);
	            ResultSet rs = stmt.executeQuery();
	
	            if (rs.next()) {
	                return rs.getInt("correct_count");
	            }
	        }
	        return 0;
	    }
	    public void deleteAnswersByAttempt(int userId, String videoId, int attemptId) throws SQLException {
	        String sql = "DELETE FROM answer WHERE user_id = ? AND video_id = ? AND attempt_id = ?";
	        try (PreparedStatement ps = conn.prepareStatement(sql)) {
	            ps.setInt(1, userId);
	            ps.setString(2, videoId);
	            ps.setInt(3, attemptId);
	            ps.executeUpdate();
	        }
	    }
		public void deleteAnswersByUserAndVideo(int userId, String videoId) throws SQLException {
			String sql = "DELETE FROM answer WHERE user_id = ? AND video_id = ?";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setInt(1, userId);
				ps.setString(2, videoId);
				ps.executeUpdate();
			}
		}
	}
