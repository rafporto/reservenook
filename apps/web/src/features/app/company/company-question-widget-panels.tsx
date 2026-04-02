"use client";

import { Alert, Button, Checkbox, FormControlLabel, Grid, MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import { domainPattern, questionTypes, splitTextList, widgetThemes, CompanyBackofficeData } from "@/features/app/company/company-backoffice-types";
import { SectionProps } from "@/features/app/company/company-backoffice-panel-types";

export function CustomerQuestionsAndWidgetPanels(props: SectionProps) {
  const { slug, data, drafts, feedback, saving, updateDrafts, saveSection, setSectionFeedback, applyData } = props;

  return (
    <Grid container spacing={3}>
      <Grid size={{ xs: 12, xl: 7 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2} component="form" onSubmit={(event) => {
            event.preventDefault();
            void saveSection<{
              message: string;
              customerQuestions: CompanyBackofficeData["customerQuestions"];
            }>("questions", `/api/app/company/${slug}/customer-questions`, "PUT", {
              entries: drafts.questions.map((question, index) => ({
                label: question.label.trim(),
                questionType: question.questionType,
                required: question.required,
                enabled: question.enabled,
                displayOrder: index,
                options: splitTextList(question.optionsText)
              }))
            }, (payload) => applyData({ ...data, customerQuestions: payload.customerQuestions }));
          }}>
            <Stack direction="row" justifyContent="space-between">
              <Typography variant="h5">Customer Questions</Typography>
              <Button onClick={() => updateDrafts((current) => ({
                ...current,
                questions: [...current.questions, { label: "", questionType: "SHORT_TEXT", required: false, enabled: true, displayOrder: current.questions.length, optionsText: "" }]
              }))}>Add question</Button>
            </Stack>
            {feedback.questions ? <Alert severity={feedback.questions.type}>{feedback.questions.message}</Alert> : null}
            {drafts.questions.map((question, index) => (
              <Paper key={`${question.label}-${index}`} sx={{ p: 2 }} variant="outlined">
                <Stack spacing={1}>
                  <TextField label="Question label" value={question.label} onChange={(event) => updateDrafts((current) => ({
                    ...current,
                    questions: current.questions.map((item, itemIndex) => itemIndex === index ? { ...item, label: event.target.value } : item)
                  }))} fullWidth />
                  <TextField label="Question type" value={question.questionType} onChange={(event) => updateDrafts((current) => ({
                    ...current,
                    questions: current.questions.map((item, itemIndex) => itemIndex === index ? { ...item, questionType: event.target.value } : item)
                  }))} select fullWidth>
                    {questionTypes.map((type) => <MenuItem key={type} value={type}>{type}</MenuItem>)}
                  </TextField>
                  <FormControlLabel control={<Checkbox checked={question.required} onChange={(event) => updateDrafts((current) => ({
                    ...current,
                    questions: current.questions.map((item, itemIndex) => itemIndex === index ? { ...item, required: event.target.checked } : item)
                  }))} />} label="Required" />
                  <FormControlLabel control={<Checkbox checked={question.enabled} onChange={(event) => updateDrafts((current) => ({
                    ...current,
                    questions: current.questions.map((item, itemIndex) => itemIndex === index ? { ...item, enabled: event.target.checked } : item)
                  }))} />} label="Enabled" />
                  {question.questionType === "SINGLE_SELECT" ? <TextField label="Selectable options" helperText="One option per line" value={question.optionsText} onChange={(event) => updateDrafts((current) => ({
                    ...current,
                    questions: current.questions.map((item, itemIndex) => itemIndex === index ? { ...item, optionsText: event.target.value } : item)
                  }))} multiline minRows={3} fullWidth /> : null}
                  <Stack direction="row" spacing={1}>
                    <Button disabled={index === 0} onClick={() => updateDrafts((current) => {
                      const next = [...current.questions];
                      [next[index - 1], next[index]] = [next[index], next[index - 1]];
                      return { ...current, questions: next };
                    })}>Move up</Button>
                    <Button disabled={index === drafts.questions.length - 1} onClick={() => updateDrafts((current) => {
                      const next = [...current.questions];
                      [next[index], next[index + 1]] = [next[index + 1], next[index]];
                      return { ...current, questions: next };
                    })}>Move down</Button>
                    <Button color="error" onClick={() => updateDrafts((current) => ({
                      ...current,
                      questions: current.questions.filter((_, itemIndex) => itemIndex !== index)
                    }))}>Remove</Button>
                  </Stack>
                </Stack>
              </Paper>
            ))}
            <Button type="submit" variant="contained" disabled={saving === "questions"}>{saving === "questions" ? "Saving questions..." : "Save customer questions"}</Button>
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, xl: 5 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2} component="form" onSubmit={(event) => {
            event.preventDefault();
            if (splitTextList(drafts.widget.allowedDomainsText).some((domain) => !domainPattern.test(domain.toLowerCase()))) {
              setSectionFeedback("widget", { type: "error", message: "Allowed widget domains must be valid host names." });
              return;
            }
            void saveSection<{
              message: string;
              widgetSettings: CompanyBackofficeData["widgetSettings"];
            }>("widget", `/api/app/company/${slug}/widget-settings`, "PUT", {
              ctaLabel: drafts.widget.ctaLabel.trim() || null,
              widgetEnabled: drafts.widget.widgetEnabled,
              allowedDomains: splitTextList(drafts.widget.allowedDomainsText),
              themeVariant: drafts.widget.themeVariant
            }, (payload) => applyData({ ...data, widgetSettings: payload.widgetSettings }));
          }}>
            <Typography variant="h5">Widget Settings</Typography>
            {feedback.widget ? <Alert severity={feedback.widget.type}>{feedback.widget.message}</Alert> : null}
            <TextField label="CTA label" value={drafts.widget.ctaLabel} onChange={(event) => updateDrafts((current) => ({ ...current, widget: { ...current.widget, ctaLabel: event.target.value } }))} fullWidth />
            <FormControlLabel control={<Checkbox checked={drafts.widget.widgetEnabled} onChange={(event) => updateDrafts((current) => ({ ...current, widget: { ...current.widget, widgetEnabled: event.target.checked } }))} />} label="Widget enabled" />
            <TextField label="Theme variant" value={drafts.widget.themeVariant} onChange={(event) => updateDrafts((current) => ({ ...current, widget: { ...current.widget, themeVariant: event.target.value } }))} select fullWidth>
              {widgetThemes.map((theme) => <MenuItem key={theme} value={theme}>{theme}</MenuItem>)}
            </TextField>
            <TextField label="Allowed domains" helperText="One host name per line" value={drafts.widget.allowedDomainsText} onChange={(event) => updateDrafts((current) => ({ ...current, widget: { ...current.widget, allowedDomainsText: event.target.value } }))} multiline minRows={4} fullWidth />
            <Button type="submit" variant="contained" disabled={saving === "widget"}>{saving === "widget" ? "Saving widget settings..." : "Save widget settings"}</Button>
          </Stack>
        </Paper>
      </Grid>
    </Grid>
  );
}
