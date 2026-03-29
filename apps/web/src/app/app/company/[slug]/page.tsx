import { CompanyBackofficeScreen } from "@/features/app/company/company-backoffice-screen";

type CompanyBackofficePageProps = {
  params: Promise<{ slug: string }>;
};

export default async function CompanyBackofficePage({ params }: CompanyBackofficePageProps) {
  const { slug } = await params;

  return <CompanyBackofficeScreen slug={slug} />;
}
