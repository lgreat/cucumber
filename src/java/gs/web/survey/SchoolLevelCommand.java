package gs.web.survey;

import gs.data.school.LevelCode;
import gs.data.school.School;

public class SchoolLevelCommand {
    private School _school;
    private LevelCode.Level _level;

    public School getSchool() {
        return _school;
    }

    public void setSchool(School school) {
        _school = school;
    }

    public LevelCode.Level getLevel() {
        return _level;
    }

    public void setLevel(String level) {
        _level = LevelCode.Level.getLevelCode(level);
    }
}
