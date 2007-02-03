/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolRatingsDisplay.java,v 1.23 2007/02/03 01:09:21 eddie Exp $
 */

package gs.web.test.rating;

import gs.data.school.Grade;
import gs.data.school.Grades;
import gs.data.school.School;
import gs.data.test.ITestDataSetDao;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.state.State;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generates to display based on a RatingsConfig object.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class SchoolRatingsDisplay implements IRatingsDisplay {

    private final IRatingsConfig _ratingsConfig;
    private final List _subjectGroupLabels;
    private final List _rowGroups;
    private final Map _rawResults;


    public SchoolRatingsDisplay(final IRatingsConfig ratingsConfig, School school, final ITestDataSetDao testDataSetDao) {
        _ratingsConfig = ratingsConfig;

        _rawResults = testDataSetDao.findAllRawResults(school,
                new int [] {ratingsConfig.getYear() - 1, ratingsConfig.getYear()}, true);

        Grades grades = school.getGradeLevels();

        // Subject group labels are precalculated.
        _subjectGroupLabels = new ArrayList();
        for (int i = 0; i < _ratingsConfig.getSubjectGroupConfigs().length; i++) {
            IRatingsConfig.ISubjectGroupConfig subjectGroupConfig = _ratingsConfig.getSubjectGroupConfigs()[i];
            _subjectGroupLabels.add(subjectGroupConfig.getLabel());

        }

        // must combine row groups from the different subject groups
        _rowGroups = new ArrayList();
        final IRatingsConfig.IRowGroupConfig[] rowGroupConfigs = _ratingsConfig.getRowGroupConfigs();
        for (int j = 0; j < rowGroupConfigs.length; j++) {
            IRatingsConfig.IRowGroupConfig rowGroupConfig = rowGroupConfigs[j];
            RowGroup rowGroup = new RowGroup(rowGroupConfig);
            String label = rowGroup.getLabel();
            boolean isGradeRowgroup = false;

            if (label.startsWith("By Grade")) {
                isGradeRowgroup = true;
            }

            final IRatingsConfig.IRowConfig[] rowConfigs = rowGroupConfig.getRowConfigs();

            List allRowConfigs = new ArrayList();

            int numAllRow = 0;
            for (int i = 0; i < rowConfigs.length; i++) {
                final IRatingsConfig.IRowConfig rowConfig = rowConfigs[i];
                List singleRowConfig = new ArrayList();
                singleRowConfig.add(rowConfig);
                Row row = new Row(singleRowConfig);

                if ("All Students".equals(row.getLabel())) {
                    continue;
                }

                if (isGradeRowgroup) {
                    String rowLabel = row.getConfigLabel();
                    String grade = rowLabel.replaceAll("Grade ", "");

                    if (grades.contains(Grade.getGradeLevel(grade))) {
                        rowGroup.add(row);
                    }  else if(rowLabel.startsWith("Grade All")  ){

                        String gradeall = rowLabel.substring(6);
                        if(school.getLevelCode().containsSimilarLevelCode(Grade.getLevelCodeFromName(gradeall))){
                            allRowConfigs.add(rowConfig);
                            numAllRow++;
                        }
                    }
                } else {
                    if (!row.isAllEmptyCells()) {
                        rowGroup.add(row);
                    }
                }
            }

            if(numAllRow > 0){
                Row all = new Row(allRowConfigs);
                rowGroup.add(all);
            }

            if (rowGroup.getRows().size() > 0) {
                _rowGroups.add(rowGroup);
            }

        }
    }

    protected class Row implements IRowGroup.IRow {
        private final IRatingsConfig.IRowConfig _rowConfig;
        private List _cells;


        Row(final List rowConfigs) {
            _rowConfig= (IRatingsConfig.IRowConfig) rowConfigs.get(0);
            _cells = new ArrayList();

            IRatingsConfig.ISubjectGroupConfig [] subjects = _ratingsConfig.getSubjectGroupConfigs();

            for (int numSubjectGroups = 0; numSubjectGroups < subjects.length; numSubjectGroups++) {
                List dataSetIds = new ArrayList();

                for (Iterator rowConfigIter = rowConfigs.iterator(); rowConfigIter.hasNext();) {
                    IRatingsConfig.IRowConfig rowConfig = (IRatingsConfig.IRowConfig) rowConfigIter.next();
                    dataSetIds = addIntArrayToList(dataSetIds,_ratingsConfig.getDataSetIds(subjects[numSubjectGroups], rowConfig));
                }

                int count = 0;
                int total = 0;

                int prevCount = 0;
                int prevTotal = 0;

                int ids[] = getValuesFromIntList(dataSetIds);
                for (int i = 0; i < ids.length; i++) {
                    Integer testDataSetId = new Integer(ids[i]);
                    if (_rawResults.containsKey(testDataSetId)) {
                        ITestDataSetDao.IRawResult result = (ITestDataSetDao.IRawResult) _rawResults.get(testDataSetId);
                        int decile = result.getDecile();
                        if (decile > 0) {
                            count ++;
                            total += decile;
                        }
                    }
                }

                Integer rating = null;
                Integer prevRating = null;

                if (count > 0) {
                    rating = new Integer(Math.round((float) total / (float) count));
                }
                if (prevCount > 0) {
                    prevRating = new Integer(Math.round((float) prevTotal / (float) prevCount));
                }

                _cells.add(new Cell(rating, TestManager.calculateRatingTrend(rating, prevRating)));
            }
        }



        public String getLabel() {
            String label = _rowConfig.getLabel();
            State state = _ratingsConfig.getState();
            if(label.startsWith("Grade All")){
                if(state.equals(State.AR)){
                    label = "EOC";
                }
                else if(state.equals(State.MD)){
                    label = "HSA";
                }
                else if(state.equals(State.MS)){
                    label = "SATP";
                }
                else if(state.equals(State.NC)){
                    label = "EOC";
                }
                else if(state.equals(State.UT)){
                    label = "EOC";
                }
                else if(state.equals(State.VA)){
                    label = "EOC";
                }
                else if(state.equals(State.VT)){
                    label = "NECAP";
                }
            }

            return label;

        }


        public String getConfigLabel() {
            return _rowConfig.getLabel();

        }

        public List getCells() {
            return _cells;
        }

        boolean isAllEmptyCells() {
            for (Iterator iter = _cells.iterator(); iter.hasNext();) {
                Cell cell = (Cell) iter.next();
                if (cell.getRating() != null) {
                    return false;
                }
            }

            return true;
        }
    }

    public List getSubjectGroupLabels() {
        return _subjectGroupLabels;
    }

    public List getRowGroups() {
        // list of IRowGroup objects
        return _rowGroups;
    }


    public static List addIntArrayToList(List a,int [] b) {

        for (int numInts = 0; numInts < b.length; numInts++) {
            Integer tempInt = new Integer(b[numInts]);
            a.add(tempInt);
        }

        return a;
    }

    public static int[] getValuesFromIntList(List a) {

        int[] ids = new int[a.size()];
        int indexInt = 0;
        for (Iterator iter = a.iterator(); iter.hasNext();) {
            Integer ID = (Integer) iter.next();
            int id = ID.intValue();
            ids[indexInt] = id;
            indexInt++;
        }

        return ids;
    }


}
