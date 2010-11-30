package gs.web.school;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for DC NCLB project (GS-9899)
 */
public class DcNclbReportCardHelper {
    // from School_without_LEA_PAGES.xlsx in GS-9899
    // GS-10804 Unlike last year we will not be including an LEA level PDF for schools with a district_id='0'. These schools will only display a school and state PDF.
    final private static Map<Integer,Integer> schoolIdLeaCodeMap = new HashMap<Integer,Integer>();

    static {
        schoolIdLeaCodeMap.put(374,101); // Academia Bilingue de la Comunidad PCS
        schoolIdLeaCodeMap.put(375,102); //Alta PCS
        schoolIdLeaCodeMap.put(383,124); //Howard University of Math & Science Middle School
        schoolIdLeaCodeMap.put(385,139); //Potomac PCS
        schoolIdLeaCodeMap.put(557,111); //City Collegiate PCS
        schoolIdLeaCodeMap.put(560,136); //Nia Community PCS
        schoolIdLeaCodeMap.put(809,151); //Washington Latin School PCS
        schoolIdLeaCodeMap.put(382,118); //Early Childhood Academy PCS
        schoolIdLeaCodeMap.put(565,132); //Mary McLeod Bethune PCS - Slowe
        schoolIdLeaCodeMap.put(367,116); //E.L. Haynes PCS
        schoolIdLeaCodeMap.put(370,154); //Young America Works PCS
        schoolIdLeaCodeMap.put(250,106); //Booker T. Washington
        schoolIdLeaCodeMap.put(253,110); //Children's Studio PCS
        schoolIdLeaCodeMap.put(256,144); //E. Whitlow Stokes PCS
        schoolIdLeaCodeMap.put(257,122); //Hospitality PCS
        schoolIdLeaCodeMap.put(260,126); //Idea PCS
        schoolIdLeaCodeMap.put(262,135); //Meridian PCS
        schoolIdLeaCodeMap.put(264,145); //Next Step PCS
        schoolIdLeaCodeMap.put(265,137); //Options PCS
        schoolIdLeaCodeMap.put(286,138); //Paul JH PCS
        schoolIdLeaCodeMap.put(267,140); //Roots PCS - Kennedy
        schoolIdLeaCodeMap.put(269,142); //The Seed School
        schoolIdLeaCodeMap.put(273,152); //Washington Math, Science, and Tech PCS
        schoolIdLeaCodeMap.put(801,141); //Sail Lower School
        schoolIdLeaCodeMap.put(276,104); //Arts & Technology PCS
        schoolIdLeaCodeMap.put(558,143); //St. Coletta Special Education PCS
        schoolIdLeaCodeMap.put(287,147); //Tree of Life Community PCS
        schoolIdLeaCodeMap.put(284,128); //Kamit Institute PCS
        schoolIdLeaCodeMap.put(300,146); //Thurgood Marshall PCS
        schoolIdLeaCodeMap.put(290,105); //Barbara Jordan PCS
        schoolIdLeaCodeMap.put(338,130); //Lamb PCS
        schoolIdLeaCodeMap.put(369,149); //Two Rivers PCS
        schoolIdLeaCodeMap.put(360,114); //DC Bilingual PCS
        schoolIdLeaCodeMap.put(587,155); //Achievement Preparatory Academy PCS
        schoolIdLeaCodeMap.put(615,157); //Thea Bowman Preparatory Academy PCS
        schoolIdLeaCodeMap.put(607,159); //Imagine Southeast
        schoolIdLeaCodeMap.put(258,125); //Hyde Leadership PCS
    }

    public static int getDcNclbLeaCode(int schoolId) {
        Integer code = schoolIdLeaCodeMap.get(schoolId);
        if (code != null) {
            return code;
        }
        return -1;
    }
}
