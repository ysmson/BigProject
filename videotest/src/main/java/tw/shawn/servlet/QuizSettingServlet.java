package tw.shawn.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/quizSetting")
public class QuizSettingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='zh-Hant'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>題目設定</title>");
        out.println("<style>");
        out.println("body { margin: 0; font-family: sans-serif; background-color: #111; color: #000; padding-top: 80px; padding-bottom: 60px; }");
        out.println(".navbar { background-color: white; padding: 20px; display: flex; justify-content: space-between; align-items: center; position: fixed; top: 0; width: 100%; }");
        out.println(".btn:hover { background-color: #333; }");
        out.println(".nav-left { font-size: 24px; margin-left: 20px; }");
        out.println(".nav-right { display: flex; gap: 20px; margin-right: 40px; font-size: 18px; }");
        out.println(".container { background-color: white; margin: 80px auto; padding: 40px; max-width: 640px; border-radius: 6px; box-shadow: 0 4px 16px rgba(0,0,0,0.2); }");
        out.println(".row { display: flex; align-items: center; margin-bottom: 30px; }");
        out.println(".label { width: 150px; font-size: 20px; }");
        out.println("input[type='text'] { width: 150px; padding: 5px; background-color: black; color: white; border: none; text-align: center; }");
        out.println("input[type='range'] { width: 100%; }");
        out.println(".btn { background-color: black; color: white; padding: 10px 30px; border: none; font-size: 18px; float: right; cursor: pointer; }");
        out.println(".footer { background-color: white; text-align: center; padding: 20px; font-size: 18px; position: fixed; bottom: 0; width: 100%; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");

        // 導覽列
        out.println("<div class='navbar'>");
        out.println("<div class='nav-left'>Logo</div>");
        out.println("<div class='nav-right'>");
        out.println("<div>測驗中心</div>");
        out.println("<div>商城</div>");
        out.println("<div>經驗值</div>");
        out.println("<div>我的資料</div>");
        out.println("<div>設定</div>");
        out.println("</div>");
        out.println("</div>");

        // 主內容
        out.println("<div class='container'>");
        out.println("<form id='settingForm'>");

        out.println("<div class='row'>");
        out.println("<div class='label'>總題數</div>");
        out.println("<input type='text' name='totalQuestions' placeholder='Input Text'>");
        out.println("</div>");

        out.println("<div class='row'>");
        out.println("<div class='label'>難易度分配</div>");
        out.println("<label><input type='radio' name='difficulty' value='basic' checked> 基礎</label>");
        out.println("<label style='margin-left:20px;'><input type='radio' name='difficulty' value='normal'> 普通</label>");
        out.println("<label style='margin-left:20px;'><input type='radio' name='difficulty' value='advanced'> 進階</label>");
        out.println("</div>");

        out.println("<button type='submit' class='btn'>確認</button>");
        out.println("</form>");
        out.println("</div>");

        // JavaScript：加入 videoId + 表單導向處理
        out.println("<script>");
        out.println("document.getElementById('settingForm').onsubmit = function(e) {");
        out.println("    e.preventDefault();");
        out.println("    const quizNum = document.querySelector('[name=totalQuestions]').value;");
        out.println("    const difficulty = document.querySelector('[name=difficulty]:checked').value;");
        out.println("    const urlParams = new URLSearchParams(window.location.search);");
        out.println("    const videoId = urlParams.get('videoId');");
        out.println("    if (!quizNum) { alert('請輸入總題數'); return; }");
        out.println("    const url = `quiz.html?videoId=${encodeURIComponent(videoId)}&quizNum=${encodeURIComponent(quizNum)}&difficulty=${encodeURIComponent(difficulty)}`;");
        out.println("    window.location.href = url;");
        out.println("};");
        out.println("</script>");

        out.println("<div class='footer'>客服信箱</div>");
        out.println("</body>");
        out.println("</html>");
    }
}
