#!/bin/bash

# 柠檬Push Windows AMD64 构建脚本
# 此脚本会编译应用并压缩成 lemon_push_v版本号_windows_amd64.zip 格式

# 获取当前脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 获取版本号
VERSION=$(cat wails.json | grep -o '"productVersion": "[^"]*"' | cut -d'"' -f4)
if [ -z "$VERSION" ]; then
    VERSION="1.0.0"
fi

# 设置平台信息和输出文件名
PLATFORM="windows_amd64"
OUTPUT_NAME="lemon_push_v${VERSION}_${PLATFORM}"
ZIP_FILE="${OUTPUT_NAME}.zip"

echo "开始构建柠檬Push Windows AMD64版本..."
echo "版本号: $VERSION"
echo "平台: $PLATFORM"

# 清理之前的构建
echo "清理之前的构建文件..."
rm -rf build/bin/*
rm -f "$ZIP_FILE"

# 构建应用
echo "开始编译应用..."
wails build -platform "windows/amd64" -clean

# 检查构建是否成功
if [ $? -ne 0 ]; then
    echo "构建失败，请检查错误信息"
    exit 1
fi

# 获取构建后的可执行文件路径
EXE_NAME="柠檬Push.exe"
EXE_PATH="build/bin/$EXE_NAME"

# 检查可执行文件是否存在
if [ ! -f "$EXE_PATH" ]; then
    echo "错误: 找不到构建后的可执行文件 $EXE_PATH"
    exit 1
fi

# 创建临时目录用于打包
TEMP_DIR="temp_package"
mkdir -p "$TEMP_DIR"

# 复制可执行文件到临时目录
echo "复制可执行文件到临时目录..."
cp "$EXE_PATH" "$TEMP_DIR/"

# 创建版本信息文件
echo "创建版本信息文件..."
cat > "$TEMP_DIR/VERSION.txt" << EOF
柠檬Push
版本: $VERSION
平台: Windows AMD64
构建时间: $(date)
EOF

# 压缩应用
echo "压缩应用..."
cd "$TEMP_DIR"
zip -r "../$ZIP_FILE" .
cd ..

# 清理临时目录
echo "清理临时文件..."
rm -rf "$TEMP_DIR"

# 检查压缩文件是否创建成功
if [ -f "$ZIP_FILE" ]; then
    echo "构建成功! 输出文件: $ZIP_FILE"
    ls -lh "$ZIP_FILE"
else
    echo "错误: 创建压缩文件失败"
    exit 1
fi

echo "构建完成!"