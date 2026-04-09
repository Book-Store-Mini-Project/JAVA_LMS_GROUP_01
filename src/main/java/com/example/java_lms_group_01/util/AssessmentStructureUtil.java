package com.example.java_lms_group_01.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class AssessmentStructureUtil {

    private AssessmentStructureUtil() {
    }

    public static MarkBreakdown calculateMarkBreakdown(Connection connection, String courseCode,
                                                       Double quiz1, Double quiz2, Double quiz3,
                                                       Double assessment, Double project, Double midTerm,
                                                       Double finalTheory, Double finalPractical) throws SQLException {
        Map<String, Double> weights = loadWeights(connection, courseCode);
        double topQuizContribution = topTwoQuizContribution(
                quiz1, weights.getOrDefault("quiz_1", 0.0),
                quiz2, weights.getOrDefault("quiz_2", 0.0),
                quiz3, weights.getOrDefault("quiz_3", 0.0)
        );
        double assessmentContribution = weightedMark(assessment, weights.getOrDefault("assessment", 0.0));
        double projectContribution = weightedMark(project, weights.getOrDefault("project", 0.0));
        double midTermContribution = weightedMark(midTerm, weights.getOrDefault("mid_term", 0.0));

        double caMarks = topQuizContribution + assessmentContribution + projectContribution + midTermContribution;
        double endMarks = calculateEndMarks(weights, finalTheory, finalPractical);
        return new MarkBreakdown(caMarks, endMarks, caMarks + endMarks);
    }

    private static Map<String, Double> loadWeights(Connection connection, String courseCode) throws SQLException {
        String sql = """
                SELECT component, weight
                FROM assessment_structure
                WHERE courseCode = ?
                """;
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
        return switch (normalized) {
            case "quiz1" -> "quiz_1";
            case "quiz2" -> "quiz_2";
            case "quiz3" -> "quiz_3";
            case "assignment" -> "assessment";
            case "project_work" -> "project";
            case "mid_exam", "midterm", "mid" -> "mid_term";
            case "end_theory", "theory", "endtheory" -> "final_theory";
            case "end_practical", "practical", "endpractical" -> "final_practical";
            case "endexam", "finalexam", "end_exam_marks" -> "end_exam";
            default -> normalized;
        };
    }

    private static double weightedMark(Double mark, double weight) {
        if (mark == null || weight <= 0) {
            return 0.0;
        }
        return mark * weight / 100.0;
    }

    private static double calculateEndMarks(Map<String, Double> weights, Double finalTheory, Double finalPractical) {
        double combinedWeight = weights.getOrDefault("end_exam", 0.0);
        if (combinedWeight > 0) {
            return weightedMark(averageEndExamMark(finalTheory, finalPractical), combinedWeight);
        }

        double finalTheoryContribution = weightedMark(finalTheory, weights.getOrDefault("final_theory", 0.0));
        double finalPracticalContribution = weightedMark(finalPractical, weights.getOrDefault("final_practical", 0.0));
        return finalTheoryContribution + finalPracticalContribution;
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
        QuizScore[] quizzes = {
                new QuizScore(quiz1, quiz1Weight),
                new QuizScore(quiz2, quiz2Weight),
                new QuizScore(quiz3, quiz3Weight)
        };

        for (int i = 0; i < quizzes.length - 1; i++) {
            for (int j = i + 1; j < quizzes.length; j++) {
                if (quizzes[j].mark() > quizzes[i].mark()) {
                    QuizScore temp = quizzes[i];
                    quizzes[i] = quizzes[j];
                    quizzes[j] = temp;
                }
            }
        }

        return quizzes[0].contribution() + quizzes[1].contribution();
    }

    private record QuizScore(double mark, double contribution) {
        private QuizScore(Double mark, double weight) {
            this(mark == null ? -1.0 : mark, weightedValue(mark, weight));
        }
    }

    private static double weightedValue(Double mark, double weight) {
        return mark == null || weight <= 0 ? 0.0 : mark * weight / 100.0;
    }

    public record MarkBreakdown(double caMarks, double endMarks, double totalMarks) {}
}
