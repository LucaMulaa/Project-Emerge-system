import {cleanup, fireEvent, render, screen} from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { NeighborhoodControl } from "./NeighborhoodControl";

function fakeService(answer: unknown): ReturnType<typeof vi.fn> {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(answer), { status: 200 }));
    vi.stubGlobal("fetch", fetchMock);
    return fetchMock;
}

afterEach(() => {
    cleanup();
    vi.unstubAllGlobals();
});

describe("neighborhood control", () => {
    it("shows the neighborhood of the service", async () => {
        fakeService({ type: "RADIUS", radius: 0.3 });
        render(<NeighborhoodControl />);
        // findBy... WAITS for the GET to answer; getBy... would look only once, too early
        expect(await screen.findByRole("button", { name: "Raggio" })).toHaveAttribute("aria-pressed", "true");
        expect(screen.getByRole("button", { name: "Tutti" })).toHaveAttribute("aria-pressed", "false");
        expect(screen.getByRole("slider", { name: "Raggio del vicinato in metri" })).toHaveValue("0.3");
        expect(screen.getByText("0.30 m")).toBeInTheDocument();
    });

    it("moving the slider sends the new radius", async () => {
        const fetchMock = fakeService({ type: "RADIUS", radius: 0.3 });
        render(<NeighborhoodControl />);
        const slider = await screen.findByRole("slider", { name: "Raggio del vicinato in metri" });

        fireEvent.change(slider, { target: { value: "1" } });

        const [, options] = fetchMock.mock.calls.at(-1) as [string, RequestInit];
        expect(options.method).toBe("POST");
        expect(options.body).toBe(JSON.stringify({ type: "RADIUS", radius: 1 }));
    });

    it("says when the service is unreachable", async () => {
        vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("Failed to fetch")));
        render(<NeighborhoodControl />);
        expect(await screen.findByText("Servizio del vicinato non raggiungibile")).toBeInTheDocument();
    });
});