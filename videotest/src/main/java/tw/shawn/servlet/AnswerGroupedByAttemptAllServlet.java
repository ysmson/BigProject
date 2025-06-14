package tw.shawn.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import tw.shawn.dao.AnswerDAO;
import tw.shawn.dao.AnswerDAO.AttemptGroup;
import tw.shawn.model.AnswerGroupDTO;
import tw.shawn.util.DBUtil;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Jackson 導入
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WebServlet("/api/answerGroupAll")
public class AnswerGroupedByAttemptAllServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int userId = Integer.parseInt(request.getParameter("userId"));

        try (Connection conn = DBUtil.getConnection()) {
            AnswerDAO dao = new AnswerDAO(conn);
            Map<String, List<AnswerDAO.AttemptGroup>> rawData = dao.getAnswersGroupedByUserAcrossVideos(userId);

            List<AnswerGroupDTO> result = new ArrayList<>();

            for (Map.Entry<String, List<AnswerDAO.AttemptGroup>> entry : rawData.entrySet()) {
                AnswerGroupDTO dto = new AnswerGroupDTO();
                dto.videoId = entry.getKey();
                dto.videoTitle = dao.getVideoTitle(dto.videoId);
                dto.attempts = entry.getValue();
                result.add(dto);
            }

            // ✅ 正確處理 LocalDateTime
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            response.setContentType("application/json; charset=UTF-8");
            mapper.writeValue(response.getWriter(), result);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
