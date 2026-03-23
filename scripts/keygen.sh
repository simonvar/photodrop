#!/usr/bin/env bash
set -euo pipefail

VERSION="1.0.0"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PEPK_JAR="${SCRIPT_DIR}/pepk.jar"

# --- ANSI colors (auto-disable when stdout is not a TTY) ---------------
if [[ -t 1 ]]; then
    RED=$'\033[0;31m'
    GREEN=$'\033[0;32m'
    YELLOW=$'\033[1;33m'
    BLUE=$'\033[0;34m'
    CYAN=$'\033[0;36m'
    BOLD=$'\033[1m'
    NC=$'\033[0m'
else
    RED='' GREEN='' YELLOW='' BLUE='' CYAN='' BOLD='' NC=''
fi

# --- Utilities ---------------------------------------------------------
info()    { printf '%s\n' "${BLUE}[INFO]${NC} $*"; }
success() { printf '%s\n' "${GREEN}[OK]${NC} $*"; }
warn()    { printf '%s\n' "${YELLOW}[WARN]${NC} $*" >&2; }
error()   { printf '%s\n' "${RED}[ERROR]${NC} $*" >&2; }
die()     { error "$@"; exit 1; }

# --- Dependency checks -------------------------------------------------
check_deps() {
    if ! command -v java &>/dev/null; then
        die "java not found. Install JDK 11+."
    fi
    local java_ver
    java_ver=$(java -version 2>&1 | head -1 | sed -E 's/.*"([0-9]+).*/\1/')
    if [[ "$java_ver" -lt 11 ]]; then
        die "Java 11+ required, found version ${java_ver}."
    fi
    if ! command -v keytool &>/dev/null; then
        die "keytool not found. It ships with any JDK."
    fi
}

# --- Password generation -----------------------------------------------
gen_password() {
    local length="${1:-48}"
    local first_char
    first_char=$(LC_ALL=C tr -dc 'A-Za-z' </dev/urandom | head -c 1)
    local rest
    rest=$(LC_ALL=C tr -dc 'A-Za-z0-9_.~@#%+=' </dev/urandom | head -c "$((length - 1))")
    printf '%s%s\n' "$first_char" "$rest"
}

# --- Interactive password prompt ----------------------------------------
prompt_password() {
    local password="" confirm=""
    if [[ -n "${KEYSTORE_PASSWORD:-}" ]]; then
        password="$KEYSTORE_PASSWORD"
    else
        while true; do
            echo -n "Enter keystore password: " >&2
            read -rs password
            echo >&2
            if [[ ${#password} -lt 8 ]]; then
                error "Password must be at least 8 characters."
                continue
            fi
            echo -n "Confirm password: " >&2
            read -rs confirm
            echo >&2
            if [[ "$password" != "$confirm" ]]; then
                error "Passwords do not match."
                continue
            fi
            break
        done
    fi
    echo "$password"
}

# --- Validation helpers -------------------------------------------------
validate_keysize() {
    case "$1" in
        2048|3072|4096) return 0 ;;
        *) die "Invalid --keysize '$1'. Allowed: 2048, 3072, 4096." ;;
    esac
}

validate_sigalg() {
    case "$1" in
        SHA256withRSA|SHA384withRSA|SHA512withRSA) return 0 ;;
        *) die "Invalid --sigalg '$1'. Allowed: SHA256withRSA, SHA384withRSA, SHA512withRSA." ;;
    esac
}

# --- Print fingerprints ------------------------------------------------
print_fingerprints() {
    local keystore="$1" alias="$2" storepass="$3"
    echo ""
    info "Certificate fingerprints:"
    echo "─────────────────────────────────────────────────"
    local sha256 sha1
    sha256=$(keytool -list -v -keystore "$keystore" -alias "$alias" -storepass "$storepass" 2>/dev/null \
        | grep "SHA256:" | sed 's/.*SHA256: //')
    sha1=$(keytool -list -v -keystore "$keystore" -alias "$alias" -storepass "$storepass" 2>/dev/null \
        | grep "SHA1:" | sed 's/.*SHA1: //')
    printf '%s\n' "  ${BOLD}SHA-256:${NC} ${sha256:-N/A}"
    printf '%s\n' "  ${BOLD}SHA-1:${NC}   ${sha1:-N/A}"
    echo "─────────────────────────────────────────────────"
}

# --- Usage --------------------------------------------------------------
usage() {
    cat <<EOF
${BOLD}keygen.sh${NC} v${VERSION} — Keystore management for AAB signing

${BOLD}USAGE:${NC}
    $(basename "$0") <command> [options]
    $(basename "$0") --help | --version

${BOLD}COMMANDS:${NC}
    generate    Create a new PKCS12 keystore (RSA)
    pepk        Export encrypted private key for market upload (via pepk.jar)
    pem         Export public certificate in PEM format

${BOLD}GENERATE OPTIONS:${NC}
    --output <path>         (required) Output keystore file path (.keystore)
    --alias <name>          (required) Key alias
    --cn <name>             (required) Common Name (CN)
    --ou <name>             Organizational Unit (OU)
    --o <name>              Organization (O)
    --l <city>              Locality / city (L)
    --st <state>            State / region (ST)
    --c <code>              Country code, 2 letters (C), e.g. RU
    --keysize <bits>        RSA key size: 2048 | 3072 | 4096 (default: 4096)
    --sigalg <alg>          Signature algorithm (default: SHA512withRSA)
                            Allowed: SHA256withRSA, SHA384withRSA, SHA512withRSA
    --days <n>              Validity in days (default: 10000, ~27 years)
    --startdate <date>      Validity start date (YYYY-MM-DD), default: now
    --gen-password          Auto-generate a secure password
    --password-length <n>   Generated password length, 16–128 (default: 48)

${BOLD}PEPK OPTIONS:${NC}
    --keystore <path>       (required) Path to keystore file
    --alias <name>          (required) Key alias
    --output <path>         (required) Output .zip file path
    --encryption-key <hex>  (required) Encryption key from Play Console

${BOLD}PEM OPTIONS:${NC}
    --keystore <path>       (required) Path to keystore file
    --alias <name>          (required) Key alias
    --output <path>         (required) Output .pem file path

${BOLD}PASSWORD:${NC}
    generate/pem: read interactively (read -s) or from KEYSTORE_PASSWORD env.
    pepk: password is prompted by pepk.jar itself.

${BOLD}EXAMPLES:${NC}
    # Generate a new keystore with auto-generated password
    $(basename "$0") generate --output upload.keystore --alias <name> \\
        --cn "domain.com" --o "Company" --c RU --gen-password

    # Export encrypted key for Google Play
    $(basename "$0") pepk --keystore upload.keystore --alias <name> \\
        --output pepk_out.zip --encryption-key <hex>

    # Export PEM certificate
    $(basename "$0") pem --keystore upload.keystore --alias <name> \\
        --output upload.pem
EOF
}

# --- cmd_generate -------------------------------------------------------
cmd_generate() {
    local output="" alias_name="" cn="" ou="" o="" l="" st="" c=""
    local keysize=4096 sigalg="SHA512withRSA" days=10000 startdate=""
    local gen_pass=false pass_length=48

    while [[ $# -gt 0 ]]; do
        case "$1" in
            --output)          output="$2"; shift 2 ;;
            --alias)           alias_name="$2"; shift 2 ;;
            --cn)              cn="$2"; shift 2 ;;
            --ou)              ou="$2"; shift 2 ;;
            --o)               o="$2"; shift 2 ;;
            --l)               l="$2"; shift 2 ;;
            --st)              st="$2"; shift 2 ;;
            --c)               c="$2"; shift 2 ;;
            --keysize)         keysize="$2"; shift 2 ;;
            --sigalg)          sigalg="$2"; shift 2 ;;
            --days)            days="$2"; shift 2 ;;
            --startdate)       startdate="$2"; shift 2 ;;
            --gen-password)    gen_pass=true; shift ;;
            --password-length) pass_length="$2"; shift 2 ;;
            *) die "Unknown option: $1. Run with --help for usage." ;;
        esac
    done

    # --- Required params ---
    [[ -z "$output" ]] && die "Missing required --output."
    [[ -z "$alias_name" ]] && die "Missing required --alias."
    [[ -z "$cn" ]] && die "Missing required --cn."

    # --- Validations ---
    validate_keysize "$keysize"
    validate_sigalg "$sigalg"

    if [[ "$output" == *.jks ]]; then
        die "JKS format is not supported. Use .keystore extension (PKCS12)."
    fi
    if [[ "$output" != *.keystore ]]; then
        warn "Output file does not have .keystore extension."
    fi
    if [[ -f "$output" ]]; then
        die "File '$output' already exists. Remove it first or choose a different path."
    fi

    if [[ -n "$c" && ${#c} -ne 2 ]]; then
        die "Country code (--c) must be exactly 2 characters, got '${c}'."
    fi

    if [[ "$gen_pass" == true ]]; then
        if [[ "$pass_length" -lt 16 || "$pass_length" -gt 128 ]]; then
            die "--password-length must be between 16 and 128, got ${pass_length}."
        fi
    fi

    # --- Build dname ---
    local dname="CN=${cn}"
    [[ -n "$ou" ]] && dname="${dname}, OU=${ou}"
    [[ -n "$o" ]]  && dname="${dname}, O=${o}"
    [[ -n "$l" ]]  && dname="${dname}, L=${l}"
    [[ -n "$st" ]] && dname="${dname}, ST=${st}"
    [[ -n "$c" ]]  && dname="${dname}, C=${c}"

    # --- Password ---
    local password=""
    if [[ "$gen_pass" == true ]]; then
        password=$(gen_password "$pass_length")
        echo ""
        printf '%s\n' "${BOLD}Generated password:${NC}"
        echo "─────────────────────────────────────────────────"
        printf '%s\n' "  ${CYAN}${password}${NC}"
        echo "─────────────────────────────────────────────────"
        warn "Save this password securely! It will NOT be shown again."
        echo ""
    else
        password=$(prompt_password)
    fi

    # --- Build keytool command ---
    local keytool_args=(
        -genkeypair
        -storetype PKCS12
        -keyalg RSA
        -keysize "$keysize"
        -sigalg "$sigalg"
        -validity "$days"
        -alias "$alias_name"
        -dname "$dname"
        -keystore "$output"
        -storepass "$password"
        -keypass "$password"
    )

    if [[ -n "$startdate" ]]; then
        keytool_args+=(-startdate "$startdate")
    fi

    info "Generating PKCS12 keystore..."
    info "  Output:   ${output}"
    info "  Alias:    ${alias_name}"
    info "  DN:       ${dname}"
    info "  Key:      RSA ${keysize}"
    info "  SigAlg:   ${sigalg}"
    info "  Validity: ${days} days"
    echo ""

    keytool "${keytool_args[@]}"

    success "Keystore created: ${output}"

    print_fingerprints "$output" "$alias_name" "$password"

    # --- secure.properties block ---
    local basename
    basename=$(basename "$output")
    echo ""
    info "Add to ${BOLD}app/secure.properties${NC} (replace <market> with google, rustore, or huawei):"
    echo "─────────────────────────────────────────────────"
    echo "<market>StoreFile=../tools/keys/${basename}"
    echo "<market>StorePassword=${password}"
    echo "<market>KeyAlias=${alias_name}"
    echo "<market>KeyPassword=${password}"
    echo "─────────────────────────────────────────────────"
    echo ""
    info "Example for Google Play:"
    echo "  googleStoreFile=../tools/keys/${basename}"
    echo "  googleStorePassword=${password}"
    echo "  googleKeyAlias=${alias_name}"
    echo "  googleKeyPassword=${password}"
}

# --- cmd_pepk -----------------------------------------------------------
cmd_pepk() {
    local keystore="" alias_name="" output="" encryption_key=""

    while [[ $# -gt 0 ]]; do
        case "$1" in
            --keystore)       keystore="$2"; shift 2 ;;
            --alias)          alias_name="$2"; shift 2 ;;
            --output)         output="$2"; shift 2 ;;
            --encryption-key) encryption_key="$2"; shift 2 ;;
            *) die "Unknown option: $1. Run with --help for usage." ;;
        esac
    done

    [[ -z "$keystore" ]] && die "Missing required --keystore."
    [[ -z "$alias_name" ]] && die "Missing required --alias."
    [[ -z "$output" ]] && die "Missing required --output."
    [[ -z "$encryption_key" ]] && die "Missing required --encryption-key."

    [[ ! -f "$keystore" ]] && die "Keystore file not found: ${keystore}"
    [[ ! -f "$PEPK_JAR" ]] && die "pepk.jar not found at: ${PEPK_JAR}"

    info "Running pepk.jar..."
    info "  Keystore: ${keystore}"
    info "  Alias:    ${alias_name}"
    info "  Output:   ${output}"
    echo ""

    java -jar "$PEPK_JAR" \
        --keystore="$keystore" \
        --alias="$alias_name" \
        --output="$output" \
        --encryptionkey="$encryption_key" \
        --include-cert

    success "Encrypted key exported: ${output}"
}

# --- cmd_pem ------------------------------------------------------------
cmd_pem() {
    local keystore="" alias_name="" output=""

    while [[ $# -gt 0 ]]; do
        case "$1" in
            --keystore) keystore="$2"; shift 2 ;;
            --alias)    alias_name="$2"; shift 2 ;;
            --output)   output="$2"; shift 2 ;;
            *) die "Unknown option: $1. Run with --help for usage." ;;
        esac
    done

    [[ -z "$keystore" ]] && die "Missing required --keystore."
    [[ -z "$alias_name" ]] && die "Missing required --alias."
    [[ -z "$output" ]] && die "Missing required --output."

    [[ ! -f "$keystore" ]] && die "Keystore file not found: ${keystore}"

    # --- Password ---
    local password=""
    if [[ -n "${KEYSTORE_PASSWORD:-}" ]]; then
        password="$KEYSTORE_PASSWORD"
    else
        echo -n "Enter keystore password: " >&2
        read -rs password
        echo >&2
    fi

    info "Exporting PEM certificate..."
    info "  Keystore: ${keystore}"
    info "  Alias:    ${alias_name}"
    info "  Output:   ${output}"
    echo ""

    keytool -exportcert -rfc \
        -keystore "$keystore" \
        -alias "$alias_name" \
        -storepass "$password" \
        -file "$output"

    success "PEM certificate exported: ${output}"

    print_fingerprints "$keystore" "$alias_name" "$password"
}

# --- Main dispatcher ----------------------------------------------------
main() {
    if [[ $# -eq 0 ]]; then
        usage
        exit 1
    fi

    case "$1" in
        generate)
            check_deps
            shift
            cmd_generate "$@"
            ;;
        pepk)
            check_deps
            shift
            cmd_pepk "$@"
            ;;
        pem)
            check_deps
            shift
            cmd_pem "$@"
            ;;
        --help|-h)
            usage
            exit 0
            ;;
        --version|-v)
            echo "keygen.sh v${VERSION}"
            exit 0
            ;;
        *)
            die "Unknown command: $1. Run with --help for usage."
            ;;
    esac
}

main "$@"
