# iDash JAKIM calendar

Power was replaced and the dashboard Favorites section removed at the owner's request. The dock retains Bluetooth, Files and YouTube.

The widget starts without a selected zone. Tap it to choose SGR01/02/03 or WLY01, which have official2026 annual cache files reused from NexaDash. Select another valid zone code and request **Kemas kini daripada JAKIM** to download its annual data. Refresh uses the official HTTPS e-Solat endpoint with8-second connect/read timeouts, a2MiB response cap and zone/status/date/time validation. A failed refresh preserves previously valid data. No cloud live-endpoint pass is claimed.

Today's row is selected by the exact Malaysia date (`Asia/Kuala_Lumpur`) based on Android system time. There is no date rollover fallback to a different year, astronomical estimate, unverified countdown or alarm. The Hijri string comes from that official row. Set the device date correctly before relying on the displayed day. A tap menu also lets the user browse a specific date and see all six times in a full dialog.

Zone selection and visibility are stored independently in iDash's own preferences. No location/notification/alarm permission or NexaDash installation is required. The parser's date/time validation was adapted from NexaDash with its MIT notice retained; official data origin/retrieval/hashes are bundled in `app/src/main/assets/jakim/source.json`.

The annual cache covers2026 only. New-year or missing-zone data must be refreshed. The UI explicitly shows missing date coverage and a download prompt; stale-day times must not be presented as current.
