package com.example.java_lms_group_01.util;

import com.example.java_lms_group_01.model.summary.MarkBreakdown;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

// This class calculates final marks based on database rules (weights)
public final class AssessmentStructureUtil {

    // Prevent creating objects of this class
    private AssessmentStructureUtil() {
    }


    public static MarkBreakdown calculateMarkBreakdown(
            Connection connection,
            String courseCode,
            Double quiz1Marks, Double quiz2Marks, Double quiz3Marks,
            Double assignmentMarks, Double projectMarks, Double midTermMarks,
            Double finalTheoryMarks, Double finalPracticalMarks) throws SQLException {

        //Get weight rules from database for this course
        Map<String, Double> weightMap = loadWeights(connection, courseCode);


        // Calculate quiz marks (best 2 quizzes will be used)
        double quizContribution = calculateTopTwoQuizContribution(
                quiz1Marks, quiz2Marks, quiz3Marks,
                getWeight(weightMap, "quiz_1"),
                getWeight(weightMap, "quiz_2"),
                getWeight(weightMap, "quiz_3")
        );

        // Calculate assignment marks based on weight
        double assignmentContribution = calculateWeightedMark(
                assignmentMarks,
                getWeight(weightMap, "assessment")
        );

        // Calculate project marks based on weight
        double projectContribution = calculateWeightedMark(
                projectMarks,
                getWeight(weightMap, "project")
        );

        // Calculate mid-term marks based on weight
        double midTermContribution = calculateWeightedMark(
                midTermMarks,
                getWeight(weightMap, "mid_term")
        );

        // Add all CA components together
        double totalCaMarks =
                quizContribution +
                        assignmentContribution +
                        projectContribution +
                        midTermContribution;


        double totalEndMarks;

        // Check if course uses combined end exam
        double endExamWeight = getWeight(weightMap, "end_exam");

        if (endExamWeight > 0) {

            // If combined exam exists → average theory + practical
            Double averageEndMarks = calculateAverage(finalTheoryMarks, finalPracticalMarks);

            totalEndMarks = calculateWeightedMark(averageEndMarks, endExamWeight);

        } else {

            // Otherwise calculate theory and practical separately
            double theoryContribution = calculateWeightedMark(
                    finalTheoryMarks,
                    getWeight(weightMap, "final_theory")
            );

            double practicalContribution = calculateWeightedMark(
                    finalPracticalMarks,
                    getWeight(weightMap, "final_practical")
            );

            totalEndMarks = theoryContribution + practicalContribution;
        }

        // Return final result object
        return new MarkBreakdown(
                totalCaMarks,                 // CA marks
                totalEndMarks,               // End exam marks
                totalCaMarks + totalEndMarks, // Total marks
                calculateCaMaximum(weightMap), // Max CA possible
                calculateEndMaximum(weightMap) // Max End possible
        );
    }

    private static Map<String, Double> loadWeights(Connection connection, String courseCode) throws SQLException {

        Map<String, Double> weightMap = new HashMap<>();

        // SQL query to get weights for a course
        String sqlQuery =
                "SELECT component, weight FROM assessment_structure WHERE courseCode = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        preparedStatement.setString(1, courseCode);

        ResultSet resultSet = preparedStatement.executeQuery();

        // Read each row from database
        while (resultSet.next()) {

            // Get component name (example: Quiz 1 → quiz_1)
            String componentName = resultSet.getString("component")
                    .toLowerCase()
                    .replace(" ", "_");

            // Get weight value (example: 10%)
            double weightValue = resultSet.getDouble("weight");

            // Store in map
            weightMap.put(componentName, weightValue);
        }

        return weightMap;
    }


    private static double calculateWeightedMark(Double marks, double weight) {

        // If student didn't submit marks → treat as 0
        if (marks == null) return 0.0;

        return (marks * weight) / 100.0;
    }

    // Get weight safely from map
    private static double getWeight(Map<String, Double> weightMap, String componentName) {
        return weightMap.getOrDefault(componentName, 0.0);
    }

    private static Double calculateAverage(Double value1, Double value2) {

        // If both missing
        if (value1 == null && value2 == null) return null;

        // If only one exists
        if (value1 == null) return value2;
        if (value2 == null) return value1;

        // If both exist → average them
        return (value1 + value2) / 2.0;
    }

    private static double calculateTopTwoQuizContribution(
            Double quiz1Marks, Double quiz2Marks, Double quiz3Marks,
            double quiz1Weight, double quiz2Weight, double quiz3Weight) {

        double quiz1Value = (quiz1Marks == null) ? 0 : (quiz1Marks * quiz1Weight / 100);
        double quiz2Value = (quiz2Marks == null) ? 0 : (quiz2Marks * quiz2Weight / 100);
        double quiz3Value = (quiz3Marks == null) ? 0 : (quiz3Marks * quiz3Weight / 100);

        // Find lowest quiz
        double lowestQuizValue =
                Math.min(quiz1Value, Math.min(quiz2Value, quiz3Value));

        // Remove lowest quiz and return best 2
        return quiz1Value + quiz2Value + quiz3Value - lowestQuizValue;
    }

    private static double calculateCaMaximum(Map<String, Double> weightMap) {

        double quiz1Weight = getWeight(weightMap, "quiz_1");
        double quiz2Weight = getWeight(weightMap, "quiz_2");
        double quiz3Weight = getWeight(weightMap, "quiz_3");

        // Remove lowest quiz weight (same rule as marks)
        double lowestQuizWeight =
                Math.min(quiz1Weight, Math.min(quiz2Weight, quiz3Weight));

        double totalQuizWeight =
                quiz1Weight + quiz2Weight + quiz3Weight - lowestQuizWeight;

        return totalQuizWeight
                + getWeight(weightMap, "assessment")
                + getWeight(weightMap, "project")
                + getWeight(weightMap, "mid_term");
    }

    private static double calculateEndMaximum(Map<String, Double> weightMap) {

        double endExamWeight = getWeight(weightMap, "end_exam");

        // If combined exam exists
        if (endExamWeight > 0) {
            return endExamWeight;
        }

        // Otherwise add theory + practical
        return getWeight(weightMap, "final_theory")
                + getWeight(weightMap, "final_practical");
    }
}
