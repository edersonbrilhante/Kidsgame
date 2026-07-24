#!/usr/bin/env python3
"""
Build-time helper: download Twemoji PNGs for every emoji the app uses and place them in
app/src/main/assets/twemoji/<codepoints>.png. Runtime stays fully offline — these are
bundled into the APK. Missing icons are skipped; the app falls back to the system emoji.

Twemoji graphics are CC-BY 4.0 (https://github.com/jdecked/twemoji).
"""
import glob
import os
import re
import sys
import urllib.request

VER = "15.1.0"
BASE = f"https://cdn.jsdelivr.net/gh/jdecked/twemoji@{VER}/assets/72x72"
OUT = "app/src/main/assets/twemoji"

# Emojis used as glyphs / specials that are not written as a 4-arg Word(...).
EXTRAS = [
    "⚽", "🐲", "🕷️", "🦾", "💪", "🐶", "🐭", "🐰", "⛄", "⚡", "🏠",
    "🧩", "🎈", "🔢", "🔤", "🎨", "🔎", "🧠", "🔟",
    "🌈", "⭐", "🍎", "🐱", "🐟", "❓", "🎉", "🔊", "🎁", "👑", "🎩", "🦸",
]


def collect_emojis():
    found = set(EXTRAS)
    pattern = re.compile(
        r'Word\(\s*"[^"]*"\s*,\s*"[^"]*"\s*,\s*"[^"]*"\s*,\s*"([^"]+)"\s*\)'
    )
    for path in glob.glob("app/src/main/java/**/*.kt", recursive=True):
        with open(path, encoding="utf-8") as f:
            for m in pattern.finditer(f.read()):
                found.add(m.group(1))
    return found


def key_for(emoji):
    """Twemoji filename: hex codepoints joined by '-', dropping VS16 (fe0f)."""
    return "-".join(f"{ord(c):x}" for c in emoji if ord(c) != 0xFE0F)


def download(codes, dest):
    try:
        req = urllib.request.Request(f"{BASE}/{codes}.png", headers={"User-Agent": "matteo-games"})
        with urllib.request.urlopen(req, timeout=20) as r, open(dest, "wb") as out:
            out.write(r.read())
        return True
    except Exception:
        return False


def main():
    os.makedirs(OUT, exist_ok=True)
    emojis = collect_emojis()
    ok, missing = 0, []
    for e in sorted(emojis):
        codes = key_for(e)
        if not codes:
            continue
        dest = os.path.join(OUT, codes + ".png")
        if os.path.exists(dest):
            ok += 1
            continue
        # primary (fe0f stripped), then fall back to full codepoints
        full = "-".join(f"{ord(c):x}" for c in e)
        if download(codes, dest) or (full != codes and download(full, dest)):
            ok += 1
        else:
            missing.append((e, codes))
    print(f"twemoji: {ok} downloaded, {len(missing)} missing")
    for e, c in missing:
        print(f"  missing: {e!r} ({c})")
    return 0  # never fail the build; app falls back to system emoji


if __name__ == "__main__":
    sys.exit(main())
