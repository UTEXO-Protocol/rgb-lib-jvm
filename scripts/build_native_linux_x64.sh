#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RGB_LIB_DIR="${ROOT_DIR}/vendor/rgb-lib"
CFFI_DIR="${RGB_LIB_DIR}/bindings/c-ffi"

if [[ ! -d "${CFFI_DIR}" ]]; then
  echo "Missing submodule sources at ${CFFI_DIR}"
  echo "Run: git submodule update --init --recursive"
  exit 1
fi

# Keep cargo cache local to this repository for reproducible CI runs.
export CARGO_HOME="${ROOT_DIR}/.cargo-home"
mkdir -p "${CARGO_HOME}"

cd "${CFFI_DIR}"

cargo build --release --target x86_64-unknown-linux-gnu

TARGET_ROOT="${CARGO_TARGET_DIR:-${CFFI_DIR}/target}"
SRC_LIB="${TARGET_ROOT}/x86_64-unknown-linux-gnu/release/librgblibcffi.so"

if [[ ! -f "${SRC_LIB}" ]]; then
  echo "Expected native library not found: ${SRC_LIB}"
  exit 1
fi

OUT_DIR="${ROOT_DIR}/build/native/linux-x86_64"
mkdir -p "${OUT_DIR}"
cp "${SRC_LIB}" "${OUT_DIR}/"
cp "${CFFI_DIR}/rgblib.h" "${OUT_DIR}/"
cp "${CFFI_DIR}/rgblib.hpp" "${OUT_DIR}/"

echo "Native artifacts prepared in ${OUT_DIR}:"
ls -la "${OUT_DIR}"
