package tw.shawn.model;

import java.util.List;
import tw.shawn.dao.AnswerDAO.AttemptGroup;

public class AnswerGroupDTO {
    public String videoId;
    public String videoTitle;
    public List<AttemptGroup> attempts;
}
