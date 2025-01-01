#!/usr/bin/env bash

# 确保脚本使用LF换行符
set -e  # 遇到错误立即退出
set -u  # 使用未定义的变量时报错

# 检测操作系统类型
if [[ "$OSTYPE" == "darwin"* ]]; then
    # Mac OS X 使用 gtimeout (需要安装 coreutils)
    TIMEOUT_CMD="gtimeout"
else
    # Linux 使用 timeout
    TIMEOUT_CMD="timeout"
fi

# 定义超时函数
function run_with_timeout() {
    if command -v $TIMEOUT_CMD >/dev/null 2>&1; then
        $TIMEOUT_CMD --foreground 3s "$@"
    else
        # 如果没有 timeout 命令，使用 perl 实现超时
        perl -e '
            use strict;
            use IPC::Open3;
            my $pid = open3(undef, undef, undef, @ARGV);
            eval {
                local $SIG{ALRM} = sub { die "timeout\n" };
                alarm 3;
                waitpid($pid, 0);
                alarm 0;
            };
            if ($@ =~ /timeout/) {
                kill 9, $pid;
                exit 124;
            }
            exit $? >> 8;
        ' -- "$@"
    fi
}

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
            run_with_timeout java -XX:+UseParallelGC -XX:ParallelGCThreads=2 \
                 -Xmx256m -XX:MaxRAM=512m \
                 -cp . "$base_name"
        else
            echo "Java compilation failed"
            exit 1
        fi
        ;;
        
    "gcc -O2 -Wall -fno-asm -D_FORTIFY_SOURCE=2 -o Main")
        # 编译C代码
        $command "$code_file" 2>&1
        if [ $? -eq 0 ]; then
            chmod +x "./Main"
            # 运行程序
            run_with_timeout "./Main"
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
            run_with_timeout "$output_file"
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
        run_with_timeout $command "$code_file" 2>&1
        ;;
        
    "python3")
        # 运行Python代码
        run_with_timeout python3 "$code_file"
        ;;
        
    *)
        echo "Unsupported language"
        exit 1
        ;;
esac 