#!/bin/bash

# 设置资源限制
ulimit -t 5      # CPU时间限制5秒
ulimit -v 100000 # 虚拟内存限制100MB
ulimit -f 1024   # 文件大小限制1MB

command=$1
code_file=$2

case "$command" in
    "javac-and-run")
        # 获取文件名（不含扩展名）
        base_name=$(basename "$code_file" .java)
        
        # 编译Java代码
        javac -J-Xmx256m -J-XX:+UseParallelGC "$code_file"
        if [ $? -eq 0 ]; then
            # 运行Java程序，使用文件名作为类名
            java -XX:+UseParallelGC -XX:ParallelGCThreads=2 \
                 -Xmx256m -XX:MaxRAM=512m \
                 -cp $(dirname "$code_file") "$base_name"
        else
            echo "Java compilation failed"
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
        
    *)
        echo "Unsupported language"
        exit 1
        ;;
esac 