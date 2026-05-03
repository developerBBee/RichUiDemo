package jp.developer.bbee.richuidemo.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class MyScaffoldIssueRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(MyScaffoldPaddingDetector.ISSUE)

    override val api: Int
        get() = CURRENT_API
}

