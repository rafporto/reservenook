"use client";

import { CompanyBackofficeData, Drafts, Feedback } from "@/features/app/company/company-backoffice-types";

export type UpdateDrafts = (updater: (current: Drafts) => Drafts) => void;

export type SaveSection = <TResponse extends { message?: string } | null>(
  section: string,
  endpoint: string,
  method: "PUT" | "POST",
  body: Record<string, unknown>,
  apply: (payload: TResponse) => void
) => Promise<void>;

export type SectionProps = {
  slug: string;
  data: CompanyBackofficeData;
  drafts: Drafts;
  feedback: Feedback;
  saving: string | null;
  updateDrafts: UpdateDrafts;
  saveSection: SaveSection;
  setSectionFeedback: (section: string, next: Feedback[string]) => void;
  applyData: (data: CompanyBackofficeData) => void;
};
