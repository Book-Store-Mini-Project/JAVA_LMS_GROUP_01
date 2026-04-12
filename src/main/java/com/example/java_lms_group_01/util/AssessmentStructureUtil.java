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
        double combinedWeight = weights.getOrDefault("end_exam", 0.0);
        if (combinedWeight > 0) {
            return weightedMark(averageEndExamMark(finalTheory, finalPractical), combinedWeight);
        }

        double finalTheoryContribution = weightedMark(finalTheory, weights.getOrDefault("final_theory", 0.0));
        double finalPracticalContribution = weightedMark(finalPractical, weights.getOrDefault("final_practical", 0.0));
        return finalTheoryContribution + finalPracticalContribution;
    }

    private static double calculateCaMaximum(Map<String, Double> weights) {
        return topTwoQuizWeight(
                weights.getOrDefault("quiz_1", 0.0),
                weights.getOrDefault("quiz_2", 0.0),
                weights.getOrDefault("quiz_3", 0.0)
        )
                + weights.getOrDefault("assessment", 0.0)
                + weights.getOrDefault("project", 0.0)
                + weights.getOrDefault("mid_term", 0.0);
    }

    private static double calculateEndMaximum(Map<String, Double> weights) {
        double combinedWeight = weights.getOrDefault("end_exam", 0.0);
        if (combinedWeight > 0) {
            return combinedWeight;
        }
        return weights.getOrDefault("final_theory", 0.0) + weights.getOrDefault("final_practical", 0.0);
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
                if (quizzes[j].getMark() > quizzes[i].getMark()) {
                    QuizScore temp = quizzes[i];
                    quizzes[i] = quizzes[j];
                    quizzes[j] = temp;
                }
            }
        }

        return quizzes[0].getContribution() + quizzes[1].getContribution();
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

    private static class QuizScore {
        private final double mark;
        private final double contribution;

        private QuizScore(Double mark, double weight) {
            this.mark = mark == null ? -1.0 : mark;
            this.contribution = weightedValue(mark, weight);
        }

        public double getMark() {
            return mark;
        }

        public double getContribution() {
            return contribution;
        }
    }

    private static double weightedValue(Double mark, double weight) {
        return mark == null || weight <= 0 ? 0.0 : mark * weight / 100.0;
    }

    public static class MarkBreakdown {
        private final double caMarks;
        private final double endMarks;
        private final double totalMarks;
        private final double caMaximum;
        private final double endMaximum;

        public MarkBreakdown(double caMarks, double endMarks, double totalMarks, double caMaximum, double endMaximum) {
            this.caMarks = caMarks;
            this.endMarks = endMarks;
            this.totalMarks = totalMarks;
            this.caMaximum = caMaximum;
            this.endMaximum = endMaximum;
        }

        public double getCaMarks() {
            return caMarks;
        }

        public double getEndMarks() {
            return endMarks;
        }

        public double getTotalMarks() {
            return totalMarks;
        }

        public double getCaMaximum() {
            return caMaximum;
        }

        public double getEndMaximum() {
            return endMaximum;
        }
    }
}
