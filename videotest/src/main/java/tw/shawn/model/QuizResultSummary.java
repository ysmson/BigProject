package tw.shawn.model;

public class QuizResultSummary {
    private String videoId;
    private String videoTitle;     // ✅ 新增欄位：影片名稱
    private String source;
    private int totalQuizCount;
    private int total;
    private int correct;

    // 建構子（保留原來）
    public QuizResultSummary(String videoId, String source, int totalQuizCount, int total, int correct) {
        this.videoId = videoId;
        this.source = source;
        this.totalQuizCount = totalQuizCount;
        this.total = total;
        this.correct = correct;
    }

    // ✅ 若你 DAO 是使用無參構造 + setter，也可保留這個空建構子
    public QuizResultSummary() {}

    // === Getter / Setter ===

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getTotalQuizCount() {
        return totalQuizCount;
    }

    public void setTotalQuizCount(int totalQuizCount) {
        this.totalQuizCount = totalQuizCount;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }
}
