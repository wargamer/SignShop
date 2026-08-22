#!/usr/bin/env python3
"""
Generate SignShop material translation files from Minecraft 1.21 language files.

This script:
1. Downloads Minecraft 1.21 language files from Mojang's asset server
2. Extracts material translations (block.minecraft.*, item.minecraft.*)
3. Maps them to Bukkit Material enum names
4. Generates YAML files for each locale

Usage: python generate_material_translations.py
Output: ../src/main/resources/materials-translated/materials-{locale}.yml
"""

import json
import os
import re
import urllib.request
from pathlib import Path

# Minecraft version to use
MC_VERSION = "1.21"

# Output directory
SCRIPT_DIR = Path(__file__).parent
OUTPUT_DIR = SCRIPT_DIR.parent / "src" / "main" / "resources" / "materials-translated"

# Mojang API endpoints
VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json"
ASSET_BASE_URL = "https://resources.download.minecraft.net"


def download_json(url: str) -> dict:
    """Download and parse JSON from URL."""
    print(f"  Downloading: {url[:80]}...")
    with urllib.request.urlopen(url) as response:
        return json.loads(response.read().decode('utf-8'))


def get_version_info(version: str) -> dict:
    """Get version metadata from Mojang."""
    manifest = download_json(VERSION_MANIFEST_URL)
    for v in manifest["versions"]:
        if v["id"] == version:
            return download_json(v["url"])
    raise ValueError(f"Version {version} not found")


def get_asset_index(version_info: dict) -> dict:
    """Get asset index containing file hashes."""
    asset_index_url = version_info["assetIndex"]["url"]
    return download_json(asset_index_url)


def download_lang_file(asset_hash: str) -> dict:
    """Download a language file from Mojang's asset server."""
    # Assets are stored by first 2 chars of hash
    url = f"{ASSET_BASE_URL}/{asset_hash[:2]}/{asset_hash}"
    return download_json(url)


def minecraft_key_to_material(key: str) -> str:
    """
    Convert Minecraft translation key to Bukkit Material enum name.

    Examples:
        block.minecraft.stone -> STONE
        item.minecraft.diamond_sword -> DIAMOND_SWORD
        block.minecraft.oak_planks -> OAK_PLANKS
    """
    # Extract the material name from the key
    match = re.match(r'^(block|item)\.minecraft\.(.+)$', key)
    if not match:
        return None

    material_name = match.group(2)
    # Convert to uppercase and replace dots with underscores
    return material_name.upper().replace(".", "_")


def extract_material_translations(lang_data: dict) -> dict:
    """Extract material translations from language data."""
    materials = {}

    for key, value in lang_data.items():
        # Only process block and item translations
        if not (key.startswith("block.minecraft.") or key.startswith("item.minecraft.")):
            continue

        material = minecraft_key_to_material(key)
        if material and material not in materials:
            # Escape any special YAML characters in the value
            materials[material] = value

    return materials


def escape_yaml_value(value: str) -> str:
    """Escape special characters for YAML string values."""
    # If value contains special chars, wrap in quotes
    if any(c in value for c in [':', '#', '"', "'", '\n', '[', ']', '{', '}']):
        # Escape existing quotes and wrap in quotes
        return '"' + value.replace('\\', '\\\\').replace('"', '\\"') + '"'
    return f'"{value}"'


def write_yaml_file(locale: str, materials: dict):
    """Write materials to YAML file."""
    output_file = OUTPUT_DIR / f"materials-{locale}.yml"

    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("# SignShop material translations\n")
        f.write(f"# Locale: {locale}\n")
        f.write(f"# Generated from Minecraft {MC_VERSION}\n")
        f.write("# User customizations in materials.yml will override these\n\n")
        f.write("materials:\n")

        # Sort materials alphabetically
        for material in sorted(materials.keys()):
            value = escape_yaml_value(materials[material])
            f.write(f"  {material}: {value}\n")

    print(f"  Written: {output_file.name} ({len(materials)} materials)")


def main():
    print(f"=== SignShop Material Translation Generator ===")
    print(f"Minecraft version: {MC_VERSION}")
    print(f"Output directory: {OUTPUT_DIR}\n")

    # Ensure output directory exists
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    # Get version info and asset index
    print("Step 1: Getting Minecraft version info...")
    version_info = get_version_info(MC_VERSION)

    print("Step 2: Getting asset index...")
    asset_index = get_asset_index(version_info)

    # Find all language files
    print("Step 3: Finding language files...")
    lang_files = {}
    for asset_path, asset_info in asset_index["objects"].items():
        if asset_path.startswith("minecraft/lang/") and asset_path.endswith(".json"):
            locale = asset_path.replace("minecraft/lang/", "").replace(".json", "")
            lang_files[locale] = asset_info["hash"]

    print(f"  Found {len(lang_files)} language files\n")

    # Process each language file
    print("Step 4: Processing language files...")
    success_count = 0
    error_count = 0

    for locale, file_hash in sorted(lang_files.items()):
        try:
            # Download language file
            lang_data = download_lang_file(file_hash)

            # Extract material translations
            materials = extract_material_translations(lang_data)

            if materials:
                # Write YAML file
                write_yaml_file(locale, materials)
                success_count += 1
            else:
                print(f"  Skipped: {locale} (no materials found)")

        except Exception as e:
            print(f"  Error processing {locale}: {e}")
            error_count += 1

    print(f"\n=== Complete ===")
    print(f"Successfully generated: {success_count} files")
    if error_count > 0:
        print(f"Errors: {error_count}")


if __name__ == "__main__":
    main()
