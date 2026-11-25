import { useEffect, useState } from "react";
import { fetchNeighborhood, updateNeighborhood, type NeighborhoodSettings } from "../services/neighborhood-client";
import { useLocale } from "../services/locale-context";

const MIN_RADIUS_M = 0.1;
const MAX_RADIUS_M = 2;
const RADIUS_STEP_M = 0.05;

/** Lets the user switch the neighborhood between everyone and a radius, from the dashboard. */
export function NeighborhoodControl(): React.JSX.Element {
  const { t } = useLocale();

  const [settings, setSettings] = useState<NeighborhoodSettings | null>(null);
  const [unreachable, setUnreachable] = useState(false);

  useEffect(() => {
    fetchNeighborhood()
      .then((current) => setSettings(current))
      .catch(() => setUnreachable(true));
  }, []);

  // Shows the new neighborhood at once, then tells the service.
  function apply(next: NeighborhoodSettings): void {
    setSettings(next);
    updateNeighborhood(next)
      .then(() => setUnreachable(false))
      .catch(() => setUnreachable(true));
  }

  // Until the service answers there is nothing to show but a short message.
  if (!settings) {
    return <span className="neighborhood-control">{unreachable ? t.neighborhood.unreachable : t.neighborhood.loading}</span>;
  }

  return (
    <div className="neighborhood-control">
      <div className="segmented-control" aria-label={t.neighborhood.ariaLabel}>
        <button
          type="button"
          className={settings.type === "FULL" ? "active" : ""}
          aria-pressed={settings.type === "FULL"}
          onClick={() => apply({ ...settings, type: "FULL" })}
        >
          {t.neighborhood.full}
        </button>
        <button
          type="button"
          className={settings.type === "RADIUS" ? "active" : ""}
          aria-pressed={settings.type === "RADIUS"}
          onClick={() => apply({ ...settings, type: "RADIUS" })}
        >
          {t.neighborhood.radius}
        </button>
      </div>
      {settings.type === "RADIUS" && (
        <label className="neighborhood-radius">
          <input
            type="range"
            min={MIN_RADIUS_M}
            max={MAX_RADIUS_M}
            step={RADIUS_STEP_M}
            value={settings.radius}
            aria-label={t.neighborhood.radiusAria}
            onChange={(event) => apply({ ...settings, radius: Number(event.target.value) })}
          />
          {settings.radius.toFixed(2)} m
        </label>
      )}
      {unreachable && <span className="neighborhood-error">{t.neighborhood.unreachable}</span>}
    </div>
  );
}
