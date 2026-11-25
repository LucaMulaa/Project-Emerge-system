import { z } from "zod";

export const NeighborhoodSettingsSchema = z.object({
    type: z.enum(["RADIUS", "FULL"]),
    radius: z.number().positive(),
});

export type NeighborhoodSettings = z.infer<typeof NeighborhoodSettingsSchema>;

function neighborhoodUrl(): string {
    const configured = import.meta.env.VITE_NEIGHBORHOOD_URL as string | undefined;
    if (configured) return configured;
    return `${window.location.protocol}//${window.location.hostname}:5000/neighborhood`;
}

/** Reads the neighborhood that the service is using right now. */
export async function fetchNeighborhood(): Promise<NeighborhoodSettings> {
    const response = await fetch(neighborhoodUrl());
    if (!response.ok) throw new Error(`Neighborhood service answered ${response.status}`);
    return NeighborhoodSettingsSchema.parse(await response.json());
}

/**
 * Asks the service to use a new neighborhood.
 * @param settings the neighborhood type and radius to use from now on.
 */
export async function updateNeighborhood(settings: NeighborhoodSettings): Promise<void> {
    const response = await fetch(neighborhoodUrl(), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(settings),
    });
    if (!response.ok) throw new Error(`Neighborhood service answered ${response.status}`);
}