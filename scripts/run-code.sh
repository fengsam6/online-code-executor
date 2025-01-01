#!/usr/bin/env bash

# 确保脚本使用LF换行符
set -e  # 遇到错误立即退出
set -u  # 使用未定义的变量时报错

# 设置资源限制
ulimit -t 5      # CPU时间限制5秒
ulimit -v 100000 # 虚拟内存限制100MB
ulimit -f 1024   # 文件大小限制1MB

# 设置字符编码
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8

command=$1
code_file=$2
dir_path=$(dirname "$code_file")

case "$command" in
    "javac-and-run")
        # 获取文件名（不含扩展名）
        base_name=$(basename "$code_file" .java)
        
        # 编译Java代码
        javac -J-Xmx256m -J-XX:+UseParallelGC "$code_file"
        if [ $? -eq 0 ]; then
            # 运行Java程序
            java -XX:+UseParallelGC -XX:ParallelGCThreads=2 \
                 -Xmx256m -XX:MaxRAM=512m \
                 -cp $(dirname "$code_file") "$base_name"
        else
            echo "Java compilation failed"
            exit 1
        fi
        ;;
        
    "gcc -O2 -Wall -fno-asm -D_FORTIFY_SOURCE=2 -o Main")
        # 进入代码所在目录
        cd "$dir_path" || exit 1
        
        # 编译C代码，指定输出文件为Main
        $command "$code_file" 2>&1
        if [ $? -eq 0 ]; then
            # 确保输出文件存在且可执行
            if [ -f "./Main" ]; then
                chmod +x "./Main"
                # 运行程序
                timeout 3s "./Main"
            else
                echo "编译成功但可执行文件未生成"
                exit 1
            fi
        else
            echo "C compilation failed"
            exit 1
        fi
        ;;
        
    "g++ -O2 -Wall -std=c++17 -fno-asm -D_FORTIFY_SOURCE=2")
        output_file="${code_file%.*}"
        
        # 编译C++代码
        $command "$code_file" -o "$output_file" 2>&1
        if [ $? -eq 0 ]; then
            # 运行C++程序
            timeout 3s "$output_file"
        else
            echo "C++ compilation failed"
            exit 1
        fi
        ;;
        
    "go run -gcflags='-N -l'")
        # 设置Go环境变量
        export GOOS=linux
        export GOARCH=amd64
        export GOMAXPROCS=2
        export GOGC=50
        
        # 运行Go代码
        cd $(dirname "$code_file")
        $command "$code_file" 2>&1
        ;;
        
    "python3")
        # 运行Python代码
        timeout 3s python3 "$code_file"
        ;;
        
    *)
        echo "Unsupported language"
        exit 1
        ;;
esac 