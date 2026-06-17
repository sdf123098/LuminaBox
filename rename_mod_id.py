import os
import shutil

project_dir = r"D:\Project\MCMOD\custom-bgm"

# 1. Rename directories named "dynamicbgm" to "luminabox"
for root, dirs, files in os.walk(project_dir, topdown=False):
    if ".gradle" in root or "build" in root or ".idea" in root or ".git" in root:
        continue
    for d in dirs:
        if d == "dynamicbgm":
            old_path = os.path.join(root, d)
            new_path = os.path.join(root, "luminabox")
            os.rename(old_path, new_path)
            print(f"Renamed dir: {old_path} -> {new_path}")
        elif d == "dynamic_bgm":
            old_path = os.path.join(root, d)
            new_path = os.path.join(root, "luminabox")
            os.rename(old_path, new_path)
            print(f"Renamed dir: {old_path} -> {new_path}")

# 2. Text replacements
replacements = {
    "dynamic_bgm": "luminabox",
    "dynamic-bgm": "luminabox",
    "DynamicBgm": "LuminaBox",
    "DynamicBGM": "LuminaBox",
    "dynamicbgm": "luminabox",
    "dynamic_music": "luminabox"
}

# 3. File renames
file_renames = {
    "DynamicBgm.java": "LuminaBox.java",
    "DynamicBgmClient.java": "LuminaBoxClient.java",
    "DynamicBgmFabric.java": "LuminaBoxFabric.java",
    "DynamicBgmNeoForge.java": "LuminaBoxNeoForge.java",
    "DynamicBgmClientFabric.java": "LuminaBoxClientFabric.java",
    "DynamicBgmClientNeoForge.java": "LuminaBoxClientNeoForge.java"
}

def process_file(path):
    # Rename file if needed
    basename = os.path.basename(path)
    if basename in file_renames:
        new_path = os.path.join(os.path.dirname(path), file_renames[basename])
        os.rename(path, new_path)
        path = new_path

    # Read and replace content
    try:
        with open(path, 'r', encoding='utf-8') as f:
            content = f.read()
    except UnicodeDecodeError:
        return

    new_content = content
    for old, new in replacements.items():
        new_content = new_content.replace(old, new)
        
    if new_content != content:
        with open(path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated: {path}")

# Traverse and process
for root, dirs, files in os.walk(project_dir):
    if ".gradle" in root or "build" in root or ".idea" in root or ".git" in root:
        continue
    for file in files:
        if file.endswith((".java", ".json", ".toml", ".kts", ".gradle", ".properties", ".xml", ".mixins.json")):
            process_file(os.path.join(root, file))

# Rename mixin config files
for root, dirs, files in os.walk(project_dir):
    if ".gradle" in root or "build" in root or ".idea" in root or ".git" in root:
        continue
    for file in files:
        if file == "dynamic_bgm.mixins.json":
            os.rename(os.path.join(root, file), os.path.join(root, "luminabox.mixins.json"))
        elif file == "dynamic_bgm-common.mixins.json":
            os.rename(os.path.join(root, file), os.path.join(root, "luminabox-common.mixins.json"))

print("Rename complete!")
