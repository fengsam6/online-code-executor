# 使用Java 22作为基础镜像
FROM openjdk:22-slim

# 安装必要的编译器和工具
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    golang \
    python3 \
    dos2unix \
    && rm -rf /var/lib/apt/lists/*

# 创建必要的目录
RUN mkdir -p /app/scripts /app/tmp
WORKDIR /app

# 复制应用文件并设置权限
COPY target/*.jar app.jar
COPY scripts/run-code.sh /app/scripts/
RUN chmod +x /app/scripts/run-code.sh
RUN dos2unix /app/scripts/run-code.sh

# 设置Java虚拟线程参数
ENV JAVA_TOOL_OPTIONS="-XX:+UseZGC -XX:+ZGenerational"

# 启动应用
CMD ["java", "-jar", "app.jar"] 