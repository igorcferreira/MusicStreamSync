# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Stage 1: build with JDK 21.
#
# 21 (not 17) because the generated arkana/build.gradle.kts pins
# jvmToolchain(21) ("DO NOT MODIFY" header) and settings.gradle.kts has no
# toolchain resolver, so a 17-only container cannot provision 21. The other
# modules target 17 bytecode, which runs fine on 21.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build

# ruby for the Arkana secret generator; unzip/curl for the Android tooling.
RUN apt-get update \
    && apt-get install -y --no-install-recommends ruby-full unzip curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && gem install arkana

# Configuring the build applies AGP to :shared/:composeApp, which requires an
# Android SDK location with accepted licenses even though only JVM variants
# are compiled here.
ENV ANDROID_HOME=/opt/android-sdk
RUN mkdir -p "$ANDROID_HOME/cmdline-tools" \
    && curl -fsSL https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip \
        -o /tmp/cmdline-tools.zip \
    && unzip -q /tmp/cmdline-tools.zip -d "$ANDROID_HOME/cmdline-tools" \
    && mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest" \
    && rm /tmp/cmdline-tools.zip \
    && yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null

WORKDIR /app
COPY . .

# Generate the obfuscated secrets module (reads .env; see server/README.md).
RUN arkana -l kotlin

RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon :server:installDist

# ---------------------------------------------------------------------------
# Stage 2: slim JRE 21 runtime, non-root.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime

RUN useradd --system --home /app --shell /usr/sbin/nologin appuser
WORKDIR /app
COPY --from=build /app/server/build/install/server/ /app/
USER appuser

EXPOSE 8080
ENTRYPOINT ["/app/bin/server"]
