"""
Genera los iconos de lanzador PNG (legacy, API 24-25) para PuenteLab.
Para API 26+ se usa el icono adaptativo vectorial en mipmap-anydpi-v26.
Uso: python3 tools/generate_launcher_icons.py
"""
from PIL import Image, ImageDraw
import os

SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

BLUE_900 = (13, 42, 74, 255)
BLUE_700 = (22, 74, 122, 255)
AMBER = (245, 195, 76, 255)
ORANGE = (242, 153, 74, 255)

OUT_ROOT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res")


def draw_icon(size: int) -> Image.Image:
    img = Image.new("RGBA", (size, size), BLUE_900)
    d = ImageDraw.Draw(img)
    s = size / 108.0

    # river band
    d.rectangle([0, 68 * s, size, size], fill=BLUE_700)

    # deck
    d.rectangle([22 * s, 66 * s, 86 * s, 74 * s], fill=AMBER)

    # truss zigzag
    pts = [(24 * s, 66 * s), (34 * s, 50 * s), (44 * s, 66 * s), (54 * s, 50 * s),
           (64 * s, 66 * s), (74 * s, 50 * s), (84 * s, 66 * s)]
    d.line(pts, fill=ORANGE, width=max(2, int(4 * s)))

    # supports
    d.polygon([(20 * s, 74 * s), (30 * s, 74 * s), (26 * s, 86 * s), (16 * s, 86 * s)], fill=ORANGE)
    d.polygon([(78 * s, 74 * s), (88 * s, 74 * s), (92 * s, 86 * s), (82 * s, 86 * s)], fill=ORANGE)

    return img


def main():
    for folder, size in SIZES.items():
        out_dir = os.path.join(OUT_ROOT, folder)
        os.makedirs(out_dir, exist_ok=True)
        icon = draw_icon(size)
        icon.save(os.path.join(out_dir, "ic_launcher.png"))
        icon.save(os.path.join(out_dir, "ic_launcher_round.png"))
        print(f"OK {folder} ({size}x{size})")


if __name__ == "__main__":
    main()
