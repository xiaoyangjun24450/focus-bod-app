package com.xyj.focuspod.flow

import com.xyj.focuspod.model.GradeResult
import com.xyj.focuspod.model.PlanScore
import com.xyj.focuspod.model.StudyPlan

object ExamScoring {
    const val TOTAL_EXAM_SCORE = 100.0
    private const val FULL_SCORE_EPSILON = 0.01
    private const val PASS_SCORE_EPSILON = 0.0001

    fun questionMaxScore(examQuestionCount: Int): Double {
        return if (examQuestionCount <= 0) 0.0 else TOTAL_EXAM_SCORE / examQuestionCount
    }

    fun totalScore(results: List<GradeResult>): Double {
        return results.sumOf { if (isFullScore(it)) it.maxScore else it.score }
            .coerceIn(0.0, TOTAL_EXAM_SCORE)
    }

    fun mistakes(results: List<GradeResult>): List<GradeResult> {
        return results.filter { !isFullScore(it) }
    }

    fun isPassed(totalScore: Double, passScore: Int): Boolean {
        return totalScore + PASS_SCORE_EPSILON >= passScore
    }

    /**
     * 多份计划合并考试时，每份计划按自己题目部分的得分率折算成百分制，
     * 再与它自己的 passScore 比较；全部达标才算整体达标。
     */
    fun planScores(
        results: List<GradeResult>,
        plans: List<StudyPlan>,
        questionPlan: Map<String, String>
    ): List<PlanScore> {
        return plans.mapNotNull { plan ->
            val own = results.filter { questionPlan[it.questionId] == plan.id }
            if (own.isEmpty()) return@mapNotNull null
            val earned = own.sumOf { if (isFullScore(it)) it.maxScore else it.score }
            val max = own.sumOf { it.maxScore }
            val scaled = if (max <= 0.0) 0.0 else earned / max * TOTAL_EXAM_SCORE
            PlanScore(
                planId = plan.id,
                title = plan.title,
                score = scaled.coerceIn(0.0, TOTAL_EXAM_SCORE),
                passScore = plan.passScore,
                passed = scaled + PASS_SCORE_EPSILON >= plan.passScore
            )
        }
    }

    private fun isFullScore(result: GradeResult): Boolean {
        return result.score + FULL_SCORE_EPSILON >= result.maxScore
    }
}
