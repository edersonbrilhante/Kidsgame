# syntax=docker/dockerfile:1
#
# Builds the debug APK inside a container. Run this on a machine WITH internet
# (your laptop, a server, or CI) — the build downloads the Android SDK + Gradle
# dependencies, so it cannot run in an offline sandbox.
#
# Quickest way to get the APK out (uses BuildKit's --output):
#     ./docker-build.sh
# ...which is equivalent to:
#     DOCKER_BUILDKIT=1 docker build --target export --output type=local,dest=./out .
# The APK lands at ./out/app-debug.apk

########## Stage 1: build ##########
FROM eclipse-temurin:17-jdk AS builder

# Bump these if a download 404s (Google rotates the cmdline-tools build number).
ARG ANDROID_CMDLINE_VERSION=11076708
ARG ANDROID_PLATFORM=android-35
ARG ANDROID_BUILD_TOOLS=35.0.0
ARG GRADLE_VERSION=8.9

ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y --no-install-recommends \
        curl unzip ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Android command-line tools
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    curl -sSLo /tmp/cmdline.zip \
      https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_VERSION}_latest.zip && \
    unzip -q /tmp/cmdline.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest && \
    rm /tmp/cmdline.zip

ENV PATH="${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${PATH}"

# Accept licenses + install required SDK packages
RUN yes | sdkmanager --licenses > /dev/null && \
    sdkmanager --install \
      "platform-tools" \
      "platforms;${ANDROID_PLATFORM}" \
      "build-tools;${ANDROID_BUILD_TOOLS}" > /dev/null

# Gradle (project ships no wrapper jar, so install Gradle directly)
RUN curl -sSLo /tmp/gradle.zip \
      https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip -q /tmp/gradle.zip -d /opt && \
    ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/local/bin/gradle && \
    rm /tmp/gradle.zip

WORKDIR /project
COPY . .

RUN gradle --no-daemon :app:assembleDebug

########## Stage 2: export only the APK ##########
FROM scratch AS export
COPY --from=builder /project/app/build/outputs/apk/debug/app-debug.apk /app-debug.apk
