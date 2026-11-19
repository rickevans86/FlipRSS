package com.fliprss.app.browser

import android.app.Activity
import android.app.AlertDialog
import android.widget.EditText
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSession.PromptDelegate
import org.mozilla.geckoview.GeckoSession.PromptDelegate.*

/** Common web form/JS prompts, attached only while the browser Activity is resumed. */
internal class BrowserPrompts(private val activity: Activity) : PromptDelegate {
    private val dialogs = mutableSetOf<AlertDialog>()
    private fun show(prompt: BasePrompt, message: String?, configure: (AlertDialog.Builder, (() -> PromptResponse) -> Unit) -> Unit): GeckoResult<PromptResponse> {
        val result = GeckoResult<PromptResponse>()
        var activeDialog: AlertDialog? = null
        fun finish(response: () -> PromptResponse) {
            if (!prompt.isComplete) result.complete(response())
            activeDialog?.dismiss()
        }
        val builder = AlertDialog.Builder(activity).setTitle("Web page message").setMessage(message)
        configure(builder, ::finish)
        val dialog = builder.create(); activeDialog = dialog; dialogs.add(dialog)
        dialog.setOnDismissListener {
            dialogs.remove(dialog)
            if (!prompt.isComplete) result.complete(prompt.dismiss())
        }
        prompt.setDelegate(object : PromptInstanceDelegate {
            override fun onPromptDismiss(prompt: BasePrompt) { dialog.dismiss() }
        })
        dialog.show()
        return result
    }
    override fun onAlertPrompt(session: GeckoSession, prompt: AlertPrompt) = show(prompt, prompt.message) { builder, finish ->
        builder.setPositiveButton("OK") { _, _ -> finish { prompt.dismiss() } }
    }
    override fun onButtonPrompt(session: GeckoSession, prompt: ButtonPrompt) = show(prompt, prompt.message) { builder, finish ->
        builder.setPositiveButton("OK") { _, _ -> finish { prompt.confirm(ButtonPrompt.Type.POSITIVE) } }
        builder.setNegativeButton("Cancel") { _, _ -> finish { prompt.confirm(ButtonPrompt.Type.NEGATIVE) } }
    }
    override fun onTextPrompt(session: GeckoSession, prompt: TextPrompt) = show(prompt, prompt.message) { builder, finish ->
        val field = EditText(activity).apply { setText(prompt.defaultValue.orEmpty()); setSingleLine(true) }
        builder.setView(field).setPositiveButton("OK") { _, _ -> finish { prompt.confirm(field.text.toString()) } }
            .setNegativeButton("Cancel") { _, _ -> finish { prompt.dismiss() } }
    }
    override fun onChoicePrompt(session: GeckoSession, prompt: ChoicePrompt): GeckoResult<PromptResponse> {
        fun flatten(items: Array<ChoicePrompt.Choice>): List<ChoicePrompt.Choice> = items.flatMap {
            when { it.disabled || it.separator -> emptyList(); it.items != null -> flatten(it.items!!); else -> listOf(it) }
        }
        val choices = flatten(prompt.choices)
        return show(prompt, null) { builder, finish ->
            val labels = choices.map { it.label }.toTypedArray()
            if (prompt.type == ChoicePrompt.Type.MULTIPLE) {
                val selected = choices.filter { it.selected }.map { it.id }.toMutableSet()
                builder.setMultiChoiceItems(labels, choices.map { it.selected }.toBooleanArray()) { _, index, checked ->
                    if (checked) selected.add(choices[index].id) else selected.remove(choices[index].id)
                }.setPositiveButton("OK") { _, _ -> finish { prompt.confirm(selected.toTypedArray()) } }
            } else {
                builder.setSingleChoiceItems(labels, choices.indexOfFirst { it.selected }) { _, index -> finish { prompt.confirm(choices[index].id) } }
            }
            builder.setNegativeButton("Cancel") { _, _ -> finish { prompt.dismiss() } }
        }
    }
    fun close() { dialogs.toList().forEach { it.dismiss() }; dialogs.clear() }
}
