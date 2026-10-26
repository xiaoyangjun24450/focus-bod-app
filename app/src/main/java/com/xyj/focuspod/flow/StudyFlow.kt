package com.xyj.focuspod.flow

import android.os.Handler
import android.view.TextureView
import com.xyj.focuspod.model.DeviceCheckItem
import com.xyj.focuspod.model.DeviceCheckStatus
import com.xyj.focuspod.model.DoorStatus
import com.xyj.focuspod.model.GradeResult
import com.xyj.focuspod.model.GradingStep
import com.xyj.focuspod.model.PlanScore
import com.xyj.focuspod.model.Question
import com.xyj.focuspod.model.QuestionSource
import com.xyj.focuspod.model.StudyPage
import com.xyj.focuspod.model.StudyPlan
import com.xyj.focuspod.model.StudySessionState
import com.xyj.focuspod.model.StudyStage
import com.xyj.focuspod.model.TutoringMessage
import com.xyj.focuspod.model.TutoringSpeaker
import com.xyj.focuspod.service.ai.GradingAiService
import com.xyj.focuspod.service.ai.TutoringAiListener
import com.xyj.focuspod.service.ai.TutoringAiService
import com.xyj.focuspod.service.ai.TutoringSubtitle
import com.xyj.focuspod.service.api.StudyPlanApi
import com.xyj.focuspod.service.camera.CameraCaptureService
import com.xyj.focuspod.service.device.DoorControlService
import com.xyj.focuspod.service.sensor.PoseDetector
import com.xyj.focuspod.service.voice.VoicePromptService
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

class StudyFlow(
    private val handler: Handler,
    private val studyPlanApi: StudyPlanApi,
    private val poseDetector: PoseDetector,
    private val cameraCaptureService: CameraCaptureService,
    private val tutoringAiService: TutoringAiService,
    private val gradingAiService: GradingAiService,
    private val doorControlService: DoorControlService,
    private val voicePromptService: VoicePromptService
) {
    private var state = StudySessionState()
    private val listeners = mutableSetOf<(StudySessionState) -> Unit>()
    private var activeStageQuestions: List<Question> = emptyList()
    private var examRoundQuestions: List<Question> = emptyList()
    private var exampleIndex = 0
    private var examIndex = 0
    private var examRound = 1
    private val examResults = mutableListOf<GradeResult>()
    private var completedAttemptCount = 0
    private var hasStartedCurrentPage = false
    private var tutoringMessageSequence = 0
    private var tutoringRetryCount = 0
    private var sessionPlans: List<StudyPlan> = emptyList()
    private var questionPlan: Map<String, String> = emptyMap()
    private var previewing = false

    fun observe(listener: (StudySessionState) -> Unit) {
        listeners += listener
        listener(state)
    }

    fun removeObserver(listener: (StudySessionState) -> Unit) {
        listeners -= listener
    }

    fun start() {
        hasStartedCurrentPage = false
        update(
            state.copy(
                page = StudyPage.START,
                voiceCaption = "正在启动，请稍候",
                alertMessage = null
            )
        )
        handler.postDelayed({
            loadPlans()
        }, START_DELAY_MS)
    }

    /**
     * 仅调试构建使用：直接渲染指定页面，用来核对视觉稿。
     * 不启动计时器、不申请相机、不推进任何真实流程。
     */
    fun previewPage(page: StudyPage) {
        previewing = true
        val question = Question(
            id = "preview/q1",
            title = "例题：分数应用题",
            questionMarkdown = "一根绳子长 **24 米**，用去了 1/3。还剩多少米？",
            answer = "16 米",
            solutionMarkdown = "",
            knowledgePoints = listOf("求一个数的几分之几"),
            tutoringPrompt = "",
            gradingPrompt = ""
        )
        val plan = StudyPlan(
            id = "preview",
            title = "分数四则运算",
            subject = "数学 · 王老师",
            passScore = 80,
            exampleQuestions = listOf(question),
            examQuestions = listOf(question),
            estimatedMinutes = 25
        )
        state = StudySessionState(
            page = page,
            stage = StudyStage.EXAMPLE,
            selectedPlan = plan,
            currentQuestion = question,
            questionNumber = 6,
            remainingCount = 3,
            lastGradeResult = GradeResult(
                questionId = question.id,
                stage = StudyStage.EXAMPLE,
                score = 86.0,
                feedback = "思路正确：先算出用去的 8 米，再算剩余 16 米。单位写完整。",
                suggestion = "最后一步减法可以再检查一遍。",
                maxScore = 100.0
            ),
            voiceCaption = "请先读题，找出已知条件；你也可以直接说出卡住的地方。",
            tutoringMessages = listOf(
                TutoringMessage("p1", TutoringSpeaker.AI, "请先读题，找出已知条件；你也可以直接说出卡住的地方。"),
                TutoringMessage("p2", TutoringSpeaker.STUDENT, "用去了三分之一是什么意思？"),
                TutoringMessage("p3", TutoringSpeaker.AI, "想一想：24 米的三分之一，可以怎么算？")
            ),
            gradingStep = GradingStep.PREPARING,
            gradingCountdownSeconds = 5,
            resultCountdownSeconds = 3,
            totalExamScore = 92.0,
            examPassed = true,
            planScores = listOf(
                PlanScore(planId = "math", title = "分数四则运算", score = 92.0, passScore = 80, passed = true)
            ),
            completedQuestionCount = 10,
            mistakeCount = 2,
            doorStatus = DoorStatus.OPENED
        )
        listeners.forEach { it(state) }
    }

    /**
     * 学习计划可以多选：多份计划合并成一次学习会话。
     * 题量、预计时长相加；达标分取各计划平均值；题目 id 加计划前缀避免重名。
     */
    fun selectPlans(plans: List<StudyPlan>) {
        if (plans.isEmpty()) return
        val plan = if (plans.size == 1) plans.first() else mergePlans(plans)
        sessionPlans = plans
        questionPlan = if (plans.size == 1) {
            val only = plans.first()
            only.examQuestions.associate { it.id to only.id }
        } else {
            plans.flatMap { p -> p.examQuestions.map { "${p.id}/${it.id}" to p.id } }.toMap()
        }
        startSession(plan)
    }

    fun selectPlan(plan: StudyPlan) = selectPlans(listOf(plan))

    private fun mergePlans(plans: List<StudyPlan>): StudyPlan {
        fun qualify(question: Question, plan: StudyPlan): Question =
            question.copy(id = "${plan.id}/${question.id}")
        return StudyPlan(
            id = plans.joinToString("+") { it.id },
            title = "${plans.size} 项计划",
            subject = plans.map { it.subject }.distinct().joinToString(" / "),
            passScore = kotlin.math.round(plans.map { it.passScore }.average()).toInt(),
            exampleQuestions = plans.flatMap { plan -> plan.exampleQuestions.map { qualify(it, plan) } },
            examQuestions = plans.flatMap { plan -> plan.examQuestions.map { qualify(it, plan) } },
            estimatedMinutes = plans.sumOf { it.estimatedMinutes }
        )
    }

    private fun startSession(plan: StudyPlan) {
        activeStageQuestions = plan.exampleQuestions
        examRoundQuestions = plan.examQuestions
        exampleIndex = 0
        examIndex = 0
        examRound = 1
        examResults.clear()
        completedAttemptCount = 0
        update(
            state.copy(
                page = StudyPage.DEVICE_SELF_CHECK,
                stage = StudyStage.EXAMPLE,
                selectedPlan = plan,
                currentQuestion = null,
                remainingCount = plan.exampleQuestions.size,
                lastGradeResult = null,
                alertMessage = null,
                deviceChecks = initialDeviceChecks(),
                voiceCaption = "请把手机放入手机仓并关闭仓门，系统开始自检。",
                completedQuestionCount = 0,
                mistakeCount = 0,
                totalExamScore = 0.0,
                examPassed = null
            )
        )
        runDeviceSelfCheck()
    }

    fun enterCurrentPage() {
        if (previewing) return
        if (hasStartedCurrentPage) return
        hasStartedCurrentPage = true
        when (state.page) {
            StudyPage.TUTORING -> {
                startTutoringVoiceSession()
                waitForFlipToGrade()
            }
            StudyPage.EXAM -> waitForFlipToGrade()
            StudyPage.GRADING -> runGrading()
            StudyPage.GRADE_RESULT -> runResultCountdown()
            StudyPage.EXAM_SCORE -> runExamScorePage()
            else -> Unit
        }
    }

    fun release() {
        poseDetector.release()
        tutoringAiService.release()
        cameraCaptureService.release()
        voicePromptService.release()
    }

    fun bindGradingCameraPreview(textureView: TextureView) {
        cameraCaptureService.bindPreview(textureView)
    }

    private fun loadPlans() {
        studyPlanApi.getPlans { result ->
            result.fold(
                onSuccess = { plans ->
                    update(
                        state.copy(
                            page = StudyPage.PLAN_SELECT,
                            availablePlans = plans,
                            voiceCaption = "请选择学习计划。",
                            alertMessage = if (plans.isEmpty()) "暂无学习计划，请联系老师。" else null
                        )
                    )
                },
                onFailure = {
                    update(state.copy(page = StudyPage.PLAN_SELECT, alertMessage = "学习计划加载失败，请稍后重试。"))
                }
            )
        }
    }

    private fun runDeviceSelfCheck() {
        val names = listOf("相机", "麦克风", "网络", "姿态传感器", "USB 手机仓")
        names.forEachIndexed { index, name ->
            handler.postDelayed({
                val checks = names.mapIndexed { itemIndex, itemName ->
                    DeviceCheckItem(
                        name = itemName,
                        status = if (itemIndex <= index) DeviceCheckStatus.PASSED else DeviceCheckStatus.CHECKING
                    )
                }
                update(
                    state.copy(
                        deviceChecks = checks,
                        voiceCaption = "$name 检查通过。"
                    )
                )
                if (index == names.lastIndex) {
                    handler.postDelayed({ enterExampleQuestion() }, 300L)
                }
            }, (index + 1) * 1_000L)
        }
    }

    private fun enterExampleQuestion() {
        val question = activeStageQuestions.getOrNull(exampleIndex)
        if (question == null) {
            enterExamQuestion()
            return
        }
        tutoringRetryCount = 0
        update(
            state.copy(
                page = StudyPage.TUTORING,
                stage = StudyStage.EXAMPLE,
                currentQuestion = question,
                questionNumber = exampleIndex + 1,
                remainingCount = activeStageQuestions.size - exampleIndex,
                lastGradeResult = null,
                alertMessage = null,
                voiceCaption = "",
                tutoringMessages = emptyList()
            )
        )
    }

    private fun enterExamQuestion() {
        val plan = state.selectedPlan ?: return
        if (examRoundQuestions.isEmpty()) {
            examRoundQuestions = plan.examQuestions
        }
        activeStageQuestions = examRoundQuestions
        val question = activeStageQuestions.getOrNull(examIndex)
        if (question == null) {
            finishExamRound()
            return
        }
        update(
            state.copy(
                page = StudyPage.EXAM,
                stage = StudyStage.EXAM,
                currentQuestion = question,
                questionNumber = examIndex + 1,
                remainingCount = activeStageQuestions.size - examIndex,
                lastGradeResult = null,
                alertMessage = null,
                voiceCaption = "考试开始。写完后请翻转手机提交本题。"
            )
        )
    }

    private fun waitForFlipToGrade() {
        val page = state.page
        val caption = when (page) {
            StudyPage.TUTORING -> "完成后请翻转手机开始批卷。"
            StudyPage.EXAM -> "系统正在等待手机翻转。"
            else -> state.voiceCaption
        }
        update(state.copy(voiceCaption = caption))
        poseDetector.waitForFlip {
            if (state.page == page) {
                update(
                    state.copy(
                        page = StudyPage.GRADING,
                        gradingStep = GradingStep.PREPARING,
                        gradingCountdownSeconds = 0,
                        voiceCaption = "检测到翻转，请把答案完整放在画面中，然后按快门拍照。"
                    )
                )
                voicePromptService.speak("请把答案完整放在画面中，然后按快门拍照。")
            }
        }
    }

    private fun startTutoringVoiceSession() {
        val question = state.currentQuestion ?: return
        val questionId = question.id
        tutoringAiService.startSession(
            stage = state.stage,
            question = question,
            listener = object : TutoringAiListener {
                override fun onSessionStarted() {
                    if (isCurrentTutoringQuestion(questionId)) {
                        tutoringRetryCount = 0
                        update(
                            state.copy(
                                alertMessage = null
                            )
                        )
                    }
                }

                override fun onStudentSpeech(text: String) {
                    if (isCurrentTutoringQuestion(questionId)) {
                        appendTutoringMessage(TutoringSpeaker.STUDENT, text)
                        updateTutoringVoiceCaption(TutoringSpeaker.STUDENT, text)
                    }
                }

                override fun onCaption(text: String) {
                    if (isCurrentTutoringQuestion(questionId)) {
                        appendTutoringMessage(TutoringSpeaker.AI, text)
                        updateTutoringVoiceCaption(TutoringSpeaker.AI, text)
                    }
                }

                override fun onSubtitle(subtitle: TutoringSubtitle) {
                    if (isCurrentTutoringQuestion(questionId)) {
                        appendTutoringMessage(subtitle.speaker, subtitle.text)
                        updateTutoringVoiceCaption(subtitle.speaker, subtitle.text)
                    }
                }

                override fun onError(message: String) {
                    if (isCurrentTutoringQuestion(questionId)) {
                        handleTutoringError(questionId, message)
                    }
                }
            }
        )
    }

    /**
     * 批卷页只做手动拍照：进入页面后停在「准备拍摄」，等学生按下快门。
     */
    private fun runGrading() {
        state.currentQuestion ?: return
        update(
            state.copy(
                gradingStep = GradingStep.PREPARING,
                gradingCountdownSeconds = 0,
                voiceCaption = "请把答案完整放在画面中，然后按快门拍照。"
            )
        )
        voicePromptService.speak("请把答案完整放在画面中，然后按快门拍照。")
    }

    /** 学生按下快门。只在「准备拍摄」态响应，避免重复触发。 */
    fun captureNow() {
        if (state.page != StudyPage.GRADING) return
        if (state.gradingStep != GradingStep.PREPARING) return
        val question = state.currentQuestion ?: return
        captureAndGrade(question)
    }

    private fun captureAndGrade(question: Question) {
        update(state.copy(gradingStep = GradingStep.CAPTURING, voiceCaption = "正在拍摄纸面答案，请保持手机稳定。"))
        cameraCaptureService.captureAnswer { captureResult ->
            captureResult.fold(
                onSuccess = { imagePath ->
                    update(state.copy(gradingStep = GradingStep.UPLOADING, voiceCaption = "答案已拍摄，正在上传。"))
                    handler.postDelayed({
                        update(state.copy(gradingStep = GradingStep.GRADING, voiceCaption = "上传完成，AI 正在批改。"))
                        gradingAiService.grade(
                            stage = state.stage,
                            question = question,
                            imagePath = imagePath,
                            examRound = examRound,
                            questionMaxScore = currentQuestionMaxScore()
                        ) { gradeResult ->
                            gradeResult.fold(
                                onSuccess = { result -> waitForFaceUp(result) },
                                onFailure = { error ->
                                    update(
                                        state.copy(
                                            gradingStep = GradingStep.PREPARING,
                                            alertMessage = "AI 批改失败，请重新按快门拍摄。\n${error.message.orEmpty().take(MAX_ALERT_ERROR_LENGTH)}"
                                        )
                                    )
                                }
                            )
                        }
                    }, 900L)
                },
                onFailure = {
                    update(
                        state.copy(
                            gradingStep = GradingStep.PREPARING,
                            alertMessage = "拍摄失败，请保持手机稳定后重新按快门。"
                        )
                    )
                }
            )
        }
    }

    private fun waitForFaceUp(result: GradeResult) {
        update(
            state.copy(
                gradingStep = GradingStep.WAITING_FLIP_BACK,
                lastGradeResult = result,
                voiceCaption = "批改完成，请把手机翻回正面。"
            )
        )
        voicePromptService.speak("批改完成，请把手机翻回正面。")
        poseDetector.waitForFaceUp {
            update(
                state.copy(
                    page = StudyPage.GRADE_RESULT,
                    lastGradeResult = result,
                    resultCountdownSeconds = RESULT_COUNTDOWN_SECONDS,
                    completedQuestionCount = completedQuestionCountAfter(result),
                    voiceCaption = "本题结果已生成，3 秒后进入下一步。"
                )
            )
            voicePromptService.speak("本题结果已生成，三秒后进入下一步。")
        }
    }

    private fun runResultCountdown() {
        for (second in RESULT_COUNTDOWN_SECONDS downTo 1) {
            handler.postDelayed({
                if (state.page == StudyPage.GRADE_RESULT) {
                    update(state.copy(resultCountdownSeconds = second))
                }
            }, (RESULT_COUNTDOWN_SECONDS - second) * 1_000L)
        }
        handler.postDelayed({
            if (state.page == StudyPage.GRADE_RESULT) {
                advanceAfterResult()
            }
        }, RESULT_COUNTDOWN_SECONDS * 1_000L)
    }

    private fun advanceAfterResult() {
        val result = state.lastGradeResult ?: return
        if (result.stage == StudyStage.EXAMPLE) {
            exampleIndex += 1
            enterExampleQuestion()
            return
        }

        examResults += result
        examIndex += 1
        if (examIndex < activeStageQuestions.size) {
            enterExamQuestion()
        } else {
            finishExamRound()
        }
    }

    private fun finishExamRound() {
        state.selectedPlan ?: return
        val totalScore = ExamScoring.totalScore(examResults)
        val mistakes = ExamScoring.mistakes(examResults)
        val scores = ExamScoring.planScores(examResults, sessionPlans, questionPlan)
        val examPassed = scores.isNotEmpty() && scores.all { it.passed }
        val failedPlans = scores.filter { !it.passed }
        val caption = if (examPassed) {
            "考试成绩 ${scoreText(totalScore)} 分，已达标，正在打开手机仓。"
        } else if (failedPlans.isNotEmpty()) {
            "考试成绩 ${scoreText(totalScore)} 分，${failedPlans.first().title}未达标，10 秒后进入错题辅导。"
        } else {
            "考试成绩 ${scoreText(totalScore)} 分，未达标，10 秒后进入错题辅导。"
        }
        update(
            state.copy(
                page = StudyPage.EXAM_SCORE,
                stage = StudyStage.EXAM,
                currentQuestion = null,
                remainingCount = 0,
                totalExamScore = totalScore,
                mistakeCount = mistakes.size,
                examPassed = examPassed,
                planScores = scores,
                doorStatus = DoorStatus.OPENING,
                voiceCaption = caption
            )
        )
        voicePromptService.speak(caption)
    }

    private fun runExamScorePage() {
        val plan = state.selectedPlan ?: return
        if (state.examPassed == true) {
            runDoorOpen()
            return
        }

        handler.postDelayed({
            if (state.page == StudyPage.EXAM_SCORE && state.examPassed == false) {
                enterMistakeReview(plan)
            }
        }, EXAM_SCORE_FAIL_DELAY_MS)
    }

    private fun enterMistakeReview(plan: StudyPlan) {
        val previousExamQuestions = activeStageQuestions
        val mistakes = ExamScoring.mistakes(examResults)
        val mistakesWithOriginals = mistakes.map { result ->
            val original = previousExamQuestions.firstOrNull { it.id == result.questionId }
                ?: plan.examQuestions.firstOrNull { it.id == result.questionId }
            result to original
        }
        examRoundQuestions = mistakesWithOriginals.mapNotNull { it.second }
            .ifEmpty { previousExamQuestions.ifEmpty { plan.examQuestions } }
        activeStageQuestions = mistakesWithOriginals.mapIndexed { index, (result, original) ->
            Question(
                id = "review-${examRound}-${index + 1}",
                title = "错题讲解 ${index + 1}",
                questionMarkdown = original?.questionMarkdown ?: "请复盘本题的关键步骤。",
                answer = original?.answer.orEmpty(),
                solutionMarkdown = original?.solutionMarkdown.orEmpty(),
                knowledgePoints = original?.knowledgePoints.orEmpty(),
                tutoringPrompt = original?.tutoringPrompt ?: "引导学生复盘本题条件、关系式和易错点。不要直接告诉最终答案。",
                gradingPrompt = original?.gradingPrompt ?: "检查学生是否修正了本题关键思路和计算过程。",
                source = QuestionSource.MISTAKE_REVIEW
            )
        }.ifEmpty { plan.examQuestions }
        exampleIndex = 0
        examIndex = 0
        examRound += 1
        examResults.clear()
        update(
            state.copy(
                stage = StudyStage.EXAMPLE,
                mistakeCount = activeStageQuestions.size,
                examPassed = null,
                voiceCaption = "本轮考试未达标，错题已整理成新的例题。"
            )
        )
        handler.postDelayed({ enterExampleQuestion() }, 800L)
    }

    private fun currentQuestionMaxScore(): Double {
        return if (state.stage == StudyStage.EXAM) {
            ExamScoring.questionMaxScore(activeStageQuestions.size)
        } else {
            ExamScoring.TOTAL_EXAM_SCORE
        }
    }

    private fun runDoorOpen() {
        doorControlService.openDoor { result ->
            result.fold(
                onSuccess = {
                    update(
                        state.copy(
                            doorStatus = DoorStatus.OPENED,
                            voiceCaption = "手机仓已打开，本次学习完成。"
                        )
                    )
                    voicePromptService.speak("手机仓已打开，本次学习完成。")
                },
                onFailure = {
                    update(
                        state.copy(
                            doorStatus = DoorStatus.FAILED,
                            alertMessage = "开门失败，请联系老师或工作人员。",
                            voiceCaption = "开门失败，请联系老师或工作人员。"
                        )
                    )
                    voicePromptService.speak("开门失败，请联系老师或工作人员。")
                }
            )
        }
    }

    private fun completedQuestionCountAfter(result: GradeResult): Int {
        completedAttemptCount += 1
        return completedAttemptCount
    }

    private fun scoreText(score: Double): String {
        val rounded = round(score * 10.0) / 10.0
        return if (abs(rounded % 1.0) < 0.0001) {
            rounded.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", rounded)
        }
    }

    private fun initialDeviceChecks(): List<DeviceCheckItem> {
        return listOf("相机", "麦克风", "网络", "姿态传感器", "USB 手机仓").map {
            DeviceCheckItem(it, DeviceCheckStatus.CHECKING)
        }
    }

    private fun appendTutoringMessage(speaker: TutoringSpeaker, text: String) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return

        val currentMessages = state.tutoringMessages
        val messages = if (currentMessages.lastOrNull()?.speaker == speaker) {
            currentMessages.dropLast(1) + newTutoringMessage(speaker, cleanText)
        } else {
            currentMessages + newTutoringMessage(speaker, cleanText)
        }.takeLast(MAX_TUTORING_MESSAGES)

        update(state.copy(tutoringMessages = messages))
    }

    private fun newTutoringMessage(speaker: TutoringSpeaker, text: String): TutoringMessage {
        tutoringMessageSequence += 1
        return TutoringMessage(
            id = "tutor-message-$tutoringMessageSequence",
            speaker = speaker,
            text = text
        )
    }

    private fun isCurrentTutoringQuestion(questionId: String): Boolean {
        return state.page == StudyPage.TUTORING && state.currentQuestion?.id == questionId
    }

    private fun updateTutoringVoiceCaption(speaker: TutoringSpeaker, text: String) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return

        val prefix = when (speaker) {
            TutoringSpeaker.STUDENT -> "你说"
            TutoringSpeaker.AI -> "AI"
        }
        update(state.copy(voiceCaption = "$prefix：$cleanText"))
    }

    private fun handleTutoringError(questionId: String, message: String) {
        if (!shouldRetryTutoring(message) || tutoringRetryCount >= MAX_TUTORING_RETRY_COUNT) {
            update(
                state.copy(
                    alertMessage = message
                )
            )
            return
        }

        tutoringRetryCount += 1
        update(
            state.copy(
                alertMessage = "$message\n正在第 $tutoringRetryCount 次重连语音辅导。"
            )
        )
        handler.postDelayed({
            if (isCurrentTutoringQuestion(questionId)) {
                tutoringAiService.stopSession()
                startTutoringVoiceSession()
            }
        }, TUTORING_RETRY_DELAY_MS)
    }

    private fun shouldRetryTutoring(message: String): Boolean {
        return !message.contains("未配置") &&
            !message.contains("权限") &&
            !message.contains("通话") &&
            !message.contains("麦克风") &&
            !message.contains("AppID") &&
            !message.contains("AppKey") &&
            !message.contains("Token")
    }

    private fun update(newState: StudySessionState) {
        val previousPage = state.page
        if (previousPage == StudyPage.TUTORING && newState.page != StudyPage.TUTORING) {
            tutoringAiService.stopSession()
        }
        if (previousPage != newState.page) {
            poseDetector.cancel()
        }
        state = newState
        if (previousPage != newState.page) {
            hasStartedCurrentPage = false
        }
        listeners.forEach { it(state) }
    }

    private companion object {
        const val MAX_TUTORING_MESSAGES = 6
        const val MAX_TUTORING_RETRY_COUNT = 2
        const val START_DELAY_MS = 3_000L
        const val RESULT_COUNTDOWN_SECONDS = 3
        const val TUTORING_RETRY_DELAY_MS = 1_500L
        const val EXAM_SCORE_FAIL_DELAY_MS = 10_000L
        const val MAX_ALERT_ERROR_LENGTH = 120
    }
}
