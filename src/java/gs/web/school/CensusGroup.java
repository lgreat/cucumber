package gs.web.school;

/**
* Created with IntelliJ IDEA.
* User: samson
* Date: 3/1/13
* Time: 5:30 PM
* To change this template use File | Settings | File Templates.
*/
public enum CensusGroup {
    Teacher_Credentials(1),
    Teacher_Experience(2),
    Teacher_Education_Levels(3),
    Average_Class_Size(4),
    Student_Teacher_Ratio(5, "Student-Teacher Ratio"),
    Student_Ethnicity(6),
    /*
    from perl:
        'name' => 'Student Economic Level',
        'lowercasename' => 'student economic level',
        'abbrv' => $state eq 'nj' ? 'student_subgroups' : 'freelunch',
     */
    Student_Economic_Level(7),
    Attendance(8),
    English_Learners(9),
    Home_Languages_of_English_Learners(10),
    Home_Languages_of_All_Students(11),
    Student_Subgroups(13),
    Spending_Per_Pupil(14),
    Title_I_Funding(15),
    Attendance_and_Completion(16),
    Parent_Involvement(17),
    Parent_Education_Levels(18),
    Graduate_Intentions(19),
    School_Discipline_Incidents(20),
    Student_Staff_Ratios(21, "Student-Staff Ratios"),
    /*
    from perl:
    '23' => {
        'name' => "Spending Per Pupil",
        'lowercasename' => "spending per pupil",
        'abbrv' => "per_pupil_spending",
    },
     */
    Extended_Care(24),
    Classroom_Offerings(25),
    Graduation_Rate(26)
    ;

    private long _id;
    private String _label;

    CensusGroup(){}
    CensusGroup(long groupId) {
        _id = groupId;
        _label = defaultLabel();
    }
    CensusGroup(long groupId, String label) {
        _id = groupId;
        _label = label;
    }

    private String defaultLabel() {
        return this.name().replaceAll("_", " ");
    };
    public static CensusGroup getById(Long id) {
        for (CensusGroup group : CensusGroup.values()) {
            if (id.equals(group.getId())) {
                return group;
            }
        }
        return null;
    }
    public String getName() { return name(); } // so JSTL can access name()
    public String getLabel() { return _label; }
    public long getId() { return _id; }
}
