FROM debian:12-slim AS build

RUN apt-get update && \
    apt-get install -y wget gcc libz-dev maven && \
    rm -rf /var/lib/apt/lists/*

RUN wget https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-21.0.2/graalvm-community-jdk-21.0.2_linux-x64_bin.tar.gz && \
    tar -xvzf graalvm-community-jdk-21.0.2_linux-x64_bin.tar.gz && \
    rm graalvm-community-jdk-21.0.2_linux-x64_bin.tar.gz

# Set the GraalVM installation path
ENV GRAALVM_HOME=/graalvm-community-openjdk-21.0.2+13.1
ENV PATH=$GRAALVM_HOME/bin:$PATH

# Set working directory
WORKDIR /app

# Copy the Maven wrapper and project files
COPY . .

# Build the native image
RUN mvn -Pnative native:compile

# Use a lightweight runtime image for the final build
FROM frolvlad/alpine-glibc

# Set up workdir
WORKDIR /app

# Copy the native executable from the build stage
COPY --from=build /app/target/multi-tic-tac /app/multi-tic-tac

# Set executable permissions
RUN chmod +x /app/multi-tic-tac

# Expose application port
EXPOSE 10000

# Start the application
CMD ["./multi-tic-tac"]
