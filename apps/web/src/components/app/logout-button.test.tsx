import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { LogoutButton } from "@/components/app/logout-button";

const push = vi.fn();

vi.mock("next/navigation", async () => {
  const actual = await vi.importActual<typeof import("next/navigation")>("next/navigation");

  return {
    ...actual,
    useRouter: () => ({
      push
    })
  };
});

describe("LogoutButton", () => {
  afterEach(() => {
    vi.restoreAllMocks();
    push.mockReset();
  });

  it("redirects to the public login page after logout", async () => {
    const fetchSpy = vi
      .spyOn(global, "fetch")
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            token: "csrf-token"
          }),
          {
            status: 200,
            headers: { "Content-Type": "application/json" }
          }
        )
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            redirectTo: "/en/login"
          }),
          {
            status: 200,
            headers: { "Content-Type": "application/json" }
          }
        )
      );

    render(<LogoutButton />);

    fireEvent.click(screen.getByRole("button", { name: "Logout" }));

    await waitFor(() => {
      expect(fetchSpy).toHaveBeenCalledTimes(2);
      expect(push).toHaveBeenCalledWith("/en/login");
    });
  });
});
