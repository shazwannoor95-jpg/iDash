# User-linked PremiumMods repository

Reviewed: https://github.com/Rakshittips/PremiumMods at commit2886e4fc3273640966a774e1324891755f78946f.

The repository contains README.md, MRiOS.json, MRkeyboard.json, MRTelegram.json, Telegrampremium.json, MRStatus.json, MRLiquidDynamic.json and moviebox.html. The JSON files are update/promotion metadata, including Telegram update links. The README describes Android APK modding and iOS-style UI/fonts/emojis.

moviebox.html is a MovieBox promotional page with generic CSS font/transition rules. No buildable Android launcher source, font binaries, emoji font, Android theme renderer or license file was found in this repository snapshot. The marketing descriptions do not establish implementation or ownership of every component inside the uploaded APK. No linked Telegram app or update was installed or executed for this review.

The actual SF Pro font files used by iDash came from the owner-uploaded MRiOS V2.apk, not this repository. That APK contains four SF Pro Display TTFs; font-table inspection did not find a separate color-emoji font or COLR/CPAL/CBDT/CBLC/sbix tables in them. iDash continues using device emoji fallback. Glass/transparency and transitions are API26-compatible iDash rendering and animation, not an imported MRiOS executable engine.
