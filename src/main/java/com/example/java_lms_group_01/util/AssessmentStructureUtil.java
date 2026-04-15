package com.example.java_lms_group_01.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates course marks by reading the assessment weights for a course.
 */
public final class AssessmentStructureUtil {

    private AssessmentStructureUtil() {
    }

    public static MarkBreakdown calculateMarkBreakdown(Connection connection, String courseCode,
                                                       Double quiz1, Double quiz2, Double quiz3,
                                                       Double assessment, Double project, Double midTerm,
                                                       Double finalTheory, Double finalPractical) throws SQLException {
        Map<String, Double> weights = loadWeights(connection, courseCode);
        double quiz1Weight = getWeight(weights, "quiz_1");
        double quiz2Weight = getWeight(weights, "quiz_2");
        double quiz3Weight = getWeight(weights, "quiz_3");
        double assessmentWeight = getWeight(weights, "assessment");
        double projectWeight = getWeight(weights, "project");
        double midTermWeight = getWeight(weights, "mid_term");

        double topQuizContribution = topTwoQuizContribution(
                quiz1, quiz1Weight,
                quiz2, quiz2Weight,
                quiz3, quiz3Weight
        );
        double assessmentContribution = weightedMark(assessment, assessmentWeight);
        double projectContribution = weightedMark(project, projectWeight);
        double midTermContribution = weightedMark(midTerm, midTermWeight);

        double caMarks = topQuizContribution + assessmentContribution + projectContribution + midTermContribution;
        double endMarks = calculateEndMarks(weights, finalTheory, finalPractical);
        return new MarkBreakdown(
                caMarks,
                endMarks,
                caMarks + endMarks,
                calculateCaMaximum(weights),
                calculateEndMaximum(weights)
        );
    }

    private static Map<String, Double> loadWeights(Connection connection, String courseCode) throws SQLException {
        String sql = "SELECT component, weight FROM assessment_structure WHERE courseCode = ?";
        Map<String, Double> weights = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courseCode);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String component = normalizeComponent(rs.getString("component"));
                    if (!component.isBlank()) {
                        weights.put(component, rs.getDouble("weight"));
                    }
                }
            }
        }
        return weights;
    }

    private static String normalizeComponent(String component) {
        if (component == null) {
            return "";
        }
        String normalized = component.trim().toLowerCase().replace(' ', '_');
        if (normalized.equals("quiz1")) {
            return "quiz_1";
        }
        if (normalized.equals("quiz2")) {
            return "quiz_2";
        }
        if (normalized.equals("quiz3")) {
            return "quiz_3";
        }
        if (normalized.equals("assignment")) {
            return "assessment";
        }
        if (normalized.equals("project_work")) {
            return "project";
        }
        if (normalized.equals("mid_exam") || normalized.equals("midterm") || normalized.equals("mid")) {
            return "mid_term";
        }
        if (normalized.equals("end_theory") || normalized.equals("theory") || normalized.equals("endtheory")) {
            return "final_theory";
        }
        if (normalized.equals("end_practical") || normalized.equals("practical") || normalized.equals("endpractical")) {
            return "final_practical";
        }
        if (normalized.equals("endexam") || normalized.equals("finalexam") || normalized.equals("end_exam_marks")) {
            return "end_exam";
        }
        return normalized;
    }

    private static double weightedMark(Double mark, double weight) {
        if (mark == null || weight <= 0) {
            return 0.0;
        }
        return mark * weight / 100.0;
    }

    private static double calculateEndMarks(Map<String, Double> weights, Double finalTheory, Double finalPractical) {
        double combinedWeight = getWeight(weights, "end_exam");
        if (combinedWeight > 0) {
            return weightedMark(averageEndExamMark(finalTheory, finalPractical), combinedWeight);
        }

        double finalTheoryContribution = weightedMark(finalTheory, getWeight(weights, "final_theory"));
        double finalPracticalContribution = weightedMark(finalPractical, getWeight(weights, "final_practical"));
        return finalTheoryContribution + finalPracticalContribution;
    }

    private static double calculateCaMaximum(Map<String, Double> weights) {
        return topTwoQuizWeight(
                getWeight(weights, "quiz_1"),
                getWeight(weights, "quiz_2"),
                getWeight(weights, "quiz_3")
        )
                + getWeight(weights, "assessment")
                + getWeight(weights, "project")
                + getWeight(weights, "mid_term");
    }

    private static double calculateEndMaximum(Map<String, Double> weights) {
        double combinedWeight = getWeight(weights, "end_exam");
        if (combinedWeight > 0) {
            return combinedWeight;
        }
        return getWeight(weights, "final_theory") + getWeight(weights, "final_practical");
    }

    private static Double averageEndExamMark(Double finalTheory, Double finalPractical) {
        if (finalTheory == null && finalPractical == null) {
            return null;
        }
        if (finalTheory == null) {
            return finalPractical;
        }
        if (finalPractical == null) {
            return finalTheory;
        }
        return (finalTheory + finalPractical) / 2.0;
    }

    private static double topTwoQuizContribution(Double quiz1, double quiz1Weight,
                                                 Double quiz2, double quiz2Weight,
                                                 Double quiz3, double quiz3Weight) {
        double[] marks = {
                quiz1 == null ? -1.0 : quiz1,
                quiz2 == null ? -1.0 : quiz2,
                quiz3 == null ? -1.0 : quiz3
        };
        double[] contributions = {
                weightedValue(quiz1, quiz1Weight),
                weightedValue(quiz2, quiz2Weight),
                weightedValue(quiz3, quiz3Weight)
        };

        for (int i = 0; i < marks.length - 1; i++) {
            for (int j = i + 1; j < marks.length; j++) {
                if (marks[j] > marks[i]) {
                    double tempMark = marks[i];
                    marks[i] = marks[j];
                    marks[j] = tempMark;

                    double tempContribution = contributions[i];
                    contributions[i] = contributions[j];
                    contributions[j] = tempContribution;
                }
            }
        }

        return contributions[0] + contributions[1];
    }

    private static double topTwoQuizWeight(double quiz1Weight, double quiz2Weight, double quiz3Weight) {
        double[] quizWeights = {quiz1Weight, quiz2Weight, quiz3Weight};
        for (int i = 0; i < quizWeights.length - 1; i++) {
            for (int j = i + 1; j < quizWeights.length; j++) {
                if (quizWeights[j] > quizWeights[i]) {
                    double temp = quizWeights[i];
                    quizWeights[i] = quizWeights[j];
                    quizWeights[j] = temp;
                }
            }
        }
        return quizWeights[0] + quizWeights[1];
    }

    private static double weightedValue(Double mark, double weight) {
        if (mark == null || weight <= 0) {
            return 0.0;
        }
        return mark * weight / 100.0;
    }

    private static double getWeight(Map<String, Double> weights, String component) {
        Double value = weights.get(component);
        if (value == null) {
            return 0.0;
        }
        return value;
    }
}
